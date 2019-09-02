/*
 * Copyright 2015 WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package org.wso2.carbon.mediation.transport.handlers;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.axiom.util.blob.OverflowBlob;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.util.XMLUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpInetConnection;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.nio.NHttpServerConnection;
import org.apache.synapse.transport.nhttp.DefaultHttpGetProcessor;
import org.apache.synapse.transport.nhttp.ServerHandler;
import org.jaxen.SimpleNamespaceContext;
import org.jaxen.XPath;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.multitenancy.utils.TenantAxisUtils;
import org.wso2.carbon.core.transports.CarbonHttpRequest;
import org.wso2.carbon.core.transports.CarbonHttpResponse;
import org.wso2.carbon.core.transports.HttpGetRequestProcessor;
import org.wso2.carbon.utils.ServerConstants;
import org.wso2.micro.integrator.core.services.CarbonServerConfigurationService;

import javax.servlet.ServletException;
import javax.xml.namespace.QName;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Get Processor implementation for NHttp Transport.
 */
public class NHttpGetProcessor extends DefaultHttpGetProcessor {

    private Map<String, org.wso2.carbon.core.transports.HttpGetRequestProcessor> getRequestProcessors =
            new LinkedHashMap<String, org.wso2.carbon.core.transports.HttpGetRequestProcessor>();

    private static final QName ITEM_QN = new QName(ServerConstants.CARBON_SERVER_XML_NAMESPACE, "Item");
    private static final QName CLASS_QN = new QName(ServerConstants.CARBON_SERVER_XML_NAMESPACE, "Class");

    private static final Log log = LogFactory.getLog(NHttpGetProcessor.class);

    private void populateGetRequestProcessors() throws AxisFault {
        try {
            OMElement docEle = XMLUtils.toOM(CarbonServerConfigurationService.getInstance().getDocumentElement());
            if (docEle != null) {
                SimpleNamespaceContext nsCtx = new SimpleNamespaceContext();
                nsCtx.addNamespace("wsas", ServerConstants.CARBON_SERVER_XML_NAMESPACE);
                XPath xp = new AXIOMXPath("//wsas:HttpGetRequestProcessors/wsas:Processor");
                xp.setNamespaceContext(nsCtx);
                List nodeList = xp.selectNodes(docEle);
                for (Object aNodeList : nodeList) {
                    OMElement processorEle = (OMElement) aNodeList;
                    OMElement itemEle = processorEle.getFirstChildWithName(ITEM_QN);
                    if (itemEle == null) {
                        throw new ServletException("Required element, 'Item' not found!");
                    }
                    OMElement classEle = processorEle.getFirstChildWithName(CLASS_QN);
                    org.wso2.carbon.core.transports.HttpGetRequestProcessor processor;
                    if (classEle == null) {
                        throw new ServletException("Required element, 'Class' not found!");
                    } else {
                        processor =
                                (org.wso2.carbon.core.transports.HttpGetRequestProcessor)
                                        Class.forName(classEle.getText().trim()).newInstance();
                    }
                    getRequestProcessors.put(itemEle.getText().trim(), processor);
                }
            }
        } catch (Exception e) {
            handleException("Error populating GetRequestProcessors", e);
        }
    }

    private void processWithGetProcessor(HttpRequest request,
                                         HttpResponse response,
                                         String requestUri,
                                         String requestUrl,
                                         String queryString,
                                         String item,
                                         OutputStream outputStream,
                                         NHttpServerConnection conn) throws Exception {
        OverflowBlob temporaryData = new OverflowBlob(256, 4048, "_nhttp", ".dat");
        try {
            CarbonHttpRequest carbonHttpRequest = new CarbonHttpRequest(
                    "GET", requestUri, requestUrl);

            String uri = request.getRequestLine().getUri();
            // setting the parameters for nhttp transport
            int pos = uri.indexOf("?");
            if (pos != -1) {
                StringTokenizer st = new StringTokenizer(uri.substring(pos + 1), "&");
                while (st.hasMoreTokens()) {
                    String param = st.nextToken();
                    pos = param.indexOf("=");
                    if (pos != -1) {
                        carbonHttpRequest.setParameter(
                                param.substring(0, pos), param.substring(pos + 1));
                    } else {
                        carbonHttpRequest.setParameter(param, null);
                    }
                }
            }

            carbonHttpRequest.setContextPath(cfgCtx.getServiceContextPath());
            carbonHttpRequest.setQueryString(queryString);

            CarbonHttpResponse carbonHttpResponse = new CarbonHttpResponse(
                    temporaryData.getOutputStream());

            try {
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(TenantAxisUtils.
                        getTenantDomain(requestUrl), true);
                (getRequestProcessors.get(item)).process(carbonHttpRequest,
                        carbonHttpResponse, cfgCtx);
            } finally {
                PrivilegedCarbonContext.endTenantFlow();
            }

            // adding headers
            Map<String, String> responseHeaderMap = carbonHttpResponse.getHeaders();
            for (Object key : responseHeaderMap.keySet()) {
                Object value = responseHeaderMap.get(key);
                response.addHeader(key.toString(), value.toString());
            }

            // setting status code
            response.setStatusCode(carbonHttpResponse.getStatusCode());

            // setting error codes
            if (carbonHttpResponse.isError()) {
                if (carbonHttpResponse.getStatusMessage() != null) {
                    response.setStatusLine(response.getProtocolVersion(),
                            carbonHttpResponse.getStatusCode(),
                            carbonHttpResponse.getStatusMessage());
                } else {
                    response.setStatusLine(response.getProtocolVersion(),
                            carbonHttpResponse.getStatusCode());
                }
            }

            if (carbonHttpResponse.isRedirect()) {
                response.addHeader("Location", carbonHttpResponse.getRedirect());
                response.setStatusLine(response.getProtocolVersion(), 302);
            }

            serverHandler.commitResponseHideExceptions(conn, response);
            temporaryData.writeTo(outputStream);

            try {
                outputStream.flush();
                outputStream.close();
            } catch (Exception ignored) {}
        } finally {
            temporaryData.release();
        }
    }

    public void init(ConfigurationContext configurationContext, ServerHandler serverHandler)
            throws AxisFault {
        
        super.init(configurationContext, serverHandler);

        if (cfgCtx.getProperty("GETRequestProcessorMap") != null) {
            getRequestProcessors = (Map<String, HttpGetRequestProcessor>)
                    cfgCtx.getProperty("GETRequestProcessorMap");
        } else {
            populateGetRequestProcessors();
        }

    }

    public void process(HttpRequest request, HttpResponse response,
                        MessageContext messageContext,
                        NHttpServerConnection conn,
                        OutputStream outputStream, boolean b) {

        boolean isRequestHandled = false;


        String uri = request.getRequestLine().getUri();

        String servicePath = cfgCtx.getServiceContextPath();
        if (!servicePath.startsWith("/")) {
            servicePath = "/" + servicePath;
        }
        String serviceName = getServiceName(request);

        boolean loadBalancer = Boolean.parseBoolean(System.getProperty("wso2.loadbalancer", "false"));
        if (uri.equals("/favicon.ico")) {
            response.setStatusCode(HttpStatus.SC_MOVED_PERMANENTLY);
            response.addHeader("Location", "http://wso2.org/favicon.ico");
            serverHandler.commitResponseHideExceptions(conn, response);
            isRequestHandled = true;
        } else if(uri.startsWith(servicePath) &&
                (serviceName == null || serviceName.length() == 0)){
            //check if service listing request is blocked
            if (isServiceListBlocked(uri)) {
                response.setStatusCode(HttpStatus.SC_FORBIDDEN);
                serverHandler.commitResponseHideExceptions(conn,  response);
            } else{
                generateServicesList(response, conn, outputStream, servicePath);
            }
            try {
                outputStream.flush();
                outputStream.close();
            } catch (IOException ignore) {
            }
            isRequestHandled = true ;
        } else {
            int pos = uri.indexOf('?');
            if (pos != -1) {
                String queryString = uri.substring(pos + 1);
                String requestUri = uri.substring(0, pos);
                String requestUrl = uri;
                if (requestUri.indexOf("://") == -1) {
                    HttpInetConnection inetConn = (HttpInetConnection) conn;

                    String hostName = "localhost";
                    CarbonServerConfigurationService serverConfig = CarbonServerConfigurationService.getInstance();
                    if (serverConfig.getFirstProperty("HostName") != null) {
                        hostName = serverConfig.getFirstProperty("HostName");
                    }

                    requestUrl = "http://" +
                            hostName + ":" + inetConn.getLocalPort() + requestUri;
                }

                String contextPath = cfgCtx.getServiceContextPath();
                int beginIndex = -1;
                if (requestUri.indexOf(contextPath) != -1) {
                    beginIndex = requestUri.indexOf(contextPath) + contextPath.length() + 1;
                }

                /**
                 * This   reverseProxyMode was introduce to avoid LB exposing it's own services when invoked through rest call.
                 * For a soap call this works well. But for a rest call this does not work as intended. in LB it has to set system property "reverseProxyMode"
                 *
                 */
                boolean reverseProxyMode = Boolean.parseBoolean(System.getProperty("reverseProxyMode"));
                AxisService axisService = null;
                if (!reverseProxyMode) {
                    if (!(beginIndex < 0 || beginIndex > requestUri.length())) {
                        serviceName = requestUri.substring(beginIndex);
                        axisService = cfgCtx.getAxisConfiguration().getServiceForActivation(serviceName);
                    }

                    if (axisService == null && !loadBalancer && serviceName != null) {
                        // Try to see whether the service is available in a tenant
                        try {
                            String tenantDomain = TenantAxisUtils.getTenantDomain(uri);
                            PrivilegedCarbonContext.startTenantFlow();
                            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
                            axisService = TenantAxisUtils.getAxisService(serviceName, cfgCtx);
                        } catch (AxisFault axisFault) {
                            axisFault.printStackTrace();
                        } finally {
                            PrivilegedCarbonContext.endTenantFlow();
                        }
                    }
                }

                if (queryString != null) {
                    for (String item : getRequestProcessors.keySet()) {
                        if (queryString.indexOf(item) == 0 &&
                                (queryString.equals(item) ||
                                        queryString.indexOf("&") == item.length() ||
                                        queryString.indexOf("=") == item.length())) {
                            if (axisService == null) {
                                continue;
                            }

                            try {
                                processWithGetProcessor(request, response, requestUri,
                                        requestUrl, queryString,
                                        item, outputStream, conn);
                            } catch (Exception e) {
                                handleBrowserException(response, conn, outputStream,
                                        "Error processing request", e);
                            }
                            isRequestHandled = true;
                            break;
                        }
                    }
                }
            }
        }

        if (!isRequestHandled) {
            processGetAndDelete(request, response, messageContext,
                    conn, outputStream, "GET", b);
        }
    }

    public void handleException(String msg, Exception e) throws AxisFault {
        log.error(msg, e);
        throw new AxisFault(msg, e);
    }
}
