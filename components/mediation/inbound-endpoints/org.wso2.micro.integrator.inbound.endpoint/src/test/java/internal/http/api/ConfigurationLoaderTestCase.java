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
import org.wso2.carbon.inbound.endpoint.internal.http.api.InternalAPIHandler;

import java.net.URL;
import java.util.List;
import java.util.Map;

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

    /**
     * Test loading of internal apis from the internal-apis.xml file.
     */
    @Test
    public void testLoadHandlers() {
        InternalAPI api = getApis().get(0);
        List<InternalAPIHandler> handlers = api.getHandlers();
        Assert.assertEquals("All the handlers are not populated", 3, handlers.size());

        //Assert handler with no resources
        InternalAPIHandler handlerWithNoResources = handlers.get(0);
        Assert.assertEquals("SampleInternalApiHandlerWithNoResources", handlerWithNoResources.getName());
        Assert.assertEquals(0, handlerWithNoResources.getResources().size());

        //Assert handler with all resources
        InternalAPIHandler handlerWithAllResources = handlers.get(1);
        Assert.assertEquals("SampleInternalApiHandlerWithAllResources", handlerWithAllResources.getName());
        Assert.assertEquals(1, handlerWithAllResources.getResources().size());
        Assert.assertEquals("/", handlerWithAllResources.getResources().get(0));

        //Assert handler with 2 resources
        InternalAPIHandler handlerWithCustomResources = handlers.get(2);
        Assert.assertEquals("SampleInternalApiHandlerWithCustomResources", handlerWithCustomResources.getName());
        Assert.assertEquals(2, handlerWithCustomResources.getResources().size());
        Assert.assertEquals("/resource1", handlerWithCustomResources.getResources().get(0));
        Assert.assertEquals("/resource2", handlerWithCustomResources.getResources().get(1));

    }

    private List<InternalAPI> getApis() {
        System.setProperty(Constants.PREFIX_TO_ENABLE_INTERNAL_APIS + "SampleAPI", "true");
        URL url = getClass().getResource("internal-apis.xml");
        Assert.assertNotNull("Configuration file not found", url);

        ConfigurationLoader.loadInternalApis("internal/http/api/internal-apis.xml");
        return ConfigurationLoader.getHttpInternalApis();
    }

    @After
    public void cleanup() {
        ConfigurationLoader.destroy();
    }

}
