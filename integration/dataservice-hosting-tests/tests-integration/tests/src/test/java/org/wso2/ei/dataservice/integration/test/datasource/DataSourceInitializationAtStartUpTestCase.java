/*
 *Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *WSO2 Inc. licenses this file to you under the Apache License,
 *Version 2.0 (the "License"); you may not use this file except
 *in compliance with the License.
 *You may obtain a copy of the License at
 *
 *http://www.apache.org/licenses/LICENSE-2.0
 *
 *Unless required by applicable law or agreed to in writing,
 *software distributed under the License is distributed on an
 *"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *KIND, either express or implied.  See the License for the
 *specific language governing permissions and limitations
 *under the License.
 */
package org.wso2.ei.dataservice.integration.test.datasource;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ei.dataservice.integration.common.utils.SampleDataServiceClient;
import org.wso2.ei.dataservice.integration.test.DSSIntegrationTest;

//https://wso2.org/jira/browse/STRATOS-1631
public class DataSourceInitializationAtStartUpTestCase extends DSSIntegrationTest {

    private static final Log log = LogFactory.getLog(DataSourceInitializationAtStartUpTestCase.class);
    private final String serviceName = "CarbonDSDataServiceTest";

    private SampleDataServiceClient client;

    @BeforeClass(alwaysRun = true)
    public void serviceDeployment() throws Exception {

        super.init();
        client = new SampleDataServiceClient(getServiceUrlHttp(serviceName));
    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        deleteService(serviceName);
        cleanup();
    }

    @Test(groups = {"wso2.dss"})
    public void selectOperation() throws AxisFault {
        for (int i = 0; i < 5; i++) {
            client.getCustomerInBoston();
        }
        log.info("Select Operation Success");
    }
}
