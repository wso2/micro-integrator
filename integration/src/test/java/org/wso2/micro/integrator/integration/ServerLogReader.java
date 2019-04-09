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

package org.wso2.micro.integrator.integration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

public class ServerLogReader implements Runnable {
    private String streamType;
    private InputStream inputStream;
    private StringBuilder stringBuilder;
    private final Object lock = new Object();
    private volatile boolean running = true;
    private static final Logger log = LoggerFactory.getLogger(ServerLogReader.class);

    public ServerLogReader(String name, InputStream is) {
        this.streamType = name;
        this.inputStream = is;
        this.stringBuilder = new StringBuilder();
    }

    public void start() {
        Thread thread = new Thread(this);
        thread.start();
    }

    public void stop() {
        this.running = false;
    }

    public void run() {
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;

        try {
            inputStreamReader = new InputStreamReader(this.inputStream, Charset.defaultCharset());
            bufferedReader = new BufferedReader(inputStreamReader);

            while(this.running) {
                if (bufferedReader.ready()) {
                    String s = bufferedReader.readLine();
                    if (s == null) {
                        break;
                    }

                    if ("inputStream".equals(this.streamType)) {
                        this.stringBuilder.append(s).append("\n");
                        log.info(s);
                    } else if ("errorStream".equals(this.streamType)) {
                        this.stringBuilder.append(s).append("\n");
                        log.error(s);
                    }
                }
                else {
                    TimeUnit.MILLISECONDS.sleep(1);
                }
            }
        } catch (Exception var16) {
            log.error("Problem reading the [" + this.streamType + "] due to: " + var16.getMessage(), var16);
        } finally {
            if (inputStreamReader != null) {
                try {
                    this.inputStream.close();
                    inputStreamReader.close();
                } catch (IOException var15) {
                    log.error("Error occurred while closing the server log stream: " + var15.getMessage(), var15);
                }
            }

            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException var14) {
                    log.error("Error occurred while closing the server log stream: " + var14.getMessage(), var14);
                }
            }
        }
    }

    public String getOutput() {
        synchronized(this.lock) {
            return this.stringBuilder.toString();
        }
    }
}
