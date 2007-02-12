/*
 * Copyright (c) 2005 Sun Microsystems, Inc.
 * All rights reserved.
 */
package simple.client;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.Holder;

import simple.client.SymmetricFederatedService;
import simple.client.IPingService;
import simple.schema.client.Ping;

public class PingServiceClient {
    
    public static void main (String[] args) {
            
            SymmetricFederatedService service = new SymmetricFederatedService();
            IPingService stub = service.getScenario6IssuedTokenForCertificateSecureConversationMutualCertificate11(); 
                    
            // use static stubs to override endpoint property of WSDL       
            String serviceURL = System.getProperty("service.url");

            System.out.println("Service URL=" + serviceURL);
      
            ((BindingProvider)stub).getRequestContext().
                put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, serviceURL); 

            stub.ping(new Holder("1"), new Holder("sun"), new Holder("Passed!"));
            
    }
    
}
