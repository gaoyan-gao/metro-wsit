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
 * $Id: Advice.java,v 1.2 2010-10-21 15:38:00 snajper Exp $
 */

package com.sun.xml.wss.saml.assertion.saml11.jaxb20;

import com.sun.xml.wss.saml.SAMLException;

import com.sun.xml.wss.logging.LogDomainConstants;
import com.sun.xml.wss.saml.internal.saml11.jaxb20.AdviceType;
import com.sun.xml.wss.saml.util.SAMLJAXBUtil;


//import com.sun.xml.bind.util.ListImpl;

import javax.xml.bind.JAXBContext;
import org.w3c.dom.Element;

import java.util.List;
import java.util.logging.Logger;


/**
 *The <code>Advice</code> element contains additional information that the issuer wishes to
 *provide. This information MAY be ignored by applications without affecting
 *either the semantics or validity. Advice elements MAY be specified in
 *an extension schema.
 */
public class Advice  extends com.sun.xml.wss.saml.internal.saml11.jaxb20.AdviceType implements com.sun.xml.wss.saml.Advice {
    protected static final Logger log = Logger.getLogger(
            LogDomainConstants.WSS_API_DOMAIN,
            LogDomainConstants.WSS_API_DOMAIN_BUNDLE);
   
    @SuppressWarnings("unchecked")
    public static AdviceType fromElement(Element element) throws SAMLException {
        try {
            JAXBContext jc = SAMLJAXBUtil.getJAXBContext();
                    
            javax.xml.bind.Unmarshaller u = jc.createUnmarshaller();
            return (AdviceType)u.unmarshal(element);
        } catch ( Exception ex) {
            throw new SAMLException(ex.getMessage());
        }
    }

    
    private void setAssertionIDReferenceOrAssertionOrAny(
            List<Object> assertionIDReferenceOrAssertionOrAny) {
        this.assertionIDReferenceOrAssertionOrAny = assertionIDReferenceOrAssertionOrAny;
    }
    
    /**
     * Constructor
     *
     * @param assertionidreference A List of <code>AssertionIDReference</code>.
     * @param assertion A List of Assertion
     * @param otherelement A List of any element defined as
     *        <code>&lt;any namespace="##other" processContents="lax"&gt;</code>;
     */
    @SuppressWarnings("unchecked")
    public Advice(List assertionidreference, List assertion, List otherelement) {
        if ( null != assertionidreference ) {
            setAssertionIDReferenceOrAssertionOrAny(assertionidreference);
        } else if ( null != assertion) {
            setAssertionIDReferenceOrAssertionOrAny(assertion);
        } else if ( null != otherelement) {
            setAssertionIDReferenceOrAssertionOrAny(otherelement);
        }
    }
    
    public Advice(AdviceType adviceType) {
        if(adviceType != null){
            setAssertionIDReferenceOrAssertionOrAny(adviceType.getAssertionIDReferenceOrAssertionOrAny());
        }
    }
    
    public List<Object> getAdvice(){
        return super.getAssertionIDReferenceOrAssertionOrAny();
    }
}
