/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.micro.integrator.identity.entitlement.mediator.callback;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.wso2.micro.integrator.identity.entitlement.proxy.Attribute;

/**
 * An extension to this class can feed the Entitlement mediator with subject/resource/action and
 * envs.
 */
public abstract class EntitlementCallbackHandler {

    private static final Log log = LogFactory.getLog(EntitlementCallbackHandler.class);

    /**
     * Get the user name who should be authorized against defined Entitlement policies. The default
     * implementation reads the subject name from the
     * <code>org.apache.axis2.context.MessageContext</code> as a property. The name of this property
     * should be set as a property defined under axis2 scope with the name xacml_subject_identifier.
     * If the property xacml_subject_identifier not found, then the subject name would be read from
     * a property defined under axis2 scope with the name xacml_subject
     *
     * @param synCtx
     * @return
     */
    public String getUserName(MessageContext synCtx) {
        Axis2MessageContext axis2Msgcontext = null;
        org.apache.axis2.context.MessageContext msgContext;
        axis2Msgcontext = (Axis2MessageContext) synCtx;
        msgContext = axis2Msgcontext.getAxis2MessageContext();
        String subjectIdentifier = (String) axis2Msgcontext.getProperty("xacml_subject_identifier");
        if (subjectIdentifier != null) {
            return (String) msgContext.getProperty(subjectIdentifier);
        }
        return (String) axis2Msgcontext.getProperty("xacml_subject");
    }

    /**
     * Get the name of the operation been invoked by the user. If the property xacml_use_rest
     * defined under axis2 scope been found - with the value "true" - the HTTP_METHOD will be picked
     * as the operation name.
     *
     * @param synCtx
     * @return
     */
    public String findOperationName(MessageContext synCtx) {
        org.apache.axis2.context.MessageContext msgContext;
        Axis2MessageContext axis2Msgcontext = null;
        axis2Msgcontext = (Axis2MessageContext) synCtx;
        msgContext = axis2Msgcontext.getAxis2MessageContext();
        String useRest = (String) msgContext.getProperty("xacml_use_rest");
        if (useRest == null || "false".equals(useRest.toLowerCase())) {
            return msgContext.getEnvelope().getSOAPBodyFirstElementLocalName();
        } else {
            return (String) msgContext.getProperty("HTTP_METHOD");
        }
    }

    /**
     * Get the name the service been invoked by the user. If the property xacml_resource_prefix
     * defined under axis2 scope been found - the service name will be prefixed by that value. Also
     * if the property xacml_resource_prefix_only defined under axis2 scope been found and been set
     * to true - then the service name will be replaced by the value found in xacml_resource_prefix.
     *
     * @param synCtx
     * @return
     */
    public String findServiceName(MessageContext synCtx) {
        Axis2MessageContext axis2Msgcontext = null;
        org.apache.axis2.context.MessageContext msgContext;

        axis2Msgcontext = (Axis2MessageContext) synCtx;
        msgContext = axis2Msgcontext.getAxis2MessageContext();
        String serviceName = axis2Msgcontext.getTo().getAddress();
        String resourcePrefix = (String) msgContext.getProperty("xacml_resource_prefix");
        String resourcePrefixOnly = (String) msgContext.getProperty("xacml_resource_prefix_only");

        if (resourcePrefix != null && resourcePrefix.trim().length() > 0) {
            if (resourcePrefixOnly != null && "true".equals(resourcePrefixOnly.toLowerCase())) {
                serviceName = resourcePrefix;
            } else {
                serviceName = resourcePrefix + serviceName;
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("Service name " + serviceName);
        }

        return serviceName;
    }

    /**
     * If the property xacml_action defined under axis2 scope been found - then the value of that
     * property will be picked as the action - if not the default action is "read".
     *
     * @param synCtx
     * @return
     */
    public String findAction(MessageContext synCtx) {
        Axis2MessageContext axis2Msgcontext = null;
        axis2Msgcontext = (Axis2MessageContext) synCtx;
        org.apache.axis2.context.MessageContext msgContext;
        msgContext = axis2Msgcontext.getAxis2MessageContext();
        String action = (String) msgContext.getProperty("xacml_action");
        String useRest = (String) msgContext.getProperty("xacml_use_rest");
        if (action != null) {
            if (log.isDebugEnabled()) {
                log.debug("Action " + action);
            }
            return action;
        } else if (useRest != null && "true".equals(useRest.toLowerCase())) {
            return (String) msgContext.getProperty("HTTP_METHOD");
        } else {
            return "read";
        }
    }

    /**
     * Optional hook to supply additional attributes for any category
     * including urn:oasis:names:tc:xacml:3.0:attribute-category:environment
     *
     * @param synCtx
     * @return
     */
    public Attribute[] findOtherAttributes(MessageContext synCtx) {
        return null;
    }
}
