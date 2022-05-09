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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.http2.server.HTTP2CServerConnectionFactory;
import org.eclipse.jetty.server.CustomRequestLog;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.http2.server.HttpTransportOverHTTP2;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.Date;

public class ESBtoBackendHttp2Test extends ESBIntegrationTest {

    final Server server = new Server();

    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {

        super.init();

        // Common HTTP configuration.
        final HttpConfiguration config = new HttpConfiguration();
        // HTTP/1.1 support.
        final HttpConnectionFactory http1 = new HttpConnectionFactory(config);
        // HTTP/2 cleartext support.
        final HTTP2CServerConnectionFactory http2c = new HTTP2CServerConnectionFactory(config);
        // Add the connector.
        final ServerConnector connector = new ServerConnector(server, http1, http2c);
        connector.setHost("localhost");
        connector.setPort(8080);
        server.addConnector(connector);
        server.setRequestLog(new CustomRequestLog());
        server.setHandler(new DefaultHandler() {

            @Override
            public void handle(String target, org.eclipse.jetty.server.Request baseRequest,
                               HttpServletRequest request, HttpServletResponse response) throws IOException {

                String out = HttpVersion.HTTP_1_1.asString();
                if (baseRequest.getHttpChannel().getHttpTransport() instanceof HttpTransportOverHTTP2) {
                    out = HttpVersion.HTTP_2.asString();
                }
                response.setStatus(200);
                response.setContentType("text/plain");
                response.setContentLength(8);
                response.setDateHeader(HttpHeader.LAST_MODIFIED.toString(), new Date().getTime());
                response.setHeader(HttpHeader.CACHE_CONTROL.toString(), "max-age=360000,public");
                response.getOutputStream().write(out.getBytes(StandardCharsets.UTF_8));
            }
        });
        // Start the server.
        server.start();
    }

    @Test(enabled = true)
    public void testHttp2RequestESBtoBackend() throws Exception {

        OkHttpClient client =
                new OkHttpClient.Builder().connectTimeout(Duration.ofSeconds(1000)).protocols(Collections.singletonList(Protocol.H2_PRIOR_KNOWLEDGE)).build();
        Request request = new Request.Builder().url("http://localhost:5056/http2BackendCall").get().build();
        Response response = client.newCall(request).execute();
        Assert.assertEquals(response.protocol(), Protocol.H2_PRIOR_KNOWLEDGE);
        Assert.assertEquals(response.body().string(), HttpVersion.HTTP_2.asString());
    }

    @AfterClass(alwaysRun = true)
    public void stop() throws Exception {

        super.cleanup();
        server.stop();
    }
}
