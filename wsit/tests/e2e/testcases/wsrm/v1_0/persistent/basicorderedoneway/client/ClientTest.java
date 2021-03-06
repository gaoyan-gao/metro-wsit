/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package wsrm.v1_0.persistent.basicorderedoneway.client;

import com.sun.xml.ws.rx.testing.PacketFilteringFeature;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.TestCase;
import wsrm.v1_0.persistent.basicorderedoneway.common.EvenMessageDelayingFilter;

/**
 *
 * @author Marek Potociar <marek.potociar at sun.com>
 */
public class ClientTest extends TestCase {

    private static final Logger LOGGER = Logger.getLogger(ClientTest.class.getName());
    private static final int NUMBER_OF_THREADS = 5;

    public void testAckRequestedInterval() {
        IPing port = null;
        final CountDownLatch latch = new CountDownLatch(NUMBER_OF_THREADS);
        try {
            PingService service = new PingService();
            port = service.getPingPort(new PacketFilteringFeature(
                    EvenMessageDelayingFilter.class //  2
                    //                    EvenMessageDelayingFilter.class, //  4
                    //                    EvenMessageDelayingFilter.class, //  6
                    //                    EvenMessageDelayingFilter.class, //  8
                    //                    EvenMessageDelayingFilter.class, // 10
                    //                    EvenMessageDelayingFilter.class  // 12
                    ));

            final IPing portCopy = port;
            Executor executor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);


            for (final AtomicInteger i = new AtomicInteger(1); i.get() <= NUMBER_OF_THREADS; i.incrementAndGet()) {
                executor.execute(new Runnable() {

                    int id = i.get();

                    public void run() {
                        try {
                            LOGGER.info(String.format("Calling web service in runnable [ %d ]", id));
                            portCopy.ping("ping-" + id);
                            LOGGER.info(String.format("Web service call finished in runnable [ %d ]", id));
                        } finally {
                            latch.countDown();
                            LOGGER.info(String.format("Decreasing latch count to %d", latch.getCount()));
                        }
                    }
                });
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    Logger.getLogger(ClientTest.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Test failed with an unexpected exception.", ex);
            fail(String.format("Test failed with the execption: %s", ex));
        } finally {
            if (port != null) {
                try {
                    LOGGER.info(String.format("Still need to wait for %d threads", latch.getCount()));
                    latch.await(600, TimeUnit.SECONDS);
                } catch (InterruptedException ex) {
                    Logger.getLogger(ClientTest.class.getName()).log(Level.SEVERE, null, ex);                    
                    try {
                        ((java.io.Closeable) port).close();
                    } catch (IOException ioex) {
                        Logger.getLogger(ClientTest.class.getName()).log(Level.SEVERE, null, ioex);
                    }
                    fail("The test did not finished in 60 seconds. Most likely it is stuck in a deadlock or on sending poisoned messages");
                }
                try {
                    ((java.io.Closeable) port).close();
                } catch (IOException ex) {
                    Logger.getLogger(ClientTest.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
}
