/*
 * Copyright (c) 2022, WSO2 LLC (http://www.wso2.com).
 *
 * WSO2 LLC licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.micro.integrator.http.utils;

import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentProducer;
import org.apache.http.entity.EntityTemplate;
import org.apache.http.impl.client.CloseableHttpClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.zip.GZIPOutputStream;

import static org.wso2.micro.integrator.http.utils.Utils.getPayload;

/**
 * This class provides the base implementation for a multithreaded HTTP client.
 */
public abstract class MultiThreadedHTTPClient implements Callable<Boolean> {

    private final CloseableHttpClient httpClient;
    private final HttpUriRequest request;
    private final HTTPRequestWithBackendResponse httpRequestWithBackendResponse;

    public MultiThreadedHTTPClient(CloseableHttpClient httpClient, String apiInvocationURL,
                                   HTTPRequestWithBackendResponse httpRequestWithBackendResponse) {

        this.httpClient = httpClient;
        this.httpRequestWithBackendResponse = httpRequestWithBackendResponse;
        this.request = generateRequest(apiInvocationURL, httpRequestWithBackendResponse.getHttpRequest());
    }

    @Override
    public Boolean call() throws Exception {

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            onResponseReceived(response, httpRequestWithBackendResponse);
        }
        return true;
    }

    /**
     * This method must be implemented by concrete implementations of this class to provide the logic to validate the
     * HTTP response.
     *
     * @param response                       The HTTP response returned by the client
     * @param httpRequestWithBackendResponse The Mock HTTP request and backend response
     * @throws Exception If an error occurs while validating the response
     */
    protected abstract boolean onResponseReceived(CloseableHttpResponse response,
                                                  HTTPRequestWithBackendResponse httpRequestWithBackendResponse)
            throws Exception;

    /**
     * Extracts the payload from a HTTP response. For a given HttpResponse object, this
     * method can be called only once.
     *
     * @param response HttpResponse instance to be extracted
     * @return Content payload
     * @throws IOException If an error occurs while reading from the response
     */
    public static String getResponsePayload(HttpResponse response) throws IOException {

        if (response.getEntity() != null) {
            InputStream in = response.getEntity().getContent();
            int length;
            byte[] tmp = new byte[2048];
            StringBuilder buffer = new StringBuilder();
            while ((length = in.read(tmp)) != -1) {
                buffer.append(new String(tmp, 0, length));
            }
            return buffer.toString();
        }
        return null;
    }

    /**
     * Creates a HTTP GET or POST request with the specified URL.
     *
     * @param apiInvocationURL Target endpoint URL
     * @param httpRequest      Mock HTTP request
     * @return HttpUriRequest request
     */
    private static HttpUriRequest generateRequest(String apiInvocationURL,
                                                  HTTPRequest httpRequest) {

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "text/plain");

        if (httpRequest.getMethod().equals(RequestMethods.GET)) {
            return doGet((apiInvocationURL), headers);
        }
        return doPost((apiInvocationURL), headers, getPayload(httpRequest.getPayloadSize()), "text/plain");
    }

    /**
     * Creates a HTTP GET request with the specified URL.
     *
     * @param url     Target endpoint URL
     * @param headers Any HTTP headers that should be added to the request
     * @return HttpGet request
     */
    private static HttpUriRequest doGet(String url, Map<String, String> headers) {

        HttpUriRequest request = new HttpGet(url);
        setHeaders(headers, request);

        return request;
    }

    /**
     * Creates a HTTP POST request with the specified URL.
     *
     * @param url         Target endpoint URL
     * @param headers     Any HTTP headers that should be added to the request
     * @param payload     Content payload that should be sent
     * @param contentType Content-type of the request
     * @return HttpPost request
     */
    private static HttpUriRequest doPost(String url, final Map<String, String> headers, final String payload,
                                         String contentType) {

        HttpUriRequest request = new HttpPost(url);
        setHeaders(headers, request);
        HttpEntityEnclosingRequest entityEncReq = (HttpEntityEnclosingRequest) request;
        final boolean zip = headers != null && "gzip".equals(headers.get(HttpHeaders.CONTENT_ENCODING));

        EntityTemplate ent = new EntityTemplate(new ContentProducer() {
            public void writeTo(OutputStream outputStream) throws IOException {

                OutputStream out = outputStream;
                if (zip) {
                    out = new GZIPOutputStream(outputStream);
                }
                out.write(payload.getBytes());
                out.flush();
                out.close();
            }
        });
        ent.setContentType(contentType);
        if (zip) {
            ent.setContentEncoding("gzip");
        }
        entityEncReq.setEntity(ent);

        return request;
    }

    /**
     * Iterates a map and sets the HTTP headers to the specified HttpUriRequest.
     *
     * @param headers Any HTTP headers that should be added to the request
     * @param request HttpUriRequest to which the headers should be added
     */
    private static void setHeaders(Map<String, String> headers, HttpUriRequest request) {

        if (headers != null && headers.size() > 0) {
            for (Map.Entry<String, String> header : headers.entrySet()) {
                request.setHeader(header.getKey(), header.getValue());
            }
        }
    }
}
