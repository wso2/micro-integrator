/*
 *
 *  *Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *  *
 *  *WSO2 Inc. licenses this file to you under the Apache License,
 *  *Version 2.0 (the "License"); you may not use this file except
 *  *in compliance with the License.
 *  *You may obtain a copy of the License at
 *  *
 *  *http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *Unless required by applicable law or agreed to in writing,
 *  *software distributed under the License is distributed on an
 *  *"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  *KIND, either express or implied.  See the License for the
 *  *specific language governing permissions and limitations
 *  *under the License.
 *  
 */

package deployer;

import org.junit.Assert;
import org.junit.Test;
import org.wso2.micro.application.deployer.AppDeployerUtils;

public class AppDeployerUtilsTest {

    @Test
    public void createRegistryPath_govRegistryPath() {

        final String inputPath = "/_system/governance/my-resource.xml";
        final String expectedOutput = "gov:/my-resource.xml";
        final String result = AppDeployerUtils.createRegistryPath(inputPath);
        Assert.assertEquals(expectedOutput, result);
    }    
    
    @Test
    public void createRegistryPath_configRegistryPath() {

        final String inputPath = "/_system/config/my-resource.xml";
        final String expectedOutput = "conf:/my-resource.xml";
        final String result = AppDeployerUtils.createRegistryPath(inputPath);
        Assert.assertEquals(expectedOutput, result);
    }
}