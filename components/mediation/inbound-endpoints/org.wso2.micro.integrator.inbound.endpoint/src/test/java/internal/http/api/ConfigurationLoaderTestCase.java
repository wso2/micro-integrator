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
package internal.http.api;

import junit.framework.Assert;
import org.junit.After;
import org.junit.Test;
import org.wso2.carbon.inbound.endpoint.internal.http.api.ConfigurationLoader;
import org.wso2.carbon.inbound.endpoint.internal.http.api.Constants;
import org.wso2.carbon.inbound.endpoint.internal.http.api.InternalAPI;

import java.net.URL;
import java.util.List;

public class ConfigurationLoaderTestCase {

    /**
     * Test loading of internal apis from the internal-apis.xml file.
     */
    @Test
    public void testLoadInternalAPIs() {
        System.setProperty(Constants.PREFIX_TO_ENABLE_INTERNAL_APIS + "SampleAPI", "true");
        URL url = getClass().getResource("internal-apis.xml");
        Assert.assertNotNull("Configuration file not found", url);

        ConfigurationLoader.loadInternalApis("internal/http/api/internal-apis.xml");
        List<InternalAPI> apis = ConfigurationLoader.getHttpInternalApis();
        Assert.assertEquals("Expected number of APIs not found", 1, apis.size());
        Assert.assertEquals("Loaded API name is not correct", "SampleAPI", apis.get(0).getName());
        Assert.assertEquals("Loaded API context is not correct", "/foo", apis.get(0).getContext());
    }

    @After
    public void cleanup() {
        ConfigurationLoader.destroy();
    }

}
