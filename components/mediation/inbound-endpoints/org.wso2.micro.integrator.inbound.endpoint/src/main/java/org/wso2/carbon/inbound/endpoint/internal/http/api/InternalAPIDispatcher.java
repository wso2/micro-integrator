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
package org.wso2.carbon.inbound.endpoint.internal.http.api;

import org.apache.axis2.Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.api.cors.CORSHelper;
import org.apache.synapse.api.dispatch.DispatcherHelper;
import org.apache.synapse.api.dispatch.URITemplateHelper;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.RESTConstants;
import org.apache.synapse.rest.RESTUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * {@code InternalAPIDispatcher} takes care of dispatching messages received over the internal inbound endpoint into
 * relevant {@link InternalAPI}.
 */
public class InternalAPIDispatcher {

    private static Log log = LogFactory.getLog(InternalAPIDispatcher.class);

    private List<InternalAPI> internalApis;

    public InternalAPIDispatcher(List<InternalAPI> internalApis) {
        this.internalApis = internalApis;
    }

    /**
     * Dispatches the message into relevant internal API.
     *
     * @param synCtx the Synapse Message Context
     * @return whether to continue with post dispatching actions
     */
    public boolean dispatch(MessageContext synCtx) {

        String path = RESTUtils.getFullRequestPath(synCtx);
        InternalAPI internalApi = findAPI(path);
        if (internalApi == null) {
            log.warn("No Internal API found to dispatch the message to : " + path);
            return false;
        }
        // check null for internal apis' CORS configuration where CORS configurations are not set
        if (!Objects.isNull(internalApi.getCORSConfiguration())) {
            CORSHelper.handleCORSHeaders(internalApi.getCORSConfiguration(), synCtx, getSupportedMethodsForInternalApis(), true);
        }
        if (isOptions(synCtx)) {
            return true;
        }

        List<InternalAPIHandler> handlerList = internalApi.getHandlers();
        // check null for apis' where handlers are not set
        if (handlerList != null) {
            for (InternalAPIHandler handler : handlerList) {
                Boolean success = handler.invoke(synCtx);
                if (!success) {
                    return false;
                }
            }
        }
        APIResource resource = findResource(synCtx, internalApi);
        if (resource == null) {
            log.warn("No matching Resource found in " + internalApi.getName() + " InternalAPI to dispatch the message");
            return false;
        }
        return resource.invoke(synCtx);
    }

    /* Finds the API that the message should be dispatched to */
    private InternalAPI findAPI(String path) {
        for (InternalAPI internalApi : internalApis) {
            String context = internalApi.getContext();
            if (path.startsWith(context + "/") || path.startsWith(context + "?") || context.equals(path)) {
                return internalApi;
            }
        }
        return null;
    }

    /* Finds the Resource that the message should be dispatched to */
    private APIResource findResource(MessageContext synCtx, InternalAPI internalApi) {

        org.apache.axis2.context.MessageContext axis2Ctx = ((Axis2MessageContext) synCtx).getAxis2MessageContext();
        String method = (String) axis2Ctx.getProperty(Constants.Configuration.HTTP_METHOD);

        String path = (String) synCtx.getProperty(RESTConstants.REST_FULL_REQUEST_PATH);
        String subPath = path.substring(internalApi.getContext().length());
        if ("".equals(subPath)) {
            subPath = "/";
        }

        for (APIResource resource : internalApi.getResources()) {
            if (!resource.getMethods().contains(method)) {
                continue;
            }
            DispatcherHelper helper = resource.getDispatcherHelper();
            URITemplateHelper templateHelper = (URITemplateHelper) helper;
            Map<String, String> variables = new HashMap<>();
            if (templateHelper.getUriTemplate().matches(subPath, variables)) {
                for (Map.Entry<String, String> entry : variables.entrySet()) {
                    synCtx.setProperty(RESTConstants.REST_URI_VARIABLE_PREFIX + entry.getKey(), entry.getValue());
                }
                RESTUtils.populateQueryParamsToMessageContext(synCtx);
                return resource;
            }
        }
        return null;
    }

    private String getSupportedMethodsForInternalApis() {

        return RESTConstants.REST_ALL_SUPPORTED_METHODS + ",OPTIONS, PATCH";
    }

    private Boolean isOptions(MessageContext synCtx) {
        org.apache.axis2.context.MessageContext axis2Ctx = ((Axis2MessageContext) synCtx).getAxis2MessageContext();
        String method = (String) axis2Ctx.getProperty(Constants.Configuration.HTTP_METHOD);
        if (method.contains(RESTConstants.METHOD_OPTIONS)) {
            return true;
        }
        return false;
    }
}
