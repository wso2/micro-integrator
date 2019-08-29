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

package org.wso2.micro.integrator.core.internal;

/**
 * Class for capturing any type of exception that occurs when using the Server Configuration.
 */
public class MicroIntegratorConfigurationException extends Exception {
    /**
     * Constructs a new exception with the cause.
     *
     * @param cause the cause of this exception.
     */
    public MicroIntegratorConfigurationException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new exception.
     */
    public MicroIntegratorConfigurationException() {
    }

    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param message the detail message.
     */
    public MicroIntegratorConfigurationException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified detail message and cause.
     *
     * @param message the detail message.
     * @param cause   the cause of this exception.
     */
    public MicroIntegratorConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
