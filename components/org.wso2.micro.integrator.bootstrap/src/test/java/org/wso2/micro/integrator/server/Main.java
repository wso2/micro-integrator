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
package org.wso2.micro.integrator.server;

/**
 * Test org.wso2.carbon.server.Main class for testing Bootstrap class.
 * <p>
 * This is used to avoid the cyclic dependency created when adding the
 * dependency to org.wso2.carbon.server module
 */
public class Main {

    /**
     * Sets a system property to indicate the launch of the Carbon server.
     *
     * @param args command line arguments.
     */
    public static void main(String[] args) {
        System.setProperty("carbon.server.status", "up");
    }
}
