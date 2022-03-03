/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.micro.integrator.management.apis.security.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.rest.RESTConstants;
import org.wso2.carbon.inbound.endpoint.internal.http.api.InternalAPIHandler;
import org.wso2.micro.core.util.CarbonException;
import org.wso2.micro.integrator.management.apis.Constants;
import org.wso2.micro.integrator.management.apis.ManagementApiUndefinedException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.xml.stream.XMLStreamException;

/**
 * This class provides an abstraction for all security handlers for management api.
 */
public abstract class SecurityHandlerAdapter implements InternalAPIHandler {

    protected static boolean useCarbonUserStore = false;
    private static boolean isInitialized = false;
    /**
     * Resources defined in internal-apis.xml to be handled
     */
    protected List<String> resources;

    /**
     * default resource paths to be handled in the case where resource paths are not defined in internal-apis.xml
     */
    protected List<String> defaultResources;
    protected String context;

    private static final Log LOG = LogFactory.getLog(SecurityHandlerAdapter.class);

    public SecurityHandlerAdapter(String context) throws CarbonException, XMLStreamException, IOException,
            ManagementApiUndefinedException {
        initializeUserStore();
        this.context = context;
        populateDefaultResources();
    }

    protected SecurityHandlerAdapter() {
    }

    private static void initializeUserStore() throws CarbonException, IOException, ManagementApiUndefinedException,
            XMLStreamException {
        if (!isInitialized) {
            if (SecurityUtils.isFileBasedUserStoreEnabled()) {
                isInitialized = FileBasedUserStoreManager.getUserStoreManager().isInitialized();
            } else {
                LOG.info("File based user store has been disabled. Carbon user store settings will be used.");
                useCarbonUserStore = true;
                isInitialized = true;
            }
        }
    }

    protected boolean needsHandling(MessageContext messageContext) {

        String resourcePath = messageContext.getTo().getAddress();
        if (Constants.REST_API_CONTEXT.equals(resourcePath)) {
            LOG.debug("Authentication is skipped for management api root context.");
            return false;
        }
        if (!resources.isEmpty()) {
            return isMatchingResource(resourcePath, resources);
        }
        return isMatchingResource(resourcePath, defaultResources);
    }

    private boolean isMatchingResource(String resourcePath, List<String> defaultResources) {
        for (String resource : defaultResources) {
            if (resourcePath.startsWith(context.concat(resource))) {
                return true;
            }
        }
        return false;
    }

    protected void populateDefaultResources() {
        defaultResources = new ArrayList<>(1);
        defaultResources.add("");
    }

    @Override
    public void setResources(List<String> resources) {
        this.resources = resources;
    }

    @Override
    public List<String> getResources() {
        return resources;
    }

    @Override
    public Boolean invoke(MessageContext messageContext) {
        if (needsHandling(messageContext)) {
            return handle(messageContext);
        } else {
            return true;
        }
    }

    /**
     * Executes the handling logic relevant to the handler.
     *
     * @param messageContext the message context for the incoming request
     * @return Boolean returns true if the request is allowed
     */
    protected abstract Boolean handle(MessageContext messageContext);

    /**
     * Clear headers map preserving cors headers
     * @param headers msg ctx headers map
     * @return cors headers preserved header map
     */
    public Map clearHeaders(Map headers) {

        Object allowOriginCorsHeader = headers.get(RESTConstants.CORS_HEADER_ACCESS_CTL_ALLOW_ORIGIN);
        Object allowMethodsCorsHeader = headers.get(RESTConstants.CORS_HEADER_ACCESS_CTL_ALLOW_METHODS);
        Object allowHeadersCorsHeader = headers.get(RESTConstants.CORS_HEADER_ACCESS_CTL_ALLOW_HEADERS);
        headers.clear();
        if (allowOriginCorsHeader != null) {
            headers.put(RESTConstants.CORS_HEADER_ACCESS_CTL_ALLOW_ORIGIN, allowOriginCorsHeader);
        }
        if (allowMethodsCorsHeader != null) {
            headers.put(RESTConstants.CORS_HEADER_ACCESS_CTL_ALLOW_METHODS, allowMethodsCorsHeader);
        }
        if (allowHeadersCorsHeader != null) {
            headers.put(RESTConstants.CORS_HEADER_ACCESS_CTL_ALLOW_HEADERS, allowHeadersCorsHeader);
        }
        return headers;
    }

}
