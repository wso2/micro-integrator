/*
 * Copyright 2005-2007 WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.micro.core.services.listners;

import org.apache.axis2.AxisFault;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.wso2.micro.core.services.processors.ConfigurationServiceProcessor;

import java.util.HashMap;
import java.util.Map;

public class Axis2ConfigServiceListener implements ServiceListener {

    private static  Log log = LogFactory.getLog(  Axis2ConfigServiceListener.class);

    public static final String AXIS2_CONFIG_SERVICE = "org.apache.axis2.osgi.config.service";

    private BundleContext bundleContext;

    private Map<String, ConfigurationServiceProcessor> configServiceProcessorMap;

    public Axis2ConfigServiceListener(AxisConfiguration axisConfig, BundleContext context) {
        this.bundleContext = context;
        registerConfigServiceProcessors(axisConfig);
        processRegisteredAxis2ConfigServices();
    }

    public void serviceChanged(ServiceEvent event) {
        ServiceReference reference = event.getServiceReference();
        processRegisteredAxis2ConfigServices(reference, event.getType());
    }

    private void processRegisteredAxis2ConfigServices() {
        try {
            //Processing Axis2 config services..
            ServiceReference[] references = bundleContext.getServiceReferences((String)null,
                    "(" + AXIS2_CONFIG_SERVICE + "=*)");

            if (references != null) {
                for (ServiceReference sr : references) {
                    processRegisteredAxis2ConfigServices(sr, ServiceEvent.REGISTERED);
                }
            }
        } catch (InvalidSyntaxException e) {
            log.error("Failed to obtain registerd services. Invalid filter Syntax.", e);
        }
    }

    private void registerConfigServiceProcessors(AxisConfiguration axisConfig) {
        configServiceProcessorMap = new HashMap<String, ConfigurationServiceProcessor>();

        //Putting config service processors
      /*  configServiceProcessorMap.put(Deployer.class.getName(),
                                      new DeployerServiceProcessor(axisConfig, bundleContext));
        configServiceProcessorMap.put(Axis2ConfigParameterProvider.class.getName(),
                                      new Axis2ConfigParameterProcessor(axisConfig, bundleContext));
        configServiceProcessorMap.put(AxisObserver.class.getName(),
                                      new AxisObserverProcessor(axisConfig, bundleContext));*/
    }

    private void processRegisteredAxis2ConfigServices(ServiceReference sr, int eventType) {
        String configService = (String) sr.getProperty(AXIS2_CONFIG_SERVICE);
        if (configServiceProcessorMap != null) {
            ConfigurationServiceProcessor configServiceProcessor = configServiceProcessorMap.get(configService);
            if (configServiceProcessor != null) {
                try {
                    configServiceProcessor.processConfigurationService(sr, eventType);
                } catch (AxisFault axisFault) {
                    String msg = "Failed to process the configuration service :" + configService;
                    log.error(msg, axisFault);
                }
            }
        }
    }
}
