/*
 *  Copyright (c) 2005-2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.micro.core.queueing;

/**
 * A queue manager for carbon.
 */
@SuppressWarnings("unused")
public class CarbonQueueManager {

    private static org.wso2.micro.core.queueing.CarbonQueueManager instance = null;
    private static final Object lock = new Object();

    /**
     * Returns the singleton CarbonQueueManager
     *
     * @return the instance or null if an instance has not been set.
     */
    public static org.wso2.micro.core.queueing.CarbonQueueManager getInstance() {
        return instance;
    }

    /**
     * Returns the singleton CarbonQueueManager
     *
     * @param instance the CarbonQueueManager instance to set.
     */
    public static void setInstance(org.wso2.micro.core.queueing.CarbonQueueManager instance) {
        synchronized (lock) {
            if (org.wso2.micro.core.queueing.CarbonQueueManager.instance != null) {
                throw new RuntimeException("A queue manager instance has already been set.");
            }
            org.wso2.micro.core.queueing.CarbonQueueManager.instance = instance;
        }
    }

    /**
     * Method to obtain a named queue.
     *
     * @param name the name of the queue.
     *
     * @return the corresponding queue.
     */
    public CarbonQueue<?> getQueue(String name) {
        if (instance == null) {
            return null;
        }
        return instance.getQueue(name);
    }

}
