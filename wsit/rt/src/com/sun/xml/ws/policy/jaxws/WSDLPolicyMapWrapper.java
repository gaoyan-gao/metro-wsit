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

package com.sun.xml.ws.policy.jaxws;

import com.sun.xml.ws.policy.AssertionValidationProcessor;
import com.sun.xml.ws.policy.PolicyAssertion;
import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.ws.WebServiceException;
import com.sun.xml.ws.api.model.wsdl.WSDLExtension;
import com.sun.xml.ws.api.model.wsdl.WSDLModel;
import com.sun.xml.ws.policy.AssertionSet;
import com.sun.xml.ws.policy.PolicyMapExtender;
import com.sun.xml.ws.policy.PolicyMapKey;
import com.sun.xml.ws.policy.PolicySubject;
import com.sun.xml.ws.policy.EffectiveAlternativeSelector;
import com.sun.xml.ws.policy.EffectivePolicyModifier;
import com.sun.xml.ws.policy.Policy;
import com.sun.xml.ws.policy.PolicyException;
import com.sun.xml.ws.policy.PolicyMap;
import com.sun.xml.ws.policy.jaxws.privateutil.LocalizationMessages;
import com.sun.xml.ws.policy.privateutil.PolicyLogger;
import com.sun.xml.ws.policy.jaxws.spi.ModelConfiguratorProvider;
import com.sun.xml.ws.policy.privateutil.PolicyUtils;
import com.sun.xml.ws.policy.spi.PolicyAssertionValidator;

import static com.sun.xml.ws.policy.privateutil.PolicyUtils.Commons.logException;
/**
 * TODO: write doc
 *
 * @author Jakub Podlesak (jakub.podlesak at sun.com)
 */
public class WSDLPolicyMapWrapper implements WSDLExtension {
    private static final PolicyLogger LOGGER = PolicyLogger.getLogger(WSDLPolicyMapWrapper.class);
    private static final QName NAME = new QName(null, "WSDLPolicyMapWrapper");
    
    private static ModelConfiguratorProvider[] configurators = null;
    
    private PolicyMap policyMap;
    private EffectivePolicyModifier mapModifier;
    private PolicyMapExtender mapExtender;
    
    private static ModelConfiguratorProvider[] getModelConfiguratorProviders() {
        if (configurators == null) {
            configurators = PolicyUtils.ServiceProvider.load(ModelConfiguratorProvider.class);
        }
        return configurators;
    }
    
    protected WSDLPolicyMapWrapper(PolicyMap policyMap) {
        if (policyMap == null) {
            throw new NullPointerException(LocalizationMessages.WSP_1016_POLICY_MAP_CAN_NOT_BE_NULL());
        }
        
        this.policyMap = policyMap;
    }
    
    public WSDLPolicyMapWrapper(PolicyMap policyMap, EffectivePolicyModifier modifier, PolicyMapExtender extender) {
        this(policyMap);
        this.mapModifier = modifier;
        this.mapExtender = extender;
    }
    
    public PolicyMap getPolicyMap() {
        return policyMap;
    }
    
    void addClientConfigToMap(final Object clientWsitConfigId, final PolicyMap clientPolicyMap) throws PolicyException {
        LOGGER.entering("addClientConfigToMap");
        
        try {
            for (PolicyMapKey key : clientPolicyMap.getAllServiceScopeKeys()) {
                final Policy policy = clientPolicyMap.getServiceEffectivePolicy(key);
                // setting subject to provided URL of client WSIT config
                mapExtender.putServiceSubject(key, new PolicySubject(clientWsitConfigId, policy));
            }
            
            for (PolicyMapKey key : clientPolicyMap.getAllEndpointScopeKeys()) {
                final Policy policy = clientPolicyMap.getEndpointEffectivePolicy(key);
                // setting subject to provided URL of client WSIT config
                mapExtender.putEndpointSubject(key, new PolicySubject(clientWsitConfigId, policy));
            }
            
            for (PolicyMapKey key : clientPolicyMap.getAllOperationScopeKeys()) {
                final Policy policy = clientPolicyMap.getOperationEffectivePolicy(key);
                // setting subject to provided URL of client WSIT config
                mapExtender.putOperationSubject(key, new PolicySubject(clientWsitConfigId, policy));
            }
            
            for (PolicyMapKey key : clientPolicyMap.getAllInputMessageScopeKeys()) {
                final Policy policy = clientPolicyMap.getInputMessageEffectivePolicy(key);
                // setting subject to provided URL of client WSIT config
                mapExtender.putInputMessageSubject(key, new PolicySubject(clientWsitConfigId, policy));
            }
            
            for (PolicyMapKey key : clientPolicyMap.getAllOutputMessageScopeKeys()) {
                final Policy policy = clientPolicyMap.getOutputMessageEffectivePolicy(key);
                // setting subject to provided URL of client WSIT config
                mapExtender.putOutputMessageSubject(key, new PolicySubject(clientWsitConfigId, policy));
            }
            
            for (PolicyMapKey key : clientPolicyMap.getAllFaultMessageScopeKeys()) {
                final Policy policy = clientPolicyMap.getFaultMessageEffectivePolicy(key);
                // setting subject to provided URL of client WSIT config
                mapExtender.putFaultMessageSubject(key, new PolicySubject(clientWsitConfigId, policy));
            }
            LOGGER.fine("addClientToServerMap", LocalizationMessages.WSP_1041_CLIENT_CFG_POLICIES_TRANSFERED_INTO_FINAL_POLICY_MAP(policyMap));
        } catch (FactoryConfigurationError ex) {
            throw logException(new PolicyException(ex), LOGGER);
        }
        
        LOGGER.exiting("addClientToServerMap");
    }
    
    public void doAlternativeSelection() throws PolicyException {
        EffectiveAlternativeSelector.doSelection(mapModifier);
    }
    
    void validateServerSidePolicies() throws PolicyException {
        AssertionValidationProcessor validationProcessor = AssertionValidationProcessor.getInstance();
        for (Policy policy : policyMap) {
            
            // TODO:  here is a good place to check if the actual policy has only one alternative...
            
            for (AssertionSet assertionSet : policy) {
                for (PolicyAssertion assertion : assertionSet) {
                    PolicyAssertionValidator.Fitness validationResult = validationProcessor.validateServerSide(assertion);
                    if (validationResult != PolicyAssertionValidator.Fitness.SUPPORTED) {
                        throw logException(new PolicyException(LocalizationMessages.WSP_1046_SERVER_SIDE_ASSERTION_VALIDATION_FAILED(
                                assertion.getName(),
                                validationResult)), LOGGER);
                    }
                }
            }
        }
    }
    
    void configureModel(final WSDLModel model) {
        try {
            for (ModelConfiguratorProvider configurator : getModelConfiguratorProviders()) {
                configurator.configure(model, policyMap);
            }
        } catch (PolicyException e) {
            throw logException(new WebServiceException(LocalizationMessages.WSP_1032_FAILED_CONFIGURE_WSDL_MODEL(), e), LOGGER);
        }
    }
    
    void putEndpointSubject(final PolicyMapKey key, final PolicySubject subject) {
        if (null != this.mapExtender) {
            this.mapExtender.putEndpointSubject(key, subject);
        }
    }
    
    void putServiceSubject(final PolicyMapKey key, final PolicySubject subject) {
        if (null != this.mapExtender) {
            this.mapExtender.putServiceSubject(key, subject);
        }
    }
    
    void putOperationSubject(final PolicyMapKey key, final PolicySubject subject) {
        if (null != this.mapExtender) {
            this.mapExtender.putOperationSubject(key, subject);
        }
    }
    
    void putInputMessageSubject(final PolicyMapKey key, final PolicySubject subject) {
        if (null != this.mapExtender) {
            this.mapExtender.putInputMessageSubject(key, subject);
        }
    }
    
    void putOutputMessageSubject(final PolicyMapKey key, final PolicySubject subject) {
        if (null != this.mapExtender) {
            this.mapExtender.putOutputMessageSubject(key, subject);
        }
    }
    
    void putFaultMessageSubject(final PolicyMapKey key, final PolicySubject subject) {
        if (null != this.mapExtender) {
            this.mapExtender.putFaultMessageSubject(key, subject);
        }
    }
    
    public QName getName() {
        return NAME;
    }
}
