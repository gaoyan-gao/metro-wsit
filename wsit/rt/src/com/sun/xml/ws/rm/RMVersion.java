package com.sun.xml.ws.rm;

import com.sun.xml.bind.api.JAXBRIContext;

import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;

/**
 * This is the class which will determine which RM version we are dealing with
 * WSRM 1.0 or WSRM 1.1
 */
public enum RMVersion {

    WSRM10(
    "http://schemas.xmlsoap.org/ws/2005/02/rm",
    "http://schemas.xmlsoap.org/ws/2005/02/rm/policy",
    com.sun.xml.ws.rm.v200502.AcceptType.class,
    com.sun.xml.ws.rm.v200502.AckRequestedElement.class,
    com.sun.xml.ws.rm.v200502.CreateSequenceElement.class,
    com.sun.xml.ws.rm.v200502.CreateSequenceResponseElement.class,
    com.sun.xml.ws.rm.v200502.Expires.class,
    com.sun.xml.ws.rm.v200502.Identifier.class,
    com.sun.xml.ws.rm.v200502.OfferType.class,
    com.sun.xml.ws.rm.v200502.SequenceAcknowledgementElement.class,
    com.sun.xml.ws.rm.v200502.SequenceElement.class,
    com.sun.xml.ws.rm.v200502.SequenceFaultElement.class,
    com.sun.xml.ws.rm.v200502.TerminateSequenceElement.class,
    javax.xml.ws.wsaddressing.W3CEndpointReference.class),
    WSRM11(
    "http://docs.oasis-open.org/ws-rx/wsrm/200702",
    "http://docs.oasis-open.org/ws-rx/wsrmp/200702",
    com.sun.xml.ws.rm.v200702.AcceptType.class,
    com.sun.xml.ws.rm.v200702.AckRequestedElement.class,
    com.sun.xml.ws.rm.v200702.Address.class,
    com.sun.xml.ws.rm.v200702.CloseSequenceElement.class,
    com.sun.xml.ws.rm.v200702.CloseSequenceResponseElement.class,
    com.sun.xml.ws.rm.v200702.CreateSequenceElement.class,
    com.sun.xml.ws.rm.v200702.CreateSequenceResponseElement.class,
    com.sun.xml.ws.rm.v200702.DetailType.class,
    com.sun.xml.ws.rm.v200702.Expires.class,
    com.sun.xml.ws.rm.v200702.Identifier.class,
    com.sun.xml.ws.rm.v200702.IncompleteSequenceBehaviorType.class,
    com.sun.xml.ws.rm.v200702.MakeConnectionElement.class,
    com.sun.xml.ws.rm.v200702.MessagePendingElement.class,
    com.sun.xml.ws.rm.v200702.OfferType.class,
    com.sun.xml.ws.rm.v200702.SequenceAcknowledgementElement.class,
    com.sun.xml.ws.rm.v200702.SequenceElement.class,
    com.sun.xml.ws.rm.v200702.SequenceFaultElement.class,
    com.sun.xml.ws.rm.v200702.TerminateSequenceElement.class,
    com.sun.xml.ws.rm.v200702.TerminateSequenceResponseElement.class,
    com.sun.xml.ws.rm.v200702.UsesSequenceSSL.class,
    com.sun.xml.ws.rm.v200702.UsesSequenceSTR.class,
    javax.xml.ws.wsaddressing.W3CEndpointReference.class);
    /**
     * General constants
     */
    public final String namespaceUri;
    public final String policyNamespaceUri;
    public final JAXBRIContext jaxbContext;
    /**
     * Action constants
     */
    public final String ackRequestedAction;
    public final String createSequenceAction;
    public final String createSequenceResponseAction;
    public final String closeSequenceAction;
    public final String closeSequenceResponseAction;
    public final String lastAction;
    public final String makeConnectionAction;
    public final String sequenceAcknowledgementAction;
    public final String terminateSequenceAction;
    public final String terminateSequenceResponseAction;
    /**
     * QName constants
     */
    public final QName ackRequestedQName;
    public final QName closedSequenceQname;
    public final QName createSequenceRefusedQname;
    public final QName inactivityTimeoutAssertionQName;
    public final QName messageNumberRolloverQname;
    public final QName rmPolicyAssertionQName;
    public final QName sequenceAcknowledgementQName;
    public final QName sequenceQName;
    public final QName sequenceSTRAssertionQName;
    public final QName sequenceTerminatedQname;
    public final QName sequenceTransportSecurityAssertionQName;
    public final QName unknownSequenceQname;

    private RMVersion(String nsUri, String policynsuri, Class<?>... classes) {
        this.namespaceUri = nsUri;
        this.policyNamespaceUri = policynsuri;

        this.ackRequestedAction = namespaceUri + "/AckRequested";
        this.createSequenceAction = namespaceUri + "/CreateSequence";
        this.createSequenceResponseAction = namespaceUri + "/CreateSequenceResponse";
        this.closeSequenceAction = namespaceUri + "/CloseSequence";
        this.closeSequenceResponseAction = namespaceUri + "/CloseSequenceResponse";
        this.lastAction = namespaceUri + "/LastMessage";
        this.makeConnectionAction = namespaceUri + "/MakeConnection";
        this.sequenceAcknowledgementAction = namespaceUri + "/SequenceAcknowledgement";
        this.terminateSequenceAction = namespaceUri + "/TerminateSequence";
        this.terminateSequenceResponseAction = namespaceUri + "/TerminateSequenceResponse";

        this.ackRequestedQName = new QName(namespaceUri, "AckRequested");
        this.closedSequenceQname = new QName(namespaceUri, "SequenceClosed");
        this.createSequenceRefusedQname = new QName(namespaceUri, "CreateSequenceRefused");
        this.inactivityTimeoutAssertionQName = new QName(policyNamespaceUri, "InactivityTimeout");
        this.messageNumberRolloverQname = new QName(namespaceUri, "MessageNumberRollover");
        this.rmPolicyAssertionQName = new QName(policyNamespaceUri, "RMAssertion");
        this.sequenceAcknowledgementQName = new QName(namespaceUri, "SequenceAcknowledgement");
        this.sequenceQName = new QName(namespaceUri, "Sequence");
        this.sequenceSTRAssertionQName = new QName(policyNamespaceUri, "SequenceSTR");
        this.sequenceTerminatedQname = new QName(namespaceUri, "SequenceTerminated");
        this.sequenceTransportSecurityAssertionQName = new QName(policyNamespaceUri, "SequenceTransportSecurity");
        this.unknownSequenceQname = new QName(namespaceUri, "UnknownSequence");

        try {
            this.jaxbContext = JAXBRIContext.newInstance(classes, null, null, null, false, null);
        } catch (JAXBException e) {
            throw new Error(e);
        }
    }

    public boolean isRMAction(String action) {
        return ackRequestedAction.equals(action) ||
                createSequenceAction.equals(action) ||
                createSequenceResponseAction.equals(action) ||
                closeSequenceAction.equals(action) ||
                closeSequenceResponseAction.equals(action) ||
                lastAction.equals(action) ||
                makeConnectionAction.equals(action) ||
                sequenceAcknowledgementAction.equals(action) ||
                terminateSequenceAction.equals(action) ||
                terminateSequenceResponseAction.equals(action);
    }
}
