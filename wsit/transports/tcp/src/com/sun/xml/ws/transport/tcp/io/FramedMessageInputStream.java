/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
 */

package com.sun.xml.ws.transport.tcp.io;

import com.sun.xml.ws.transport.tcp.pool.LifeCycle;
import com.sun.xml.ws.transport.tcp.util.FrameType;
import com.sun.xml.ws.transport.tcp.util.SelectorFactory;
import com.sun.xml.ws.transport.tcp.util.TCPConstants;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Stream wrapper around a <code>ByteBuffer</code>
 */
public class FramedMessageInputStream extends InputStream implements LifeCycle {
    private static final Logger logger = Logger.getLogger(
            com.sun.xml.ws.transport.tcp.util.TCPConstants.LoggingDomain + ".streams");
    
    private ByteBuffer byteBuffer;
    private boolean useDirectBuffer;
    
    private DataInputStream dis = new DataInputStream(this);
    private SocketChannel socketChannel;
    
    private int bufferClearLimit;  // clear buffer before read from socket if its size already more than this value
    /**
     * The time to wait before timing out when reading bytes
     */
    private int readTimeout = 1000;
    
    
    private int[] headerTmpArray = new int[2];
    
    /** is message framed or direct mode is used */
    private boolean isDirectMode;
    
    /**
     * Number of times to retry before return EOF
     */
    protected int readTry = 10;
    
    private int frameSize;
    private int frameBytesRead;
    private boolean isLastFrame;
    private int currentFrameDataSize;    // for last frame actual data size could be smaller than frame size
    
    private int channelId;
    private int contentId;
    private int messageId;
    private Map<Integer, String> contentParameters = new HashMap<Integer, String>();
    
    private boolean isReadingHeader;
    
    /**
     * could be useful for debug reasons
     */
    private long receivedMessageLength;
    
    // ------------------------------------------------- Constructor -------//
    
    
    public FramedMessageInputStream() {
        this(TCPConstants.DEFAULT_FRAME_SIZE);
    }
    
    public FramedMessageInputStream(int frameSize) {
        setFrameSize(frameSize);
    }
    // ---------------------------------------------------------------------//
    
    
    public void setSocketChannel(SocketChannel socketChannel){
        this.socketChannel = socketChannel;
    }
    
    public int getChannelId() {
        return channelId;
    }
    
    public int getMessageId() {
        return messageId;
    }
    
    public int getContentId() {
        return contentId;
    }
    
    public Map<Integer, String> getContentProps() {
        return contentParameters;
    }
    
    public boolean isDirectMode() {
        return isDirectMode;
    }
    
    public void setDirectMode(boolean isDirectMode) {
        reset();
        this.isDirectMode = isDirectMode;
    }
    
    public void setFrameSize(int frameSize) {
        this.frameSize = frameSize;
    }
    
    public void setByteBuffer(ByteBuffer byteBuffer) {
        this.byteBuffer = byteBuffer;
        if (byteBuffer != null) {
            bufferClearLimit = byteBuffer.capacity() * 3 / 4;
        }
    }
    
    /**
     * Return the available bytes
     * @return the wrapped byteBuffer.remaining()
     */
    public int available() {
        return remaining();
    }
    
    
    /**
     * Close this stream.
     */
    public void close() {
        try {
            dis.close();
        } catch (IOException ex) {
        }
    }
    
    /**
     * Return true if mark is supported.
     */
    public boolean markSupported() {
        return false;
    }
    
    /**
     * Read the first byte from the wrapped <code>ByteBuffer</code>.
     */
    public int read() {
        int eof = 0;
        if (!byteBuffer.hasRemaining()){
            eof = readFromChannel();
        } else if (remaining() == 0 && isLastFrame) {   // if in buffer there is last frame's tale only
            return -1;
        }
        
        if (eof == -1 || readFrameHeaderIfRequired() == -1) return -1;
        
        if (byteBuffer.hasRemaining()) {
            frameBytesRead++;
            receivedMessageLength ++;
            int ret = byteBuffer.get() & 0xff;
            return ret;
        }
        
        return read();
    }
    
    
    /**
     * Read the bytes from the wrapped <code>ByteBuffer</code>.
     */
    public int read(byte[] b) {
        return (read(b, 0, b.length));
    }
    
    
    /**
     * Read the first byte of the wrapped <code>ByteBuffer</code>.
     */
    public int read(byte[] b, int offset, int length) {
        if (!byteBuffer.hasRemaining()) {
            int eof = readFromChannel();
            if (eof <= 0){
                return -1;
            }
        } else if (remaining() == 0 && isLastFrame) {   // if in buffer there is last frame's tale only
            return -1;
        }
        
        if (readFrameHeaderIfRequired() == -1) return -1;
        
        //@TODO add logic for reading from several frames if required
        int remaining = remaining();
        if (length > remaining) {
            length = remaining;
        }
        
        byteBuffer.get(b, offset, length);
        frameBytesRead += length;
        receivedMessageLength += length;
        
        return length;
    }
    
    
    public void forceHeaderRead() throws IOException {
        readHeader();
    }
    
    private int readFrameHeaderIfRequired() {
        if (!isDirectMode && !isLastFrame && !isReadingHeader && (frameBytesRead == 0 || frameBytesRead == currentFrameDataSize)) {
            try {
                readHeader();
            } catch (IOException ex) {
                return -1;
            }
        }
        
        return 0;
    }
    
    private void readHeader() throws IOException {
        logger.log(Level.FINEST, "FramedMessageInputStream.readHeader");
        frameBytesRead = 0;
        isReadingHeader = true;
        DataInOutUtils.readInts4(this, headerTmpArray, 2);
        channelId = headerTmpArray[0];
        messageId = headerTmpArray[1];
        if (FrameType.isFrameContainsParams(messageId)) {  //message types have description
            readHeaderContentDescription();
        }
        
        currentFrameDataSize = DataInOutUtils.readInt8(this);
        isLastFrame = FrameType.isLastFrame(messageId);
        currentFrameDataSize += frameBytesRead;
        isReadingHeader = false;
        if (logger.isLoggable(Level.FINEST)) {
            logger.log(Level.FINEST, "FramedMessageInputStream.readHeader done. " +
                    "channelId: {0}\nmessageId: {1}\ncontent-id: {2}\n" +
                    "content-params: {3}\ncurrentFrameDataSize: {4}\nisLastFrame: {5}",
                    new Object[] {channelId, messageId, contentId, contentParameters, currentFrameDataSize, isLastFrame});
        }
    }
    
    private void readHeaderContentDescription() throws IOException {
        DataInOutUtils.readInts4(this, headerTmpArray, 2);
        contentId = headerTmpArray[0];
        int paramNumber = headerTmpArray[1];
        for(int i=0; i<paramNumber; i++) {
            int paramId = DataInOutUtils.readInt4(this);
            String paramValue = dis.readUTF();
            contentParameters.put(paramId, paramValue);
        }
    }
    
    private int readFromChannel() {
        int eof = 0;
        for (int i=0; i < readTry; i++) {
            eof = doRead();
            
            if (eof != 0) {
                break;
            }
        }
        
        return eof;
    }
    
    public void skipToEndOfMessage() throws EOFException {
        do {
            readFrameHeaderIfRequired();
            skipToEndOfFrame();
            frameBytesRead = 0;
        } while(!isLastFrame);
    }
    
    private void skipToEndOfFrame() throws EOFException {
        if (currentFrameDataSize > 0) {
            if (byteBuffer.hasRemaining()) {
                int remainFrameBytes = currentFrameDataSize - frameBytesRead;
                if (remainFrameBytes <= byteBuffer.remaining()) {
                    byteBuffer.position(byteBuffer.position() + remainFrameBytes);
                    return;
                }
                
                frameBytesRead += byteBuffer.remaining();
                byteBuffer.position(byteBuffer.limit());
            }
            
            while(frameBytesRead < currentFrameDataSize) {
                int eof = readFromChannel();
                if (eof == -1) {
                    throw new EOFException("Unexpected eof. isLastFrame: " + isLastFrame + " frameBytesRead: " + frameBytesRead + " frameSize: " + frameSize + " currentFrameDataSize: " + currentFrameDataSize);
                }
                frameBytesRead += eof;
                byteBuffer.position(byteBuffer.position() + eof);
            }
            
            // if extra frame bytes were read - move position backwards
            byteBuffer.position(byteBuffer.position() - (frameBytesRead - currentFrameDataSize));
        }
    }
    
    /**
     * Read bytes using the read <code>ReadSelector</code>
     */
    private int doRead(){
        if ( socketChannel == null ) return -1;
        if (isEOF()) {
            return -1;
        }
        
        if (!byteBuffer.hasRemaining() && byteBuffer.position() >= bufferClearLimit) {
            byteBuffer.clear();
        }
        
        int bbPosition = byteBuffer.position();
        byteBuffer.limit(byteBuffer.capacity());
        
        int count;
        int byteRead = 0;
        Selector readSelector = null;
        SelectionKey tmpKey = null;
        
        try{
            do {
                count = socketChannel.read(byteBuffer);
                byteRead += count;
            } while (count > 0);
            
            if (count == -1 && byteRead >= 0) byteRead++;
            
            if ( byteRead == 0 ){
                readSelector = SelectorFactory.getSelector();
                
                if ( readSelector == null ){
                    return 0;
                }
                tmpKey = socketChannel
                        .register(readSelector,SelectionKey.OP_READ);
                tmpKey.interestOps(tmpKey.interestOps() | SelectionKey.OP_READ);
                int code = readSelector.select(readTimeout);
                
                //Nothing so return.
                tmpKey.interestOps(tmpKey.interestOps() & (~SelectionKey.OP_READ));
                if ( code == 0 ){
                    return 0;
                }
                
                do {
                    count = socketChannel.read(byteBuffer);
                    byteRead += count;
                } while (count > 0);
                if (count == -1 && byteRead >= 0) byteRead++;
            }
        } catch (Throwable t){
            logger.log(Level.SEVERE, t.getMessage(), t);
            return -1;
        } finally {
            if (tmpKey != null)
                tmpKey.cancel();
            
            if ( readSelector != null){
                try{
                    readSelector.selectNow();
                } catch (IOException ex){
                }
                SelectorFactory.returnSelector(readSelector);
            }
            
            byteBuffer.flip();
            byteBuffer.position(bbPosition);
        }
        
        return byteRead;
    }
    
    public boolean isMessageInProcess() {
        if (currentFrameDataSize == 0 || isEOF()) return false;
        
        return true;
    }
    
    private boolean isEOF() {
        return isLastFrame && frameBytesRead >= currentFrameDataSize - 1;
    }
    
    private int remaining() {
        if (isReadingHeader || isDirectMode) {
            return byteBuffer.remaining();
        }
        
        return Math.min(currentFrameDataSize - frameBytesRead, byteBuffer.remaining());
    }
    
    public void reset() {
        frameBytesRead = 0;
        currentFrameDataSize = 0;
        isLastFrame = false;
        isReadingHeader = false;
        contentId = -1;
        messageId = -1;
        contentParameters.clear();
        receivedMessageLength = 0;
    }
    
    public void activate() {
    }
    
    public void passivate() {
        reset();
        setSocketChannel(null);
        setByteBuffer(null);
    }
    
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("ByteBuffer: ");
        buffer.append(byteBuffer);
        buffer.append(" FrameBytesRead: ");
        buffer.append(frameBytesRead);
        buffer.append(" CurrentFrameDataSize: ");
        buffer.append(currentFrameDataSize);
        buffer.append(" isLastFrame: ");
        buffer.append(isLastFrame);
        buffer.append(" isDirectMode: ");
        buffer.append(isDirectMode);
        buffer.append(" isReadingHeader: ");
        buffer.append(isReadingHeader);
        
        return buffer.toString();
    }
}

