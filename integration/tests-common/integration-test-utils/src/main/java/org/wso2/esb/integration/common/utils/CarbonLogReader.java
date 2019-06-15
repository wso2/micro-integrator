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

import org.apache.commons.io.input.Tailer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class CarbonLogReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(CarbonLogReader.class);
    private CarbonLogTailer tailerListener;
    private Tailer tailer;
    private File carbonLogFile;

    public CarbonLogReader() {

        tailerListener = new CarbonLogTailer();
        carbonLogFile = new File(
                System.getProperty("carbon.home") + File.separator + "repository" + File.separator + "logs"
                        + File.separator + "wso2carbon.log");
    }

    public void start() {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Starting to tail carbon logs from : " + carbonLogFile.getPath());
        }
        tailer = new Tailer(carbonLogFile, tailerListener, 1);
        Thread thread = new Thread(tailer);
        thread.setDaemon(true);
        thread.start();
    }

    public String getLogs() {
        return tailerListener.getCarbonLogs();
    }

    public void stop() {
        LOGGER.debug("Stopped tailing carbon logs.");
        tailer.stop();
    }

}
