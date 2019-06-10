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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.rest.RESTConstants;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Objects;

public class Utils {

    private static Log log = LogFactory.getLog(Utils.class);

    public static String getQueryParameter(MessageContext messageContext, String key){
        if (Objects.nonNull(messageContext.getProperty(RESTConstants.REST_QUERY_PARAM_PREFIX + key))){
            return messageContext.getProperty(RESTConstants.REST_QUERY_PARAM_PREFIX + key).toString();
        }
        return null;
    }

    public static void setJsonPayLoad(org.apache.axis2.context.MessageContext axis2MessageContext, JSONObject payload){

        try {
            JsonUtil.getNewJsonPayload(axis2MessageContext, payload.toString(),  true, true);

        } catch (AxisFault axisFault) {
            axis2MessageContext.setProperty(Constants.HTTP_STATUS_CODE, Constants.INTERNAL_SERVER_ERROR);
            log.error("Error occurred while setting json payload", axisFault);
        }
        axis2MessageContext.setProperty("messageType", "application/json");
        axis2MessageContext.setProperty("ContentType", "application/json");
    }

    public static JSONObject createJSONList(int count) {
        JSONObject jsonBody = new JSONObject();
        JSONArray list = new JSONArray();
        jsonBody.put(Constants.COUNT, count);
        jsonBody.put(Constants.LIST, list);
        return jsonBody;
    }

    public static JSONObject createJsonErrorObject(String error) {
        JSONObject errorObject =  new JSONObject();
        errorObject.put("Error", error);
        return errorObject;
    }

    public static boolean isDoingPOST(org.apache.axis2.context.MessageContext axis2MessageContext) {
        if (Constants.HTTP_POST.equals(axis2MessageContext.getProperty(Constants.HTTP_METHOD_PROPERTY))) {
            return true;
        }
        return false;
    }
}
