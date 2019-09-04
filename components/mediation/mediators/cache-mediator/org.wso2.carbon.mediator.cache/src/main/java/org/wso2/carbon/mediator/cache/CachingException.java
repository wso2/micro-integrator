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

package org.wso2.carbon.mediator.cache;

/**
 * This is the CachingException which is a checked exception and will be thrown in case of
 * an error in the caching execution
 *
 * @see Exception
 */
public class CachingException extends RuntimeException {

    /**
     * Default constructor of the CachingException
     */
    public CachingException() {
        super();
    }

    /**
     * This constructor of the CachingException sets the given String message to the Exception
     *
     * @param message - String specifying the exception message
     */
    public CachingException(String message) {
        super(message);
    }

    /**
     * This constructor of the CachingException sets the given String message, and the
     * cause to the Exception
     * 
     * @param message - String specifying the exception message
     * @param cause - Throwable cause of the exception
     */
    public CachingException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * This constructor of the CachingException sets the given cause to the Exception
     *
     * @param cause - Throwable cause of the exception
     */
    public CachingException(Throwable cause) {
        super(cause);
    }
}
