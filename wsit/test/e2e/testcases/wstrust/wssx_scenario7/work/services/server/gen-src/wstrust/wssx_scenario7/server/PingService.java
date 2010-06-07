
package wstrust.wssx_scenario7.server;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceFeature;


/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.1.8-hudson-4-
 * Generated source version: 2.1
 * 
 */
@WebServiceClient(name = "PingService", targetNamespace = "http://tempuri.org/", wsdlLocation = "file:/Users/m_potociar/sandbox/java.net/metro-1_4-branch/test/e2e/testcases/wstrust/wssx_scenario7/server/PingService.wsdl")
public class PingService
    extends Service
{

    private final static URL PINGSERVICE_WSDL_LOCATION;
    private final static Logger logger = Logger.getLogger(wstrust.wssx_scenario7.server.PingService.class.getName());

    static {
        URL url = null;
        try {
            URL baseUrl;
            baseUrl = wstrust.wssx_scenario7.server.PingService.class.getResource(".");
            url = new URL(baseUrl, "file:/Users/m_potociar/sandbox/java.net/metro-1_4-branch/test/e2e/testcases/wstrust/wssx_scenario7/server/PingService.wsdl");
        } catch (MalformedURLException e) {
            logger.warning("Failed to create URL for the wsdl Location: 'file:/Users/m_potociar/sandbox/java.net/metro-1_4-branch/test/e2e/testcases/wstrust/wssx_scenario7/server/PingService.wsdl', retrying as a local file");
            logger.warning(e.getMessage());
        }
        PINGSERVICE_WSDL_LOCATION = url;
    }

    public PingService(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public PingService() {
        super(PINGSERVICE_WSDL_LOCATION, new QName("http://tempuri.org/", "PingService"));
    }

    /**
     * 
     * @return
     *     returns IPingServiceContract
     */
    @WebEndpoint(name = "CustomBinding_IPingServiceContract2")
    public IPingServiceContract getCustomBindingIPingServiceContract2() {
        return super.getPort(new QName("http://tempuri.org/", "CustomBinding_IPingServiceContract2"), IPingServiceContract.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns IPingServiceContract
     */
    @WebEndpoint(name = "CustomBinding_IPingServiceContract2")
    public IPingServiceContract getCustomBindingIPingServiceContract2(WebServiceFeature... features) {
        return super.getPort(new QName("http://tempuri.org/", "CustomBinding_IPingServiceContract2"), IPingServiceContract.class, features);
    }

}
