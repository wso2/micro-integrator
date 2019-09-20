/*                                                                             
 * Copyright 2004,2005 The Apache Software Foundation.                         
 *                                                                             
 * Licensed under the Apache License, Version 2.0 (the "License");             
 * you may not use this file except in compliance with the License.            
 * You may obtain a copy of the License at                                     
 *                                                                             
 *      http://www.apache.org/licenses/LICENSE-2.0                             
 *                                                                             
 * Unless required by applicable law or agreed to in writing, software         
 * distributed under the License is distributed on an "AS IS" BASIS,           
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.    
 * See the License for the specific language governing permissions and         
 * limitations under the License.                                              
 */
package org.wso2.micro.core.util;

import org.apache.axis2.context.ConfigurationContext;

/**
 * This observer will be notified when a new Axis2 ConfigurationContext is created & populated
 * or destroyed
 */
public interface Axis2ConfigurationContextObserver {

    /**
     * This method will be notified just before creating a new ConfigurationContext for any tenant
     *
     * @param tenantId The ID of the tenant
     */
    void creatingConfigurationContext(int tenantId);

    /**
     * This method will be notified after a new Axis2 ConfigurationContext is created
     *
     * @param configContext  The newly created ConfigurationContext
     */
    void createdConfigurationContext(ConfigurationContext configContext);

     /**
     * Notification before a ConfigurationContext is terminated
     *
     * @param configCtx The ConfigurationContext which will be terminated
     */
    void terminatingConfigurationContext(ConfigurationContext configCtx);

    /**
     * Notification after a ConfigurationContext has been terminated
     *
     * @param configCtx The ConfigurationContext which was terminated
     */
    void terminatedConfigurationContext(ConfigurationContext configCtx);
}
