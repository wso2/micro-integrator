/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.micro.integrator.business.messaging.hl7.message.internal;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.micro.integrator.business.messaging.hl7.message.HL7MessageBuilder;
import org.wso2.micro.integrator.business.messaging.hl7.message.HL7MessageFormatter;
import org.wso2.micro.integrator.core.services.Axis2ConfigurationContextService;

@Component(name = "hl7.message.services",
        immediate = true)
public class HL7MessageServiceComponent {

    private static Log log = LogFactory.getLog(HL7MessageServiceComponent.class);

    private Axis2ConfigurationContextService contextService;

    public HL7MessageServiceComponent() {

    }

    @Activate
    protected void activate(ComponentContext ctxt) {

        ConfigurationContext configContext;
        if (log.isDebugEnabled()) {
            log.debug("HL7 Message Service activated");
        }
        try {
            if (contextService != null) {
                // Getting server's configContext instance
                configContext = contextService.getServerConfigContext();
            } else {
                throw new Exception(
                        "ConfigurationContext is not found while loading org.wso2.micro.integrator.transport.fix "
                                + "bundle");
            }
            configContext.getAxisConfiguration().addMessageBuilder("application/edi-hl7", new HL7MessageBuilder());
            configContext.getAxisConfiguration().addMessageFormatter("application/edi-hl7", new HL7MessageFormatter());
            if (log.isDebugEnabled()) {
                log.info("Set the HL7 message builder and formatter in the Axis2 context");
            }
            if (log.isDebugEnabled()) {
                log.debug("Successfully registered the HL7 Message Service");
            }
        } catch (Throwable e) {
            log.error("Error while activating HL7 Message Bundle", e);
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext ctxt) {

        if (log.isDebugEnabled()) {
            log.debug("HL7 Message Service deactivated");
        }
    }

    @Reference(name = "config.context.service",
            service = org.wso2.micro.integrator.core.services.Axis2ConfigurationContextService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetConfigurationContextService")
    protected void setConfigurationContextService(Axis2ConfigurationContextService contextService) {

        this.contextService = contextService;
    }

    protected void unsetConfigurationContextService(Axis2ConfigurationContextService contextService) {

        this.contextService = null;
    }
}
