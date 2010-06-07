
package wstrust.scenario2n.sts;

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
@WebServiceClient(name = "SecurityTokenService", targetNamespace = "http://tempuri.org/", wsdlLocation = "file:/Users/m_potociar/sandbox/java.net/metro-1_4-branch/test/e2e/testcases/wstrust/scenario2n/sts/sts.wsdl")
public class SecurityTokenService
    extends Service
{

    private final static URL SECURITYTOKENSERVICE_WSDL_LOCATION;
    private final static Logger logger = Logger.getLogger(wstrust.scenario2n.sts.SecurityTokenService.class.getName());

    static {
        URL url = null;
        try {
            URL baseUrl;
            baseUrl = wstrust.scenario2n.sts.SecurityTokenService.class.getResource(".");
            url = new URL(baseUrl, "file:/Users/m_potociar/sandbox/java.net/metro-1_4-branch/test/e2e/testcases/wstrust/scenario2n/sts/sts.wsdl");
        } catch (MalformedURLException e) {
            logger.warning("Failed to create URL for the wsdl Location: 'file:/Users/m_potociar/sandbox/java.net/metro-1_4-branch/test/e2e/testcases/wstrust/scenario2n/sts/sts.wsdl', retrying as a local file");
            logger.warning(e.getMessage());
        }
        SECURITYTOKENSERVICE_WSDL_LOCATION = url;
    }

    public SecurityTokenService(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public SecurityTokenService() {
        super(SECURITYTOKENSERVICE_WSDL_LOCATION, new QName("http://tempuri.org/", "SecurityTokenService"));
    }

    /**
     * 
     * @return
     *     returns ISecurityTokenService
     */
    @WebEndpoint(name = "CustomBinding_ISecurityTokenService")
    public ISecurityTokenService getCustomBindingISecurityTokenService() {
        return super.getPort(new QName("http://tempuri.org/", "CustomBinding_ISecurityTokenService"), ISecurityTokenService.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns ISecurityTokenService
     */
    @WebEndpoint(name = "CustomBinding_ISecurityTokenService")
    public ISecurityTokenService getCustomBindingISecurityTokenService(WebServiceFeature... features) {
        return super.getPort(new QName("http://tempuri.org/", "CustomBinding_ISecurityTokenService"), ISecurityTokenService.class, features);
    }

}
