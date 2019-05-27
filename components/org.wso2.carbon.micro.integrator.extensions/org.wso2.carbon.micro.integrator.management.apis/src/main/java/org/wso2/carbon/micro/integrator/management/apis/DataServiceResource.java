/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.micro.integrator.management.apis;

import javax.xml.stream.XMLStreamException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
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
import org.wso2.carbon.dataservices.core.description.query.Query;
import org.wso2.carbon.dataservices.core.engine.DataService;
import org.wso2.carbon.inbound.endpoint.internal.http.api.APIResource;

public class DataServiceResource extends APIResource {
    private final Log log = LogFactory.getLog(DataService.class);

    private static final String ROOT_ELEMENT_DATA_SERVICE = "<data></data>";
    private static final String COUNT_ELEMENT = "<Count></Count>";
    private static final String LIST_ELEMENT = "<List></List>";
    private static final String LIST_ITEM = "<Item></Item>";

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
        try {
            String[] dataServiceList = getDataServiceList(msgCtx);
            for (String s : dataServiceList) {
                OMElement doctorInformationStore = getDataServiceByName(msgCtx, s);
                populateDataServiceData(msgCtx);
                log.info(doctorInformationStore);
            }
        } catch (AxisFault | XMLStreamException e) {
            log.error(e.getStackTrace());
        }
        return true;
    }

    private String[] getDataServiceList(MessageContext msgCtx) throws AxisFault {
        SynapseConfiguration configuration = msgCtx.getConfiguration();
        AxisConfiguration axisConfiguration = configuration.getAxisConfiguration();
        return DBUtils.getAvailableDS(axisConfiguration);
    }

    private void populateDataServiceData(MessageContext msgCtx) {
        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) msgCtx).getAxis2MessageContext();

        SynapseConfiguration configuration = msgCtx.getConfiguration();

    }

    private OMElement getDataServiceByName(MessageContext msgCtx, String serviceId) throws XMLStreamException {
        AxisService axisService = msgCtx.getConfiguration().
                getAxisConfiguration().getServiceForActivation(serviceId);
        DataService dataService = null;
        if (axisService != null) {
            dataService = (DataService) axisService.getParameter(DBConstants.DATA_SERVICE_OBJECT).getValue();
            Map<String, Query> queries = dataService.getQueries();
            Iterator<Map.Entry<String, Query>> iterator = queries.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Query> pair = iterator.next();
                System.out.println(pair.getKey() + " = " + pair.getValue());
            }
            log.info(dataService.toString());
        } else {
            log.error("DataService " + serviceId + " is null.");
        }
        return convertDataServiceToOMElement(dataService);
    }

    private OMElement convertDataServiceToOMElement(DataService dataService) throws XMLStreamException {
        if (null == dataService) {
            return null;
        }

        OMElement rootElement = AXIOMUtil.stringToOM(ROOT_ELEMENT_DATA_SERVICE);


        return rootElement;
    }
}