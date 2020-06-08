/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.micro.integrator.management.apis;

import org.apache.axis2.Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.config.SynapseConfiguration;
import org.json.JSONObject;
import org.wso2.micro.core.util.StringUtils;
import org.wso2.micro.integrator.initializer.handler.DataHolder;
import org.wso2.micro.integrator.initializer.handler.transaction.exception.TransactionCounterException;
import org.wso2.micro.integrator.initializer.handler.transaction.store.TransactionStore;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static org.wso2.micro.integrator.management.apis.Constants.BAD_REQUEST;
import static org.wso2.micro.integrator.management.apis.Constants.FORBIDDEN;
import static org.wso2.micro.integrator.management.apis.Constants.INTERNAL_SERVER_ERROR;


/**
 * Resource for a retrieving aggregated request count.
 * <p>
 * Handles resources in the form "management/transactions and management/transactions?year=2020&month=5"
 */
public class RequestCountResource implements MiApiResource {

    private static final Log LOG = LogFactory.getLog(RequestCountResource.class);

    @Override
    public Set<String> getMethods() {

        Set<String> methods = new HashSet<>();
        methods.add("GET");
        return methods;
    }

    @Override
    public boolean invoke(MessageContext synCtx,
                          org.apache.axis2.context.MessageContext axis2MessageContext,
                          SynapseConfiguration synapseConfiguration) {

        String yearParameter = Utils.getQueryParameter(synCtx, "year");
        String monthParameter = Utils.getQueryParameter(synCtx, "month");

        if (tryParseInt(yearParameter) != null && tryParseInt(monthParameter) != null) {
            if (!StringUtils.isEmpty(yearParameter) && !StringUtils.isEmpty(monthParameter)) {
                return takeRequestCountOfTheMonth(axis2MessageContext, Integer.parseInt(yearParameter),
                                                  Integer.parseInt(monthParameter));
            }
        } else if (StringUtils.isEmpty(yearParameter) && StringUtils.isEmpty(monthParameter)) {
            Date date = new Date();
            LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            return takeRequestCountOfTheMonth(axis2MessageContext, localDate.getYear(), localDate.getMonthValue());
        }
        JSONObject response = Utils.createJsonError("Input parameters are not valid", axis2MessageContext, BAD_REQUEST);
        Utils.setJsonPayLoad(axis2MessageContext, response);
        return true;
    }

    // Take aggregated request count for a given month.
    private boolean takeRequestCountOfTheMonth(org.apache.axis2.context.MessageContext axisCtx, int year, int month) {

        JSONObject response;
        axisCtx.setProperty(org.apache.axis2.Constants.Configuration.MESSAGE_TYPE, "application/json");
        axisCtx.setProperty(Constants.Configuration.CONTENT_TYPE, "application/json");

        TransactionStore transactionStore = DataHolder.getInstance().getTransactionStore();
        if (transactionStore != null) {
            // Get request count of current Month
            long requestCount;
            try {
                requestCount = transactionStore.getTransactionCountOfMonth(year, month);
            } catch (TransactionCounterException e) {
                response =
                        Utils.createJsonError("Error occurred while retrieving data from the database ", e, axisCtx,
                                              INTERNAL_SERVER_ERROR);
                Utils.setJsonPayLoad(axisCtx, response);
                return true;
            }
            if (requestCount != -1L) {
                response = new JSONObject(
                        "{\"Year\" : " + year + ", \"Month\" : " + month + ", \"RequestCount\" : " + requestCount +
                                "}");
            } else {
                response = new JSONObject("{\"error\" : \"Did not find stats for " + year + "/" + month + "\"}");
            }
        } else {
            response =
                    Utils.createJsonError("TransactionStore is not initialized", axisCtx, FORBIDDEN);
        }
        Utils.setJsonPayLoad(axisCtx, response);
        return true;
    }

    /**
     * Try to parse input string to an Integer.
     *
     * @param someText input string.
     * @return parsed integer, null if failed.
     */
    private static Integer tryParseInt(String someText) {

        try {
            return Integer.parseInt(someText);
        } catch (NumberFormatException ex) {
            LOG.error("Invalid input. Cannot parse the input '" + someText + "' as an Integer: ", ex);
            return null;
        }
    }
}
