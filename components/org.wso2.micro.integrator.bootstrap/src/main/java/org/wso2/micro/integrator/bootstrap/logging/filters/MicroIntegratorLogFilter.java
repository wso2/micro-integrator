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
package org.wso2.micro.integrator.bootstrap.logging.filters;

import java.util.logging.Filter;
import java.util.logging.LogRecord;

/**
 * Used for publishing logs using Java Util Logging in non-OSGi mode.
 * This publishes patch information and the last two logs printed after OSGi environment is shutdown
 */
public class MicroIntegratorLogFilter implements Filter {

    private static final String ORG_WSO2_CARBON_SERVER = "org.wso2.micro.integrator.server";
    private static final String ORG_WSO2_CARBON_CORE_INIT_CARBONSERVERMANAGER = "org.wso2.micro.core.init";

    /**
     * Filters out the logs printed before OSGi is started and after OSGi is stopped.
     *
     * @param logRecord the logRecord object
     * @return boolean true/false
     */
    @Override
    public boolean isLoggable(LogRecord logRecord) {
        return (logRecord.getLoggerName().contains(ORG_WSO2_CARBON_SERVER) || logRecord.getLoggerName()
                .contains(ORG_WSO2_CARBON_CORE_INIT_CARBONSERVERMANAGER));
    }
}
