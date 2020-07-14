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

package org.wso2.micro.service.mgt;

import org.apache.axis2.AxisFault;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.wso2.micro.service.mgt.util.Utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ServiceAdmin {

    private static AxisConfiguration axisConfiguration = null;
    private static ServiceAdmin serviceAdmin = null;

    private ServiceAdmin() {

    }

    public static ServiceAdmin getInstance() {

        if (serviceAdmin == null) {
            serviceAdmin = new ServiceAdmin();
        }
        return serviceAdmin;
    }

    public void init(AxisConfiguration axisConfiguration) {

        ServiceAdmin.axisConfiguration = axisConfiguration;
    }

    public Boolean isInitailized() {

        if (axisConfiguration != null) {
            return true;
        }
        return false;
    }

    public ServiceMetaData getServiceData(String serviceName) throws Exception {

        AxisService service = axisConfiguration.getServiceForActivation(serviceName);

        if (service == null) {
            String msg = "Invalid service name, service not found : " + serviceName;
            throw new AxisFault(msg);
        }

        String serviceType = getServiceType(service);
        List<String> ops = new ArrayList<String>();
        for (Iterator<AxisOperation> opIter = service.getOperations(); opIter.hasNext(); ) {
            AxisOperation axisOperation = opIter.next();

            if (axisOperation.getName() != null) {
                ops.add(axisOperation.getName().getLocalPart());
            }
        }

        ServiceMetaData serviceMetaData = new ServiceMetaData();
        serviceMetaData.setOperations(ops.toArray(new String[ops.size()]));
        serviceMetaData.setName(serviceName);
        serviceMetaData.setServiceId(serviceName);
        serviceMetaData.setServiceVersion("");
        serviceMetaData.setActive(service.isActive());
        String[] eprs = this.getServiceEPRs(serviceName);
        serviceMetaData.setEprs(eprs);
        serviceMetaData.setServiceType(serviceType);

        serviceMetaData.setWsdlURLs(Utils.getWsdlInformation(serviceName, axisConfiguration));
        serviceMetaData.setSwaggerUrl(Utils.getSwaggerUrl(serviceName, axisConfiguration));

        AxisServiceGroup serviceGroup = (AxisServiceGroup) service.getParent();
        serviceMetaData.setFoundWebResources(serviceGroup.isFoundWebResources());
        serviceMetaData.setScope(service.getScope());
        serviceMetaData.setWsdlPorts(service.getEndpoints());
        Parameter deploymentTime = service.getParameter("serviceDeploymentTime");
        if (deploymentTime != null) {
            serviceMetaData.setServiceDeployedTime((Long) deploymentTime.getValue());
        }

        serviceMetaData.setServiceGroupName(serviceGroup.getServiceGroupName());

        if (service.getDocumentation() != null) {
            serviceMetaData.setDescription(service.getDocumentation());
        } else {
            serviceMetaData.setDescription("No service description found");
        }

        Parameter parameter = service.getParameter("enableMTOM");
        if (parameter != null) {
            serviceMetaData.setMtomStatus((String) parameter.getValue());
        } else {
            serviceMetaData.setMtomStatus("false");
        }

        parameter = service.getParameter("disableTryIt");
        if (parameter != null && Boolean.TRUE.toString().equalsIgnoreCase((String) parameter.getValue())) {
            serviceMetaData.setDisableTryit(true);
        }

        return serviceMetaData;
    }

    private String getServiceType(AxisService service) {

        Parameter serviceTypeParam = service.getParameter("serviceType");
        String serviceType;
        if (serviceTypeParam != null) {
            serviceType = (String) serviceTypeParam.getValue();
        } else {
            serviceType = "axis2";
        }

        return serviceType;
    }

    private String[] getServiceEPRs(String serviceName) {

        this.getAxisService(serviceName).setEPRs((String[]) null);

        try {
            return this.getAxisService(serviceName).getEPRs();
        } catch (NullPointerException var3) {
            return new String[0];
        }
    }

    private AxisService getAxisService(String serviceName) {

        return axisConfiguration.getServiceForActivation(serviceName);
    }

}
