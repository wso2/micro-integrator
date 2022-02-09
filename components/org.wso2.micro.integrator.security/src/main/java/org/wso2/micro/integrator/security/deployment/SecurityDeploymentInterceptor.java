/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.micro.integrator.security.deployment;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.UUIDGenerator;
import org.apache.axis2.AxisFault;
import org.apache.axis2.description.AxisBinding;
import org.apache.axis2.description.AxisEndpoint;
import org.apache.axis2.description.AxisModule;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.AxisEvent;
import org.apache.axis2.engine.AxisObserver;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.neethi.Policy;
import org.apache.neethi.PolicyComponent;
import org.apache.neethi.PolicyReference;
import org.apache.neethi.builders.xml.XmlPrimtiveAssertion;
import org.apache.ws.security.handler.WSHandlerConstants;
import org.wso2.micro.core.Constants;
import org.wso2.micro.core.util.ServerException;
import org.wso2.micro.integrator.security.callback.AbstractPasswordCallback;
import org.wso2.micro.integrator.security.callback.DefaultPasswordCallback;
import org.wso2.micro.integrator.security.util.RahasUtil;
import org.wso2.micro.integrator.security.util.SecurityConfigParamBuilder;
import org.wso2.micro.integrator.security.SecurityConfigParams;
import org.wso2.micro.integrator.security.SecurityScenario;
import org.wso2.micro.integrator.security.SecurityScenarioDatabase;
import org.wso2.micro.integrator.security.user.api.UserStoreException;
import org.wso2.micro.integrator.security.util.ServerCrypto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * This is a deployment interceptor which handles service specific security configurations on
 * service deployment events.
 */
public class SecurityDeploymentInterceptor implements AxisObserver {
    private static final Log log = LogFactory.getLog(SecurityDeploymentInterceptor.class);
    private static final String NO_POLICY_ID = "NoPolicy";
    private static final String APPLY_POLICY_TO_BINDINGS = "applyPolicyToBindings";

    @Override
    public void init(AxisConfiguration axisConfig) {
        // Do Nothing
    }

    @Override
    public void moduleUpdate(AxisEvent event, AxisModule module) {
        // This method will not be used
    }

    @Override
    public void serviceGroupUpdate(AxisEvent event, AxisServiceGroup serviceGroup) {
        // This method will not be used
    }

    @Override
    public void serviceUpdate(AxisEvent axisEvent, AxisService axisService) {
        if (axisEvent.getEventType() == AxisEvent.SERVICE_DEPLOY) {
            Policy policy;
            try {
                policy = applyPolicyToBindings(axisService);
                if (policy != null) {
                    processPolicy(axisService, policy.getId(), policy);
                }
            } catch (Exception e) {
                log.error("Error while adding policies to bindings", e);
            }

            try {
                if (axisService.getPolicySubject() != null && axisService.getPolicySubject()
                        .getAttachedPolicyComponents() != null) {
                    if (log.isDebugEnabled()) {
                        log.debug("Policies found on axis service");
                    }
                    Iterator iterator;
                    iterator = axisService.getPolicySubject().getAttachedPolicyComponents().iterator();
                    String policyId = null;
                    while (iterator.hasNext()) {
                        PolicyComponent currentPolicyComponent = (PolicyComponent) iterator.next();
                        if (currentPolicyComponent instanceof Policy) {
                            policyId = ((Policy) currentPolicyComponent).getId();
                        } else if (currentPolicyComponent instanceof PolicyReference) {
                            policyId = ((PolicyReference) currentPolicyComponent).getURI().substring(1);
                        }
                        processPolicy(axisService, policyId, currentPolicyComponent);
                    }
                } else {
                    return;
                }
            } catch (Exception e) {
                String msg = "Cannot handle service DEPLOY event for service: " +
                        axisService.getName();
                log.error(msg, e);
                throw new RuntimeException(msg, e);
            }
        }

    }

    private void processPolicy(AxisService axisService, String policyId, PolicyComponent currentPolicyComponent)
            throws UserStoreException, AxisFault {
        AxisConfiguration axisConfiguration = null;
        // Do not apply anything if no policy
        if (StringUtils.isNotEmpty(policyId) && NO_POLICY_ID.equalsIgnoreCase(policyId)) {
            if (axisService != null) {
                removePermittedRoles(axisService);
                axisConfiguration = axisService.getAxisConfiguration();
            }

            if (axisConfiguration != null) {
                AxisModule module = axisConfiguration.getModule(Constants
                        .RAMPART_MODULE_NAME);
                // disengage at axis2
                axisService.disengageModule(module);
                return;
            } else {
                throw new UserStoreException("Error in getting Axis configuration.");
            }
        }

        if (policyId != null && isSecPolicy(policyId)) {
            if (log.isDebugEnabled()) {
                log.debug("Policy " + policyId + " is identified as a security " +
                        "policy and trying to apply security parameters");
            }

            SecurityScenario scenario = SecurityScenarioDatabase.getByWsuId(policyId);
            if (scenario == null) {
                // if there is no security scenario id,  put default id
                if (log.isDebugEnabled()) {
                    log.debug("Policy " + policyId + " does not belongs to a" +
                            " pre-defined security scenario. " +
                            "So treating as a custom policy");
                }
                SecurityScenario securityScenario = new SecurityScenario();
                securityScenario.setScenarioId(
                        Constants.CUSTOM_SECURITY_SCENARIO);
                securityScenario.setWsuId(policyId);
                securityScenario.setGeneralPolicy(false);
                securityScenario.setSummary(
                        Constants.CUSTOM_SECURITY_SCENARIO_SUMMARY);
                SecurityScenarioDatabase.put(policyId, securityScenario);
                scenario = securityScenario;
            }
            applySecurityParameters(axisService, scenario,
                    (Policy) currentPolicyComponent);
        }
    }

    private void applySecurityParameters(AxisService service, SecurityScenario secScenario, Policy policy) {
        try {
            SecurityConfigParams configParams =
                    SecurityConfigParamBuilder.getSecurityParams(getSecurityConfig(policy));

            // Set Trust (Rahas) Parameters
            if (secScenario.getModules().contains(Constants.TRUST_MODULE)) {
                AxisModule trustModule = service.getAxisConfiguration()
                        .getModule(Constants.TRUST_MODULE);
                if (log.isDebugEnabled()) {
                    log.debug("Enabling trust module : " + Constants.TRUST_MODULE);
                }

                service.disengageModule(trustModule);
                service.engageModule(trustModule);

                Properties cryptoProps = new Properties();
                cryptoProps.setProperty(ServerCrypto.PROP_ID_PRIVATE_STORE,
                                        configParams.getPrivateStore());
                cryptoProps.setProperty(ServerCrypto.PROP_ID_DEFAULT_ALIAS,
                                        configParams.getKeyAlias());
                if (configParams.getTrustStores() != null) {
                    cryptoProps.setProperty(ServerCrypto.PROP_ID_TRUST_STORES,
                                            configParams.getTrustStores());
                }
                service.addParameter(RahasUtil.getSCTIssuerConfigParameter(
                        ServerCrypto.class.getName(), cryptoProps, -1, null, true, true));

                service.addParameter(RahasUtil.getTokenCancelerConfigParameter());

            }

            // Authorization
            removePermittedRoles(service);
            String allowRolesParameter = configParams.getAllowedRoles();

            Parameter allowRolesProxyParam = service.getParameter(Constants.ALLOW_ROLES_PROXY_PARAM_NAME);
            String allowRolesProxyParamValue = (allowRolesProxyParam == null ? null : allowRolesProxyParam.getValue()
                    .toString());

            if (!StringUtils.isEmpty(allowRolesProxyParamValue)) {
                if (StringUtils.isEmpty(allowRolesParameter)) {
                    allowRolesParameter = allowRolesProxyParamValue;
                } else {
                    allowRolesParameter += ',' + allowRolesProxyParamValue;
                }
            }

            Parameter param = service.getParameter(WSHandlerConstants.PW_CALLBACK_REF);
            AbstractPasswordCallback callbackHandler;
            if (param != null && param.getValue() instanceof AbstractPasswordCallback) {
                callbackHandler = (AbstractPasswordCallback) param.getValue();
            } else {
                callbackHandler = new DefaultPasswordCallback();
                param = new Parameter();
                param.setName(WSHandlerConstants.PW_CALLBACK_REF);
            }

            if (allowRolesParameter != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Authorizing roles " + allowRolesParameter);
                }
                String[] allowRoles = allowRolesParameter.split(",");
                callbackHandler.setAllowedRoles(Arrays.asList(allowRoles));
            }

            param.setValue(callbackHandler);
            service.addParameter(param);

        } catch (Throwable e) {
            String msg = "Cannot apply security parameters";
            log.error(msg, e);
        }
    }

    /**
     * Extract carbon security config element from the Policy
     *
     * @param policy Security Policy
     * @return security config element
     */
    private OMElement getSecurityConfig(Policy policy) {
        Iterator<PolicyComponent> iterator = policy.getPolicyComponents().iterator();
        while (iterator.hasNext()) {
            PolicyComponent component = iterator.next();
            if (component instanceof XmlPrimtiveAssertion) {
                OMElement value = ((XmlPrimtiveAssertion) component).getValue();
                if (value != null &&
                        SecurityConfigParamBuilder.SECURITY_CONFIG_QNAME.equals(value.getQName())) {
                    if (log.isDebugEnabled()) {
                        log.debug("Carbon Security config found : " + value.toString());
                    }
                    return value;
                }
            }
        }
        return null;
    }

    @Override
    public void addParameter(Parameter param) throws AxisFault {
        // This method will not be used
    }

    @Override
    public void deserializeParameters(OMElement parameterElement) throws AxisFault {
        // This method will not be used
    }

    @Override
    public Parameter getParameter(String name) {
        // This method will not be used
        return null;
    }

    @Override
    public ArrayList getParameters() {
        // This method will not be used
        return new ArrayList();
    }

    @Override
    public boolean isParameterLocked(String parameterName) {
        // This method will not be used
        return false;
    }

    @Override
    public void removeParameter(Parameter param) throws AxisFault {
        // This method will not be used
    }

    /**
     * Check whether policyID belongs to a security scenario
     *
     * @param policyId policy id
     * @return whether policyID belongs to a security scenario
     */
    private boolean isSecPolicy(String policyId) {
        if ("RMPolicy".equals(policyId) || "WSO2CachingPolicy".equals(policyId)
            || "WSO2ServiceThrottlingPolicy".equals(policyId)) {
            return false;
        }
        if (log.isDebugEnabled()) {
            log.debug("Policy ID : " + policyId + " is identified as a security policy");
        }
        return true;
    }

    private void removePermittedRoles(AxisService service) throws AxisFault {
        Parameter param = service.getParameter(WSHandlerConstants.PW_CALLBACK_REF);
        if (param != null && param.getValue() instanceof AbstractPasswordCallback) {
            AbstractPasswordCallback callbackHandler = (AbstractPasswordCallback) param.getValue();
            callbackHandler.removeAllowedRoles();
            param.setValue(callbackHandler);
            service.addParameter(param);
        }
    }

    public void addPolicyToAllBindings(AxisService axisService, Policy policy) throws ServerException {
        try {
            if (policy.getId() == null) {
                // Generate an ID
                policy.setId(UUIDGenerator.getUUID());
            }
            Map endPointMap = axisService.getEndpoints();
            for (Object o : endPointMap.entrySet()) {
                Map.Entry entry = (Map.Entry) o;
                AxisEndpoint point = (AxisEndpoint) entry.getValue();
                AxisBinding binding = point.getBinding();
                String bindingName = binding.getName().getLocalPart();

                // Only UTOverTransport is allowed for HTTP
                if (bindingName.endsWith("HttpBinding") &&
                        (!policy.getAttributes().containsValue("UTOverTransport"))) {
                    continue;
                }
                binding.getPolicySubject().attachPolicy(policy);
                // Add the new policy to the registry
            }
        } catch (Exception e) {
            log.error("Error in adding security policy to all bindings", e);
            throw new ServerException("addPoliciesToService", e);
        }
    }

    private Policy applyPolicyToBindings(AxisService axisService) throws ServerException {
        Parameter parameter = axisService.getParameter(APPLY_POLICY_TO_BINDINGS);
        if (parameter != null && "true".equalsIgnoreCase(parameter.getValue().toString()) &&
                axisService.getPolicySubject() != null && axisService.getPolicySubject().getAttachedPolicyComponents()
                != null) {
            Iterator iterator = axisService.getPolicySubject().
                    getAttachedPolicyComponents().iterator();
            while (iterator.hasNext()) {
                PolicyComponent currentPolicyComponent = (PolicyComponent) iterator.next();
                if (currentPolicyComponent instanceof Policy) {
                    Policy policy = ((Policy) currentPolicyComponent);
                    String policyId = policy.getId();
                    axisService.getPolicySubject().detachPolicyComponent(policyId);
                    addPolicyToAllBindings(axisService, policy);
                    return policy;
                }
            }
        }
        return null;
    }

}
