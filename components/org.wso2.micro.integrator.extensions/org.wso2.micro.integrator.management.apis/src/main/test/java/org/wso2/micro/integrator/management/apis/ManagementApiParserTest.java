/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.micro.integrator.management.apis;

import org.apache.axiom.om.OMElement;
import org.junit.Assert;
import org.junit.Test;
import org.wso2.micro.core.util.CarbonException;
import org.wso2.micro.integrator.core.internal.MicroIntegratorBaseConstants;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.Map;

import static org.wso2.micro.integrator.management.apis.Constants.MGT_API_NAME;
import static org.wso2.micro.integrator.management.apis.Constants.NAME_ATTR;

public class ManagementApiParserTest {

    @Test
    public void getConfigurationFileTest() {
        initializeConfDirectory();
        Assert.assertEquals(getClass().getResource("internal-apis.xml").getPath(),
                            ManagementApiParser.getConfigurationFilePath());
    }

    private void initializeConfDirectory() {
        String filePath = getClass().getResource("").getPath();
        System.setProperty(MicroIntegratorBaseConstants.CARBON_CONFIG_DIR_PATH, filePath);
    }

    @Test
    public void getManagementApiElement() throws CarbonException, XMLStreamException, ManagementApiUndefinedException, IOException {
        initializeConfDirectory();
        OMElement managementApiElement = ManagementApiParser.getManagementApiElement();
        Assert.assertNotNull(managementApiElement);
        Assert.assertEquals(MGT_API_NAME, managementApiElement.getAttributeValue(NAME_ATTR));
    }

    @Test
    public void getUserList() throws CarbonException, IOException, UserStoreUndefinedException, ManagementApiUndefinedException, XMLStreamException {
        initializeConfDirectory();
        ManagementApiParser managementApiParser = new ManagementApiParser();
        Map<String, char[]> userList = managementApiParser.getUserList();

        Assert.assertEquals(3, userList.size());
        //Assert admin:admin
        Assert.assertNotNull(userList.get("admin"));
        Assert.assertEquals("admin", String.valueOf(userList.get("admin")));

        //Assert user1:pwd1
        Assert.assertNotNull(userList.get("user1"));
        Assert.assertEquals("pwd1", String.valueOf(userList.get("user1")));

        //Assert user2:pwd2
        Assert.assertNotNull(userList.get("user2"));
        Assert.assertEquals("pwd2", String.valueOf(userList.get("user2")));
    }
}
