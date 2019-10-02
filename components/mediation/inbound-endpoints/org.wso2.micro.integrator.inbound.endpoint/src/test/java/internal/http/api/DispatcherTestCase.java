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
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.synapse.MessageContext;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.core.axis2.Axis2SynapseEnvironment;
import org.apache.synapse.core.axis2.MessageContextCreatorForAxis2;
import org.apache.synapse.rest.RESTConstants;
import org.junit.After;
import org.junit.Test;
import org.wso2.carbon.inbound.endpoint.internal.http.api.ConfigurationLoader;
import org.wso2.carbon.inbound.endpoint.internal.http.api.InternalAPI;
import org.wso2.carbon.inbound.endpoint.internal.http.api.InternalAPIDispatcher;

import java.util.List;

public class DispatcherTestCase {

    /**
     * Test request dispatching of internal apis.
     */
    @Test
    public void testDispatching() throws Exception {
        System.setProperty(
                org.wso2.carbon.inbound.endpoint.internal.http.api.Constants.PREFIX_TO_ENABLE_INTERNAL_APIS
                        + "SampleAPI", "true");

        ConfigurationLoader.loadInternalApis("internal/http/api/internal-apis.xml");
        List<InternalAPI> apis = ConfigurationLoader.getHttpInternalApis();
        Assert.assertEquals("Expected API not loaded", 1, apis.size());

        InternalAPIDispatcher internalAPIDispatcher = new InternalAPIDispatcher(apis);
        MessageContext synCtx = createMessageContext();
        setRequestProperties(synCtx, "/foo/bar?q=abc", "GET");

        boolean dispatchingCompleted = internalAPIDispatcher.dispatch(synCtx);
        Assert.assertTrue("Dispatcher failed to find correct API or Resource", dispatchingCompleted);
        Assert.assertTrue("Correct resource not invoked", (Boolean) synCtx.getProperty("Success"));
        Assert.assertEquals("abc", synCtx.getProperty(RESTConstants.REST_QUERY_PARAM_PREFIX + "q"));
    }

    private MessageContext createMessageContext() throws AxisFault {
        SynapseConfiguration synConfig = new SynapseConfiguration();
        AxisConfiguration axisConfig = new AxisConfiguration();
        synConfig.setAxisConfiguration(axisConfig);

        org.apache.axis2.context.MessageContext axis2Ctx = new org.apache.axis2.context.MessageContext();
        ConfigurationContext cfgCtx = new ConfigurationContext(axisConfig);
        axis2Ctx.setConfigurationContext(cfgCtx);

        MessageContextCreatorForAxis2.setSynConfig(synConfig);
        MessageContextCreatorForAxis2.setSynEnv(new Axis2SynapseEnvironment(synConfig));
        return MessageContextCreatorForAxis2.getSynapseMessageContext(axis2Ctx);
    }

    private void setRequestProperties(MessageContext synCtx, String path, String method) {
        org.apache.axis2.context.MessageContext axis2Ctx = ((Axis2MessageContext) synCtx).getAxis2MessageContext();
        axis2Ctx.setProperty(Constants.Configuration.TRANSPORT_IN_URL, path);
        axis2Ctx.setProperty(Constants.Configuration.HTTP_METHOD, method);
    }

    @After
    public void cleanup() {
        ConfigurationLoader.destroy();
    }
}
