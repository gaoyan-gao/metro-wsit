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

package com.sun.xml.ws.policy.sourcemodel;

import com.sun.xml.ws.policy.PolicyConstants;
import com.sun.xml.ws.policy.PolicyException;
import com.sun.xml.ws.policy.privateutil.LocalizationMessages;
import com.sun.xml.ws.policy.privateutil.PolicyLogger;
import com.sun.xml.ws.policy.privateutil.PolicyUtils;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import javax.xml.namespace.QName;

/**
 * This class is a root of unmarshaled policy source structure. Each instance of the class contains factory method
 * to create new {@link com.sun.xml.ws.policy.sourcemodel.ModelNode} instances associated with the actual model instance.
 *
 * @author Marek Potociar
 */
public final class PolicySourceModel implements Cloneable {
    private static final PolicyLogger LOGGER = PolicyLogger.getLogger(PolicySourceModel.class);
    
    // TODO: move responisbility for default namespacing to the domain SPI implementation
    private static final Map<String, String> defaultNamespaceToPrefixMap = new HashMap<String, String>();
    static {
        defaultNamespaceToPrefixMap.put(PolicyConstants.POLICY_NAMESPACE_URI, PolicyConstants.POLICY_NAMESPACE_PREFIX);
        defaultNamespaceToPrefixMap.put(PolicyConstants.SUN_POLICY_NAMESPACE_URI, PolicyConstants.SUN_POLICY_NAMESPACE_PREFIX);
        
//        defaultNamespaceToPrefixMap.put(com.sun.xml.ws.encoding.policy.EncodingConstants.OPTIMIZED_MIME_NS, "");
//        defaultNamespaceToPrefixMap.put(com.sun.xml.ws.encoding.policy.EncodingConstants.ENCODING_NS, "");
        defaultNamespaceToPrefixMap.put(com.sun.xml.ws.encoding.policy.EncodingConstants.SUN_ENCODING_CLIENT_NS, "cenc");
        defaultNamespaceToPrefixMap.put(com.sun.xml.ws.encoding.policy.EncodingConstants.SUN_FI_SERVICE_NS, "fi");
        
        defaultNamespaceToPrefixMap.put(com.sun.xml.ws.security.impl.policy.Constants.TRUST_NS, "wst");
        defaultNamespaceToPrefixMap.put(com.sun.xml.ws.security.impl.policy.Constants.SECURITY_POLICY_NS, "sp");
        defaultNamespaceToPrefixMap.put(com.sun.xml.ws.security.impl.policy.Constants.UTILITY_NS, PolicyConstants.WSU_NAMESPACE_PREFIX);
        defaultNamespaceToPrefixMap.put(com.sun.xml.ws.security.impl.policy.Constants.SUN_WSS_SECURITY_CLIENT_POLICY_NS, "csp");
        defaultNamespaceToPrefixMap.put(com.sun.xml.ws.security.impl.policy.Constants.SUN_WSS_SECURITY_SERVER_POLICY_NS, "ssp");
        defaultNamespaceToPrefixMap.put(com.sun.xml.ws.security.impl.policy.Constants.SUN_TRUST_CLIENT_SECURITY_POLICY_NS, "ctp");
        defaultNamespaceToPrefixMap.put(com.sun.xml.ws.security.impl.policy.Constants.SUN_TRUST_SERVER_SECURITY_POLICY_NS, "stp");
        defaultNamespaceToPrefixMap.put(com.sun.xml.ws.security.impl.policy.Constants.SUN_SECURE_CLIENT_CONVERSATION_POLICY_NS, "cscp");
        defaultNamespaceToPrefixMap.put(com.sun.xml.ws.security.impl.policy.Constants.SUN_SECURE_SERVER_CONVERSATION_POLICY_NS, "sscp");

        defaultNamespaceToPrefixMap.put(com.sun.xml.ws.rm.Constants.version, "wsrmp");
        defaultNamespaceToPrefixMap.put(com.sun.xml.ws.rm.Constants.microsoftVersion, "msrmp");
        defaultNamespaceToPrefixMap.put(com.sun.xml.ws.rm.Constants.sunVersion, "sunrmp");
        defaultNamespaceToPrefixMap.put(com.sun.xml.ws.rm.Constants.sunClientVersion, "sunrmcp");

        defaultNamespaceToPrefixMap.put(com.sun.xml.ws.transport.tcp.wsit.TCPConstants.TCPTRANSPORT_POLICY_NAMESPACE_URI, "soaptcpsvc");
        defaultNamespaceToPrefixMap.put(com.sun.xml.ws.transport.tcp.wsit.TCPConstants.CLIENT_TRANSPORT_NS, "transport");
        defaultNamespaceToPrefixMap.put(com.sun.xml.ws.transport.tcp.wsit.TCPConstants.TCPTRANSPORT_CONNECTION_MANAGEMENT_NAMESPACE_URI, "soaptcp");

//        defaultNamespaceToPrefixMap.put(com.sun.xml.ws.api.addressing.AddressingVersion.MEMBER.policyNsUri, "");
//        defaultNamespaceToPrefixMap.put(com.sun.xml.ws.api.addressing.AddressingVersion.W3C.policyNsUri, "");

//        defaultNamespaceToPrefixMap.put(com.sun.xml.ws.tx.common.Constants.WSAT_SOAP_NSURI, "");
    }
    
    private ModelNode rootNode;
    private String policyId;
    private String policyName;
    
    private List<ModelNode> references = new LinkedList<ModelNode>(); // links to policy reference nodes
    private boolean expanded = false;
    
    /**
     * Factory method that creates new policy source model instance.
     *
     * @return newly created policy source model instance
     */
    public static PolicySourceModel createPolicySourceModel() {
        return new PolicySourceModel();
    }
    
    /**
     * Factory method that creates new policy source model instance and initializes it according to parameters provided.
     *
     * @param policyId local policy identifier - relative URI. May be {@code null}.
     * @param policyName global policy identifier - absolute policy expression URI. May be {@code null}.
     * @return newly created policy source model instance with its name and id properly set
     */
    public static PolicySourceModel createPolicySourceModel(final String policyId, final String policyName) {
        return new PolicySourceModel(policyId, policyName);
    }
    
    /**
     * Private constructor that creats new policy source model instance without any
     * id or name identifier. The namespace-to-prefix map is initialized with mapping
     * of policy namespace to the default value set by
     * {@link PolicyConstants#POLICY_NAMESPACE_PREFIX POLICY_NAMESPACE_PREFIX constant}
     */
    private PolicySourceModel() {
        this.rootNode = ModelNode.createRootPolicyNode(this);
    }
    
    /**
     * Private constructor that creats new policy source model instance with given
     * id or name identifier.
     *
     * @param policyId relative policy reference within an XML document. May be {@code null}.
     * @param policyName absloute IRI of policy expression. May be {@code null}.
     */
    private PolicySourceModel(String policyId, String policyName) {
        this();
        this.policyId = policyId;
        this.policyName = policyName;
    }
    
    /**
     * Returns a root node of this policy source model. It is allways of POLICY type.
     *
     * @return root policy source model node - allways of POLICY type.
     */
    public ModelNode getRootNode() {
        return rootNode;
    }
    
    /**
     * Returns a policy name of this policy source model.
     *
     * @return policy name.
     */
    public String getPolicyName() {
        return policyName;
    }
    
    /**
     * Returns a policy ID of this policy source model.
     *
     * @return policy ID.
     */
    public String getPolicyId() {
        return policyId;
    }
    
    /**
     * Provides information about how namespaces used in this {@link PolicySourceModel}
     * instance should be mapped to thier default prefixes when marshalled.
     *
     * @return immutable map that holds information about namespaces used in the
     *         model and their mapping to prefixes that should be used when marshalling
     *         this model.
     */
    Map<String, String> getNamespaceToPrefixMapping() {
        Map<String, String> nsToPrefixMap = new HashMap<String, String>();

        Collection<String> namespaces = getUsedNamespaces();
        for (String namespace : namespaces) {
            String prefix = getDefaultPrefix(namespace);
            if (prefix != null) {
                nsToPrefixMap.put(namespace, prefix);
            }
        }        
        
        return nsToPrefixMap;
    }
    
    /**
     * An {@code Object.equals(Object obj)} method override.
     * <p/>
     * When child nodes are tested for equality, the parent policy source model is not considered. Thus two different
     * policy source models instances may be equal based on their node content.
     */
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        
        if (!(obj instanceof PolicySourceModel))
            return false;
        
        boolean result = true;
        final PolicySourceModel that = (PolicySourceModel) obj;
        
        result = result && ((this.policyId == null) ? that.policyId == null : this.policyId.equals(that.policyId));
        result = result && ((this.policyName == null) ? that.policyName == null : this.policyName.equals(that.policyName));
        result = result && this.rootNode.equals(that.rootNode);
//        result = result && ((this.xxx == null) ? that.xxx == null : this.xxx.equals(that.xxx));
        
        return result;
    }
    
    /**
     * An {@code Object.hashCode()} method override.
     */
    public int hashCode() {
        int result = 17;
        
        result = 37 * result + ((this.policyId != null) ? this.policyId.hashCode() : 0);
        result = 37 * result + ((this.policyName != null) ? this.policyName.hashCode() : 0);
        result = 37 * result + this.rootNode.hashCode();
//        result = 37 * result + ((this.xxx != null) ? this.xxx.hashCode() : 0);
        
        return result;
    }
    
    /**
     * Returns a string representation of the object. In general, the <code>toString</code> method
     * returns a string that "textually represents" this object.
     *
     * @return  a string representation of the object.
     */
    public String toString() {
        final String innerIndent = PolicyUtils.Text.createIndent(1);
        final StringBuffer buffer = new StringBuffer("Policy source model {");
        
        buffer.append(PolicyUtils.Text.NEW_LINE);
        buffer.append(innerIndent).append("policy id = '").append(policyId).append('\'').append(PolicyUtils.Text.NEW_LINE);
        buffer.append(innerIndent).append("policy name = '").append(policyName).append('\'').append(PolicyUtils.Text.NEW_LINE);
        rootNode.toString(1, buffer).append(PolicyUtils.Text.NEW_LINE).append('}');
        
        return buffer.toString();
    }
    
    protected PolicySourceModel clone() throws CloneNotSupportedException {
        final PolicySourceModel clone = (PolicySourceModel) super.clone();
        
        clone.rootNode = this.rootNode.clone();
        try {
            clone.rootNode.setParentModel(this);
        } catch (IllegalAccessException e) {
            throw LOGGER.logSevereException(new CloneNotSupportedException(LocalizationMessages.WSP_0013_UNABLE_TO_SET_PARENT_MODEL_ON_ROOT()), e);
        }
        
        return clone;
    }
    
    /**
     * Returns a boolean value indicating whether this policy source model contains references to another policy source models.
     * <p/>
     * Every source model that references other policies must be expanded before it can be translated into a Policy objects. See
     * {@link #expand(PolicySourceModelContext)} and {@link #isExpanded()} for more details.
     *
     * @return {@code true} or {code false} depending on whether this policy source model contains references to another policy source models.
     */
    public boolean containsPolicyReferences() {
        return !references.isEmpty();
    }
    
    /**
     * Returns a boolean value indicating whether this policy source model contains is already expanded (i.e. contains no unexpanded
     * policy references) or not. This means that if model does not originally contain any policy references, it is considered as expanded,
     * thus this method returns {@code true} in such case. Also this method does not check whether the references policy source models are expanded
     * as well, so after expanding this model a value of {@code true} is returned even if referenced models are not expanded yet. Thus each model
     * can be considered to be fully expanded only if all policy source models stored in PolicySourceModelContext instance are expanded, provided the
     * PolicySourceModelContext instance contains full set of policy source models.
     * <p/>
     * Every source model that references other policies must be expanded before it can be translated into a Policy object. See
     * {@link #expand(PolicySourceModelContext)} and {@link #containsPolicyReferences()} for more details.
     *
     * @return {@code true} or {@code false} depending on whether this policy source model contains is expanded or not.
     */
    private boolean isExpanded() {
        return references.isEmpty() || expanded;
    }
    
    /**
     * Expands current policy model. This means, that if this model contains any (unexpanded) policy references, then the method expands those
     * references by placing the content of the referneced policy source models under the policy reference nodes. This operation merely creates
     * a link between this and referenced policy source models. Thus any change in the referenced models will be visible wihtin this model as well.
     * <p/>
     * Please, notice that the method does not check if the referenced models are already expanded nor does the method try to expand unexpanded
     * referenced models. This must be preformed manually within client's code. Consecutive calls of this method will have no effect.
     * <p/>
     * Every source model that references other policies must be expanded before it can be translated into a Policy object. See
     * {@link #isExpanded()} and {@link #containsPolicyReferences()} for more details.
     *
     * @param context a policy source model context holding the set of unmarshalled policy source models within the same context.
     */
    public synchronized void expand(final PolicySourceModelContext context) throws PolicyException {
        if (!isExpanded()) {
            for (ModelNode reference : references) {
                final PolicyReferenceData refData = reference.getPolicyReferenceData();
                final String digest = refData.getDigest();
                PolicySourceModel referencedModel;
                if (digest == null) {
                    referencedModel = context.retrieveModel(refData.getReferencedModelUri());
                } else {
                    referencedModel = context.retrieveModel(refData.getReferencedModelUri(), refData.getDigestAlgorithmUri(), digest);
                }
                
                reference.setReferencedModel(referencedModel);
            }
            expanded = true;
        }
    }
    
    /**
     * Adds new policy reference to the policy source model. The method is used by
     * the ModelNode instances of type POLICY_REFERENCE that need to register themselves
     * as policy references in the model.
     *
     * @param node policy reference model node to be registered as a policy reference
     *        in this model.
     */
    void addNewPolicyReference(final ModelNode node) {
        if (node.getType() != ModelNode.Type.POLICY_REFERENCE) {
            throw new IllegalArgumentException(LocalizationMessages.WSP_0042_POLICY_REFERENCE_NODE_EXPECTED_INSTEAD_OF(node.getType()));
        }
        
        references.add(node);
    }
    
    /**
     * Iterates through policy vocabulary and extracts set of namespaces used in
     * the policy expression.
     *
     * @param policy policy instance to check fro used namespaces
     * @return collection of used namespaces within given policy instance
     */
    private Collection<String> getUsedNamespaces() {
        Set<String> namespaces = new HashSet<String>();
        namespaces.add(PolicyConstants.POLICY_NAMESPACE_URI);
        
        if (this.policyId != null) {
            namespaces.add(PolicyConstants.WSU_NAMESPACE_URI);            
        }
        
        Queue<ModelNode> nodesToBeProcessed = new LinkedList<ModelNode>();
        nodesToBeProcessed.add(rootNode);
        
        ModelNode processedNode;
        while ((processedNode = nodesToBeProcessed.poll()) != null) {
            for (ModelNode child : processedNode.getContent()) {
                if (child.hasChildren()) {
                    nodesToBeProcessed.offer(child);
                }
                
                if (child.isAssertionRelatedNode()) {
                    AssertionData nodeData = child.getNodeData();
                    namespaces.add(nodeData.getName().getNamespaceURI());
                    if (nodeData.isPrivateAttributeSet()) {
                        namespaces.add(PolicyConstants.SUN_POLICY_NAMESPACE_URI);
                    }
                    
                    for (Entry<QName, String> attribute : nodeData.getAttributesSet()) {
                        namespaces.add(attribute.getKey().getNamespaceURI());
                    }                    
                }
            }
        }
        
        return namespaces;
    }
    
    /**
     * Method retrieves default prefix for given namespace. Method returns null if
     * no default prefix is defined..
     *
     * @param namespace to get default prefix for.
     * @return default prefix for given namespace. May return {@code null} if the
     *         default prefix for given namespace is not defined.
     */
    private String getDefaultPrefix(String namespace) {
        return defaultNamespaceToPrefixMap.get(namespace);
    }
}

