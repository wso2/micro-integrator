/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.micro.integrator.management.apis;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import com.google.gson.Gson;
import org.apache.axis2.AxisFault;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.wso2.carbon.dataservices.common.DBConstants;
import org.wso2.carbon.dataservices.core.DBUtils;
import org.wso2.carbon.dataservices.core.engine.DataService;
import org.wso2.carbon.inbound.endpoint.internal.http.api.APIResource;
import org.wso2.carbon.micro.integrator.management.apis.models.dataServices.DataServiceSummary;
import org.wso2.carbon.micro.integrator.management.apis.models.dataServices.DataServicesList;
import org.wso2.carbon.service.mgt.ServiceAdmin;
import org.wso2.carbon.service.mgt.ServiceMetaData;

public class DataServiceResource extends APIResource {
    private final Log log = LogFactory.getLog(DataService.class);

    public DataServiceResource(String urlTemplate) {
        super(urlTemplate);
    }

    @Override
    public Set<String> getMethods() {
        Set<String> methods = new HashSet<>();
        methods.add("GET");
        methods.add("POST");
        return methods;
    }

    @Override
    public boolean invoke(MessageContext msgCtx) {
        buildMessage(msgCtx);
        String param = Utils.getQueryParameter(msgCtx, "dataServiceName");

        if (Objects.nonNull(param)) {
            populateDataServiceByName(msgCtx, param);
        } else {
            try {
                populateDataServiceList(msgCtx);
            } catch (AxisFault axisFault) {
                log.error(axisFault.getStackTrace());
            }
        }

        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) msgCtx).getAxis2MessageContext();

        axis2MessageContext.removeProperty(Constants.NO_ENTITY_BODY);
        return true;
    }

    private void populateDataServiceList(MessageContext msgCtx) throws AxisFault {
        SynapseConfiguration configuration = msgCtx.getConfiguration();
        AxisConfiguration axisConfiguration = configuration.getAxisConfiguration();
        String[] dataServicesNames = DBUtils.getAvailableDS(axisConfiguration);

        // initiate list model
        DataServicesList dataServicesList = new DataServicesList(dataServicesNames.length);

        for (String dataServiceName : dataServicesNames) {
            DataService dataService = getDataServiceByName(msgCtx, dataServiceName);
            ServiceMetaData serviceMetaData = getServiceMetaData(dataService);

            // initiate summary model
            DataServiceSummary summary = new DataServiceSummary();
            if (Objects.nonNull(serviceMetaData)) {
                summary.setServiceName(serviceMetaData.getName());
                summary.setWsdl11(serviceMetaData.getWsdlURLs()[0]);
                summary.setWsdl20(serviceMetaData.getWsdlURLs()[1]);
            }
            dataServicesList.addServiceSummary(summary);
        }

        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) msgCtx).getAxis2MessageContext();

        Utils.setJsonPayLoad(axis2MessageContext, new Gson().toJson(dataServicesList));
    }

    private void populateDataServiceByName(MessageContext msgCtx, String param) {

    }

    private DataService getDataServiceByName(MessageContext msgCtx, String serviceId) {
        AxisService axisService = msgCtx.getConfiguration().
                getAxisConfiguration().getServiceForActivation(serviceId);
        DataService dataService = null;
        if (axisService != null) {
            dataService = (DataService) axisService.getParameter(DBConstants.DATA_SERVICE_OBJECT).getValue();
        } else {
            log.error("DataService " + serviceId + " is null.");
        }
        return dataService;
    }

    private ServiceMetaData getServiceMetaData(DataService dataService) throws AxisFault {
        if (Objects.nonNull(dataService)) {
            return new ServiceAdmin().getServiceData(dataService.getName());
        } else {
            return null;
        }
    }
}