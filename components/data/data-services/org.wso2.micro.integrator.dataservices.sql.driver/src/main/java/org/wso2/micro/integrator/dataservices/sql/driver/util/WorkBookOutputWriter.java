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

package org.wso2.micro.integrator.dataservices.sql.driver.util;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * This is the helper class when we need to write workbook data to registry, Registry needs input stream with the
 * data, so this class will write to a output stream which will be read by registry at the end.
 */
public class WorkBookOutputWriter extends Thread {
    private static final Log log = LogFactory.getLog(WorkBookOutputWriter.class);
    private Workbook workbook;
    private OutputStream outputStream;
    public WorkBookOutputWriter(Workbook workbook, OutputStream outputStream) {
        this.workbook = workbook;
        this.outputStream = outputStream;
    }
    @Override
    public void run() {
        try {
            workbook.write(outputStream);
        } catch (IOException e) {
            log.error("Error saving excel data to registry, Error - " + e.getMessage(), e);
        } finally {
            try {
                outputStream.close();
            } catch (IOException e) {
                //ignore
            }
        }
    }
}
