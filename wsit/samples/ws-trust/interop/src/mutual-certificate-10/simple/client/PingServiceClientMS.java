/*
 * Copyright (c) 2005 Sun Microsystems, Inc.
 * All rights reserved.
 */
package simple.client;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.Holder;

import simple.client.SymmetricFederatedService;
import simple.client.IPingService;
import org.xmlsoap.ping.Ping;

public class PingServiceClientMS {
    
    public static void main (String[] args) {
            
            SymmetricFederatedService service = new SymmetricFederatedService();
            IPingService stub = service.getScenario2IssuedTokenMutualCertificate10(); 
                    
            // use static stubs to override endpoint property of WSDL       
            String serviceURL = System.getProperty("service.url");

            System.out.println("Service URL=" + serviceURL);
      
            ((BindingProvider)stub).getRequestContext().
                put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, serviceURL); 

            stub.ping(new Holder("1"), new Holder("sun"), new Holder("Passed!"));
            
    }
    
}
