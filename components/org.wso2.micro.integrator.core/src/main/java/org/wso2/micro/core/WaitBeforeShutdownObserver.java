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
package org.wso2.micro.core;

/**
 * This interface must be implemented by classes (mainly threads etc), that require notification
 * and waiting before the shutdown sequence commences in the process of a graceful shutdown.
 */
public interface WaitBeforeShutdownObserver {

    /**
     * This method will be invoked on all running components before shutdown happens.
     */
    void startingShutdown();

    /**
     * The server will wait for all tasks to complete before shutting down.
     *
     * @return true if the server needs to wait or false if not.
     */
    boolean isTaskComplete();

}
