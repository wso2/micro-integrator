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
package org.wso2.micro.integrator.dataservices.core.internal;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.micro.integrator.dataservices.core.DataServiceFault;
import org.wso2.micro.integrator.dataservices.core.description.event.EventTrigger;
import org.wso2.micro.integrator.ndatasource.core.DataSourceService;
import org.wso2.carbon.securevault.SecretCallbackHandlerService;
import org.wso2.micro.core.util.Axis2ConfigurationContextObserver;
import org.wso2.micro.core.util.ConfigurationContextService;

@Component(name = "dataservices.component", immediate = true)
public class DataServicesDSComponent {

    private static Log log = LogFactory.getLog(DataServicesDSComponent.class);

    private static DataSourceService dataSourceService;

    private static SecretCallbackHandlerService secretCallbackHandlerService;

    private static ConfigurationContextService contextService;

    private static Object dsComponentLock =
            new Object(); /* class level lock for controlling synchronized access to static variables */

    /* this is to keep event trigger objects which are not registered for subscription*/
    private static List<EventTrigger> eventTriggerList = new ArrayList<EventTrigger>();

    public DataServicesDSComponent() {
    }

    @Activate
    protected void activate(ComponentContext ctxt) {
        try {
            BundleContext bundleContext = ctxt.getBundleContext();
            bundleContext.registerService(Axis2ConfigurationContextObserver.class.getName(),
                                          new DSAxis2ConfigurationContextObserver(), null);
//            bundleContext.registerService(DSDummyService.class.getName(), new DSDummyService(), null);
//            bundleContext.registerService(TransactionManagerDummyService.class.getName(),
//                                          new TransactionManagerDummyService(), null);

            log.debug("Data Services bundle is activated ");
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            /* don't throw exception */
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext ctxt) {
        log.debug("Data Services bundle is deactivated ");
    }

    @Reference(
            name = "datasources.service",
            service = org.wso2.micro.integrator.ndatasource.core.DataSourceService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetDataSourceService")
    protected void setDataSourceService(DataSourceService dataSourceService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting the Data Sources Service");
        }
        DataServicesDSComponent.dataSourceService = dataSourceService;
    }

    protected void unsetDataSourceService(DataSourceService dataSourceService) {
        if (log.isDebugEnabled()) {
            log.debug("Unsetting the Data Sources Service");
        }
        DataServicesDSComponent.dataSourceService = null;
    }

    public static DataSourceService getDataSourceService() {
        return dataSourceService;
    }

    public static void registerSubscriptions(EventTrigger eventTrigger) throws DataServiceFault {
//        synchronized (dsComponentLock) {
//            if (DataServicesDSComponent.eventBroker == null) {
//                eventTriggerList.add(eventTrigger);
//            } else {
//                eventTrigger.processEventTriggerSubscriptions();
//            }
//        }
    }

    public static void processSubscriptionsForEventTriggers() {
//        if (eventTriggerList.size() > 0 && DataServicesDSComponent.eventBroker != null) {
//            for (EventTrigger trigger: eventTriggerList) {
//                trigger.processEventTriggerSubscriptions();
//            }
//        }
    }

//    public static String getUsername() {
//        return CarbonContext.getThreadLocalCarbonContext().getUsername();
//    }

    public static SecretCallbackHandlerService getSecretCallbackHandlerService() {
        return DataServicesDSComponent.secretCallbackHandlerService;
    }

    @Reference(
            name = "secret.callback.handler.service",
            service = org.wso2.carbon.securevault.SecretCallbackHandlerService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetSecretCallbackHandlerService")
    protected void setSecretCallbackHandlerService(
            SecretCallbackHandlerService secretCallbackHandlerService) {
        if (log.isDebugEnabled()) {
            log.debug("SecretCallbackHandlerService acquired");
        }
        DataServicesDSComponent.secretCallbackHandlerService = secretCallbackHandlerService;

    }

    protected void unsetSecretCallbackHandlerService(
            SecretCallbackHandlerService secretCallbackHandlerService) {
        DataServicesDSComponent.secretCallbackHandlerService = null;
    }

    public static ConfigurationContextService getContextService() {
        return contextService;
    }
}
