/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.micro.integrator.security.pox;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.util.Base64;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPFault;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axiom.soap.SOAPHeaderBlock;
import org.apache.axiom.soap.impl.dom.soap11.SOAP11FaultCodeImpl;
import org.apache.axiom.soap.impl.dom.soap12.SOAP12FaultCodeImpl;
import org.apache.axiom.soap.impl.dom.soap12.SOAP12FaultSubCodeImpl;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.HandlerDescription;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.Handler;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.util.JavaUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.rampart.util.Axis2Util;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.message.WSSecHeader;
import org.apache.ws.security.message.WSSecTimestamp;
import org.apache.ws.security.message.WSSecUsernameToken;
import org.w3c.dom.Document;
import org.wso2.micro.integrator.core.services.CarbonServerConfigurationService;


import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Handler to convert the HTTP basic auth information into
 * <code>wsse:UsernameToken</code>
 */
public class POXSecurityHandler implements Handler {

    public static final String POX_CACHE_MANAGER = "POX_CACHE_MANAGER";
    public static final String POX_ENABLED = "pox-security";
    private static Log log = LogFactory.getLog(POXSecurityHandler.class);
    private static String POX_SECURITY_MODULE = "POXSecurityModule";
    private HandlerDescription description;
    private static final String MESSAGE_TYPE = "messageType";

    public void cleanup() {
        // Do Nothing
    }

    public void init(HandlerDescription description) {
        this.description = description;
    }

    public InvocationResponse invoke(MessageContext msgCtx) throws AxisFault {

        if (msgCtx != null && !msgCtx.isEngaged(POX_SECURITY_MODULE)) {
            return InvocationResponse.CONTINUE;
        }

        AxisService service = null;

        if (msgCtx != null) {
            service = msgCtx.getAxisService();
        } else {
            throw new AxisFault("Error in Axis message context.");
        }

        if (service == null) {
            if (log.isDebugEnabled()) {
                log.debug("Service not dispatched");
            }
            return InvocationResponse.CONTINUE;
        }

        // We do not add details of admin services to the registry, hence if a rest call comes to a
        // admin service that does not require authentication we simply skip it
        String isAdminService = (String) service.getParameterValue("adminService");
        if (isAdminService != null && JavaUtils.isTrueExplicitly(isAdminService)) {
            return InvocationResponse.CONTINUE;
        }

        String isHiddenService = (String) service.getParameterValue("hiddenService");
        if (isHiddenService != null && JavaUtils.isTrueExplicitly(isHiddenService)) {
            return InvocationResponse.CONTINUE;
        }

        String isReverseProxy = System.getProperty("reverseProxyMode");
        if (isReverseProxy != null && JavaUtils.isTrueExplicitly(isReverseProxy)) {
            return InvocationResponse.CONTINUE;
        }

        boolean isBasicAuth = false;
        Parameter configParameter = msgCtx.getConfigurationContext().getAxisConfiguration().getParameter("enableBasicAuth");
        if (configParameter != null && configParameter.getValue() != null) {
            isBasicAuth  = Boolean.parseBoolean(configParameter.getValue().toString());
        }

        Parameter serviceParameter = msgCtx.getAxisService().getParameter("enableBasicAuth");
        if (serviceParameter != null && serviceParameter.getValue() != null) {
            isBasicAuth  = Boolean.parseBoolean(serviceParameter.getValue().toString());
        }

        if (msgCtx.isFault() && Integer.valueOf(MessageContext.OUT_FAULT_FLOW).equals(msgCtx.getFLOW()) && isBasicAuth) {
            // we only need to execute this block in Unauthorized situations when basicAuth used
            // otherwise it should continue the message flow by throwing the incoming fault message since
            // this is already a fault response - ESBJAVA-2731
            if (log.isDebugEnabled()) {
                log.debug("SOAP Fault occurred and message flow equals to out fault flow. SOAP fault :" + msgCtx
                        .getEnvelope().toString());
            }
            try {
                String scenarioID = null;

                if(service.getPolicySubject().getAttachedPolicyComponent("UTOverTransport")!=null){
                    scenarioID = SecurityConstants.USERNAME_TOKEN_SCENARIO_ID;
                }
                if (scenarioID != null && scenarioID.equals(SecurityConstants.USERNAME_TOKEN_SCENARIO_ID)) {

                    boolean authenticationError = false;
                    String faultCode = null;

                    Object faultCodeObject = msgCtx.getEnvelope().getBody().getFault().getCode();
                    if (faultCodeObject instanceof SOAP11FaultCodeImpl) {
                        faultCode = ((SOAP11FaultCodeImpl) faultCodeObject).getTextContent();
                    } else if (faultCodeObject instanceof SOAP12FaultCodeImpl) {
                        faultCode = ((SOAP12FaultSubCodeImpl) ((SOAP12FaultCodeImpl) faultCodeObject).getSubCode()).getTextContent();
                    }

                    if (faultCode != null  && faultCode.contains("FailedAuthentication")) {  // this is standard error code according to the WS-Sec
                        authenticationError = true;
                    }

                    if (authenticationError) {
                        setAuthHeaders(msgCtx);

                        //If request is a REST then remove the soap fault tag contents to avoid it getting in client end
                        if (msgCtx.isDoingREST()) {
                            SOAPFault soapFault = msgCtx.getEnvelope().getBody().getFault();
                            if (soapFault != null) {
                                Iterator itr = soapFault.getChildren();
                                while (itr.hasNext()) {
                                    OMNode omNode = (OMNode) itr.next();
                                    if (omNode != null) {
                                        itr.remove();
                                    }
                                }
                            }
                        }
                    }
                    return InvocationResponse.CONTINUE;
                }
            } catch (Exception e) {
                // throwing the same fault which returned by the messageCtx
                throw new AxisFault("System error", msgCtx.getFailureReason());
            }

            return InvocationResponse.CONTINUE;
        }

        if (msgCtx.getIncomingTransportName() == null) {
            return InvocationResponse.CONTINUE;
        }

        //return if transport is not https or http (UT scenario should work over http transport as well.)
        if (!StringUtils.equals("https", msgCtx.getIncomingTransportName()) && !StringUtils.equals("http", msgCtx
                .getIncomingTransportName())) {
            return InvocationResponse.CONTINUE;
        }

        String basicAuthHeader = getBasicAuthHeaders(msgCtx);
        boolean soapWithoutSecHeader = isSOAPWithoutSecHeader(msgCtx);

        /**
         * Return if incoming message is soap and not associate with soap security headers with the absence of
         * basic auth headers. This will make sure soap message without security headers giving soap fault
         * instead unauthorized header
         */
        if (!msgCtx.isDoingREST() && soapWithoutSecHeader && basicAuthHeader == null && !isBasicAuth) {
            return InvocationResponse.CONTINUE;
        }

        /**
         * Return if incoming message is rest and not using basic auth
         */
        if (msgCtx.isDoingREST() && !isBasicAuth) {
            return InvocationResponse.CONTINUE;
        }

        //return if incoming message is soap and has soap security headers
        if (!soapWithoutSecHeader) {
            return InvocationResponse.CONTINUE;
        }

        if (log.isDebugEnabled()) {
            log.debug("Admin service check failed OR cache miss");
        }

        try {

            if (service.getPolicySubject().getAttachedPolicyComponent("UTOverTransport")!=null){
                if (log.isDebugEnabled()) {
                    log.debug("Processing POX security");
                }
            } else {
                return InvocationResponse.CONTINUE;
            }
            String username = null;
            String password = null;
            if (basicAuthHeader != null && basicAuthHeader.startsWith("Basic ")) {
                basicAuthHeader = new String(Base64.decode(basicAuthHeader.substring(6)));
                int i = basicAuthHeader.indexOf(':');
                if (i == -1) {
                    username = basicAuthHeader;
                } else {
                    username = basicAuthHeader.substring(0, i);
                }

                if (i != -1) {
                    password = basicAuthHeader.substring(i + 1);
                    if (StringUtils.equals("", password)) {
                        password = null;
                    }
                }
            }

            if (username == null || password == null || password.trim().length() == 0
                    || username.trim().length() == 0) {

                setAuthHeaders(msgCtx);

                return InvocationResponse.ABORT;
            }


            //If no soap header found in the request create new soap header
            Document doc = null;
            SOAPEnvelope soapEnvelop = msgCtx.getEnvelope();
            if (msgCtx.getEnvelope().getHeader() == null) {
                SOAPFactory omFac = (SOAPFactory) soapEnvelop.getOMFactory();
                SOAPEnvelope newEnvelop = omFac.getDefaultEnvelope();
                Iterator itr = soapEnvelop.getBody().getChildren();
                while (itr.hasNext()) {
                    OMNode omNode = (OMNode) itr.next();
                    if (omNode != null) {
                        itr.remove();
                        newEnvelop.getBody().addChild(omNode);
                    }
                }
                doc = Axis2Util.getDocumentFromSOAPEnvelope(newEnvelop, true);
            } else {
                doc = Axis2Util.getDocumentFromSOAPEnvelope(soapEnvelop, true);
            }

            WSSecHeader secHeader = new WSSecHeader();
            secHeader.insertSecurityHeader(doc);

            WSSecUsernameToken utBuilder = new WSSecUsernameToken();
            utBuilder.setPasswordType(WSConstants.PASSWORD_TEXT);
            utBuilder.setUserInfo(username, password);
            utBuilder.build(doc, secHeader);

            WSSecTimestamp tsBuilder = new WSSecTimestamp();
            tsBuilder.build(doc, secHeader);

            /**
             * Set the new SOAPEnvelope
             */
            msgCtx.setEnvelope(Axis2Util.getSOAPEnvelopeFromDOMDocument(doc, false));
        } catch (AxisFault e) {
            throw e;
        } catch (WSSecurityException wssEx) {
            throw new AxisFault("WSDoAllReceiver: Error in converting to Document", wssEx);
        } catch (Exception e) {
            throw new AxisFault("System error", e);
        }
        return InvocationResponse.CONTINUE;
    }

    private void setAuthHeaders(MessageContext msgCtx) throws IOException {
        String serverName = CarbonServerConfigurationService.getInstance().getFirstProperty("Name");

        if (serverName == null || serverName.trim().length() == 0) {
            serverName = "WSO2 Carbon";
        }

        HttpServletResponse response = (HttpServletResponse)
                msgCtx.getProperty(HTTPConstants.MC_HTTP_SERVLETRESPONSE);
        // TODO : verify this fix. This is to handle soap fault from UT scenario
        if (msgCtx.isFault() && response == null) {
            MessageContext originalContext = (MessageContext) msgCtx.getProperty(MessageContext.IN_MESSAGE_CONTEXT);
            if (originalContext != null) {
                response = (HttpServletResponse)
                        originalContext.getProperty(HTTPConstants.MC_HTTP_SERVLETRESPONSE);
            }
        }
        if (response != null) {
            if (msgCtx.getProperty(MESSAGE_TYPE) != null) {
                response.setContentType(String.valueOf(msgCtx.getProperty(MESSAGE_TYPE)));
            }
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.addHeader("WWW-Authenticate",
                    "BASIC realm=\"" + serverName + "\"");
            response.flushBuffer();
        } else {
            // if not servlet transport assume it to be nhttp transport
            // If the message is larger then 16k, there will be left over data to be read in the buffer and hence we need
            // to consume the request data before writing the error response. Hence msgCtx.getEnvelope().buildWithAttachments();
            msgCtx.getEnvelope().buildWithAttachments();
            msgCtx.setProperty("NIO-ACK-Requested", "true");
            msgCtx.setProperty("HTTP_SC", HttpServletResponse.SC_UNAUTHORIZED);
            Map<String, String> responseHeaders = new HashMap<>();
            responseHeaders.put("WWW-Authenticate",
                    "BASIC realm=\"" + serverName + "\"");
            msgCtx.setProperty(MessageContext.TRANSPORT_HEADERS, responseHeaders);
        }

    }

    /**
     * @param msgCtx message going through the handler chain
     * @return true if its a soap message without a security header
     */
    private boolean isSOAPWithoutSecHeader(MessageContext msgCtx) {
        //see whether security header present: if so return false
        SOAPHeader soapHeader = msgCtx.getEnvelope().getHeader();
        if (soapHeader == null) {
            return true; // no security header
        }
        //getting the set of secuirty headers
        List headerBlocks = soapHeader.getHeaderBlocksWithNSURI(WSConstants.WSSE_NS);
        // Issue is axiom - a returned collection must not be null
        if (headerBlocks != null) {
            Iterator headerBlocksIterator = headerBlocks.iterator();

            while (headerBlocksIterator.hasNext()) {
                Object o = headerBlocksIterator.next();
                SOAPHeaderBlock elem = null;
                OMElement element = null;
                if (o instanceof SOAPHeaderBlock) {
                    try {
                        elem = (SOAPHeaderBlock) o;
                    } catch (Exception e) {
                        log.error("Error while casting to soap header block", e);
                    }
                } else {
                    element = ((OMElement) o).cloneOMElement();
                }

                if (elem != null &&  WSConstants.WSSE_LN.equals(elem.getLocalName())) {
                    return false; // security header already present. invalid request.
                } else if (element != null && WSConstants.WSSE_LN.equals(element.getLocalName())) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Utility method to return basic auth transport headers if present
     *
     * @return
     */
    private String getBasicAuthHeaders(MessageContext msgCtx) {

        Map map = (Map) msgCtx.getProperty(MessageContext.TRANSPORT_HEADERS);
        if (map == null) {
            return null;
        }
        String tmp = (String) map.get("Authorization");
        if (tmp == null) {
            tmp = (String) map.get("authorization");
        }
        if (tmp != null && tmp.trim().startsWith("Basic ")) {
            return tmp;
        } else {
            return null;
        }
    }

    @Override
    public void flowComplete(MessageContext msgContext) {
        // Do Nothing
    }

    @Override
    /**
     * @see Handler#getHandlerDesc()
     */
    public HandlerDescription getHandlerDesc() {
        return this.description;
    }

    @Override
    /**
     * @see Handler#getName()
     */
    public String getName() {
        return "REST/POX Security handler";
    }

    @Override
    /**
     * @see Handler#getParameter(String)
     */
    public Parameter getParameter(String name) {
        return this.description.getParameter(name);
    }

    /**
     * Returns the default "POX_ENABLED" cache
     */
}
