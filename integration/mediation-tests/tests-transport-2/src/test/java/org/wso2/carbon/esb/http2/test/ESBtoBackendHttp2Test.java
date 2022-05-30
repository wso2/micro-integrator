/*
 *   Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *    WSO2 Inc. licenses this file to you under the Apache License,
 *    Version 2.0 (the "License"); you may not use this file except
 *    in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */

package org.wso2.carbon.esb.http2.test;

import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.synapse.transport.netty.BridgeConstants;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;

import java.time.Duration;
import java.util.Collections;

public class ESBtoBackendHttp2Test extends ESBIntegrationTest {

    final BackendServer server = new BackendServer();

    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {

        super.init();
        server.startServer();
    }

    @Test(description = "Verify HTTP/2 protocol used in request-response between the ESB and backend")
    public void testHttp2RequestESBtoBackend() throws Exception {

        OkHttpClient client =
                new OkHttpClient.Builder().protocols(Collections.singletonList(Protocol.H2_PRIOR_KNOWLEDGE)).connectTimeout(Duration.ofSeconds(1000)).build();
        Request request = new Request.Builder().url("http://localhost:5056/http2BackendCall").get().build();
        Response response = client.newCall(request).execute();
        Assert.assertEquals(response.body().string(), BridgeConstants.HTTP_2_0_VERSION, "HTTP/2 protocol is not used "
                + "between the ESB and backend communication");
    }

    @AfterClass(alwaysRun = true)
    public void stop() throws Exception {

        super.cleanup();
        server.stop();
    }
}
