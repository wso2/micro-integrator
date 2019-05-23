/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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


package org.wso2.carbon.micro.integrator.management.apis;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.synapse.commons.json.JsonUtil;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;

public class Utils {

    private static Log log = LogFactory.getLog(Utils.class);

    /**
     * Gives a List of query parameters
     *
     * @param axis2MessageContext
     * @return List<NameValuePair> - List of query parameters
     *
     */
    public static List<NameValuePair> getQueryParameters(org.apache.axis2.context.MessageContext axis2MessageContext){

        List<NameValuePair> queryParameter = null;

        // extract the query parameters from the Url
        try {
            queryParameter = URLEncodedUtils.parse(new URI((String) axis2MessageContext.getProperty(
                    Constants.Configuration.TRANSPORT_IN_URL)), "UTF-8");
        } catch (URISyntaxException e) {
            log.error("Error occurred while processing query parameters", e);
        }

        if (Objects.nonNull(queryParameter) && !queryParameter.isEmpty()) {
            return queryParameter;
        }
        return null;
    }

    public static void setJsonPayLoad(org.apache.axis2.context.MessageContext axis2MessageContext, JSONObject payload){

        try {
            JsonUtil.getNewJsonPayload(axis2MessageContext, payload.toString(),  true, true);
        } catch (AxisFault axisFault) {
            log.error("Error occurred while setting json payload", axisFault);
        }
        axis2MessageContext.setProperty("messageType", "application/json");
        axis2MessageContext.setProperty("ContentType", "application/json");
    }

}
