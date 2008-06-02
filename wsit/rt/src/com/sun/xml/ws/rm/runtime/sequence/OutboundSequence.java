/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package com.sun.xml.ws.rm.runtime.sequence;

import com.sun.xml.ws.rm.localization.RmLogger;
import com.sun.xml.ws.rm.runtime.sequence.Sequence.AckRange;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * TODO javadoc
 * TODO make class thread-safe
 * @author Marek Potociar (marek.potociar at sun.com)
 */
public class OutboundSequence extends AbstractSequence {

    private static final RmLogger LOGGER = RmLogger.getLogger(OutboundSequence.class);

    public OutboundSequence(SequenceData data) {
        super(data);
    }

    @Override
    public long getNextMessageId() throws MessageNumberRolloverException {
        try {
            data.acquireMessageIdDataReadWriteLock();
            
            long nextId = data.incrementAndGetLastMessageId();
            if (nextId > Sequence.MAX_MESSAGE_ID) {
                throw LOGGER.logSevereException(new MessageNumberRolloverException(data.getSequenceId(), nextId));
            }

            data.addUnackedMessageId(nextId);
            return nextId;
        } finally {
            data.releaseMessageIdDataReadWriteLock();
        }
    }

    public long getLastMessageId() {
        return data.getLastMessageId();
    }

    public void acknowledgeMessageId(long messageId) {
        // NOTE: This method will most likely not be used in our implementation as we expect range-based 
        //       acknowledgements on outbound sequence. Thus we are not trying to optimize the implementation
        data.removeUnackedMessageId(messageId);
    }

    public void acknowledgeMessageIds(List<AckRange> ranges) throws IllegalMessageIdentifierException {
        try {
            data.acquireMessageIdDataReadWriteLock();
            
            if (ranges == null || ranges.isEmpty() || data.noUnackedMessageIds()) {
                return;
            }

            if (ranges.size() > 1) {
                Collections.sort(ranges, new Comparator<AckRange>() {

                    public int compare(AckRange range1, AckRange range2) {
                        if (range1.lower <= range2.lower) {
                            return -1;
                        } else {
                            return 1;
                        }
                    }
                });
            }
            Iterator<Long> unackedIterator = data.getAllUnackedIndexes().iterator();
            Iterator<AckRange> rangeIterator = ranges.iterator();
            AckRange currentRange = rangeIterator.next();
            while (unackedIterator.hasNext()) {
                long unackedIndex = unackedIterator.next();
                if (unackedIndex >= currentRange.lower && unackedIndex <= currentRange.upper) {
                    unackedIterator.remove();
                } else if (rangeIterator.hasNext()) {
                    currentRange = rangeIterator.next();
                } else {
                    break; // no more acked ranges

                }
            }
        } finally {
            data.releaseMessageIdDataReadWriteLock();
        }
    }
}
