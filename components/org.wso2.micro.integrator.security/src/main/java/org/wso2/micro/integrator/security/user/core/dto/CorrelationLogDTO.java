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

package org.wso2.micro.integrator.security.user.core.dto;

import java.util.Hashtable;

/**
 * Dto used when during creating timing logs for ldap calls
 */
public class CorrelationLogDTO {

    private long startTime;
    private long delta;
    private Hashtable<?, ?> environment;
    private String methodName;
    private int argsLength;
    private String args;

    public long getStartTime() {

        return startTime;
    }

    public void setStartTime(long startTime) {

        this.startTime = startTime;
    }

    public long getDelta() {

        return delta;
    }

    public void setDelta(long delta) {

        this.delta = delta;
    }

    public Hashtable<?, ?> getEnvironment() {

        return environment;
    }

    public void setEnvironment(Hashtable<?, ?> environment) {

        this.environment = environment;
    }

    public String getMethodName() {

        return methodName;
    }

    public void setMethodName(String methodName) {

        this.methodName = methodName;
    }

    public int getArgsLength() {

        return argsLength;
    }

    public void setArgsLength(int argsLength) {

        this.argsLength = argsLength;
    }

    public String getArgs() {

        return args;
    }

    public void setArgs(String args) {

        this.args = args;
    }
}
