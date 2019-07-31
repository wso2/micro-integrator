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

package org.wso2.esb.integration.common.utils;

import org.apache.commons.io.input.TailerListenerAdapter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

class CarbonLogTailer extends TailerListenerAdapter {

    private static final Log LOG = LogFactory.getLog(CarbonLogTailer.class);
    private StringBuilder stringBuilder;

    CarbonLogTailer() {
        super();
        stringBuilder = new StringBuilder();
    }

    @Override
    public void handle(String s) {
        stringBuilder.append(s);
    }

    @Override
    public void fileNotFound() {
        LOG.error("The carbon log file is not present to read logs.");
    }

    @Override
    public void handle(Exception ex) {
        LOG.error("Exception occurred while reading the logs.", ex);
    }

    String getCarbonLogs() {
        return stringBuilder.toString();
    }

    void clearLogs() {
        this.stringBuilder = new StringBuilder();
    }

}