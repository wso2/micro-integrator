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
import okhttp3.Request;
import okhttp3.Response;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;

public class ESBtoBackendSecureHttp2Test extends ESBIntegrationTest {

    final BackendServer server=new BackendServer();
    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {

        super.init();
        // Common HTTP configuration.
//        final HttpConfiguration config = new HttpConfiguration();
//        config.setSecureScheme("https");
//        config.setSecurePort(8443);
//        // HTTP/1.1 support.
//        HttpConfiguration https_config = new HttpConfiguration(config);
//        https_config.addCustomizer(new SecureRequestCustomizer());
//        final HttpConnectionFactory http1 = new HttpConnectionFactory(https_config);
//
//        SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
//        sslContextFactory.setKeyStorePath(Paths.get(getClass().getResource("/").toURI()).getParent() + "/test" +
//                "-classes/keystores/products/wso2carbon.jks");
//        sslContextFactory.setKeyStorePassword("wso2carbon");
//        sslContextFactory.setKeyManagerPassword("wso2carbon");
//        sslContextFactory.setNeedClientAuth(false);
//        sslContextFactory.setWantClientAuth(false);
//        sslContextFactory.setSniRequired(false);
//        sslContextFactory.setIncludeProtocols("TLSv1.2");
//        sslContextFactory.setIncludeCipherSuites("TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256");
//
//        // HTTP/2 cleartext support.
//        final HTTP2CServerConnectionFactory http2c = new HTTP2CServerConnectionFactory(https_config);
//        //HTTP/2 secure protocol support
//        final HTTP2ServerConnectionFactory http2 = new HTTP2ServerConnectionFactory(https_config);
//        //ALPN protocol support
//        final ALPNServerConnectionFactory alpn = new ALPNServerConnectionFactory();
//        alpn.setDefaultProtocol("h2");
//        final ServerConnector connector = new ServerConnector(server, new SslConnectionFactory(sslContextFactory,
//                "alpn"), alpn, http2, http2c, http1);
//        connector.setHost("localhost");
//        connector.setPort(8444);
//        server.addConnector(connector);
//        server.setHandler(new DefaultHandler() {
//
//            @Override
//            public void handle(String target, org.eclipse.jetty.server.Request baseRequest,
//                               HttpServletRequest request, HttpServletResponse response) throws IOException {
//
//                String out = HttpVersion.HTTP_1_1.asString();
//                if (baseRequest.getHttpVersion() == HttpVersion.HTTP_2) {
//                    out = HttpVersion.HTTP_2.asString();
//                }
//                response.setStatus(200);
//                response.setContentType("text/plain");
//                response.setContentLength(8);
//                response.setDateHeader(HttpHeader.LAST_MODIFIED.toString(), new Date().getTime());
//                response.setHeader(HttpHeader.CACHE_CONTROL.toString(), "max-age=360000,public");
//                response.getOutputStream().write(out.getBytes(StandardCharsets.UTF_8));
//            }
//        });
//
//        // Start the server.
//        server.start();
        server.startSSLServer();
    }

    @Test(description = "Test secure HTTP/2 (HTTPS) request-response between the ESB and backend")
    public void testSecureHttp2RequestESBtoBackend() throws Exception {

        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        OkHttpClient client = builder.build();
//        Request request = new Request.Builder().url("https://localhost:5101/secureHttp2BackendCall").get().build();
        Request request = new Request.Builder().url("https://localhost:8080").get().build();
        Response response = client.newCall(request).execute();
        Assert.assertEquals(response.body().string(), "2.0");
    }

    @AfterClass(alwaysRun = true)
    public void stop() throws Exception {

        super.cleanup();
        server.stop();
    }
}
