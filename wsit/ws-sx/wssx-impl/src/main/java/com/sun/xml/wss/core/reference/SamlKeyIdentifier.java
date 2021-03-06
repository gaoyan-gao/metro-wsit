/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
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

/*
 * $Id: SamlKeyIdentifier.java,v 1.2 2010-10-21 15:37:14 snajper Exp $
 */

package com.sun.xml.wss.core.reference;

import javax.xml.soap.SOAPElement;

import org.w3c.dom.Document;

import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.XWSSecurityException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 */
public class SamlKeyIdentifier extends KeyIdentifier {

    /** Defaults */
    private String valueType = MessageConstants.WSSE_SAML_KEY_IDENTIFIER_VALUE_TYPE;
        

    /**
     * Creates an "empty" KeyIdentifier element with default encoding type
     * and default value type.
     */
    public SamlKeyIdentifier(Document doc) throws XWSSecurityException {
        super(doc);
        // Set default attributes
        String vType = valueType;
        NodeList nodeList = doc.getElementsByTagName(MessageConstants.SAML_ASSERTION_LNAME);
        if(nodeList.getLength() > 0){
              Node assertion = (Node)nodeList.item(0);
              if (assertion.getNamespaceURI() == MessageConstants.SAML_v2_0_NS) {
                  vType = MessageConstants.WSSE_SAML_v2_0_KEY_IDENTIFIER_VALUE_TYPE;    
              }              
              
        }
        // old behavior left for BackwardCompatibility reasons
        setAttribute("ValueType", vType);
    }

    public SamlKeyIdentifier(SOAPElement element) throws XWSSecurityException {
        super(element);
    }

} 
