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
package org.wso2.micro.integrator.security.user.core.tracker;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;
import org.wso2.micro.integrator.security.user.api.Properties;
import org.wso2.micro.integrator.security.user.api.UserStoreManager;
import org.wso2.micro.integrator.security.user.core.internal.UserStoreMgtDSComponent;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class UserStoreManagerRegistry extends UserStoreMgtDSComponent {
    private static Log log = LogFactory.getLog(UserStoreManagerRegistry.class);
    private static ServiceTracker userStoreManagerTracker;

    public static void init(BundleContext bc) throws Exception {
        if(userStoreManagerTracker != null) {
            // Log an error as there is no need to call this other than UserStoreMgtDSComponent. We can not change the
            // signature or behavior as it constitute an API change.
            log.error("UserStoreManagerRegistry init called more than once, with trace, ", new Throwable());
        }
        try {
            userStoreManagerTracker = new ServiceTracker(bc, UserStoreManager.class.getName(), null);
            userStoreManagerTracker.open();
            if (log.isDebugEnabled()) {
                log.debug(userStoreManagerTracker.getServices().length + " User Store Managers registered.");
            }
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.error("Error" + e.getMessage(), e);
            }
            throw e;
        }
    }


    /**
     * Get all the available user store manager implementations
     *
     * @return Map<Class,<Map<Property,Value>>
     */
    private static Map<String, Properties> getUserStoreManagers() {

        Map<String, Properties> userStoreManagers = new HashMap<>();
        Object[] objects = userStoreManagerTracker.getServices();
        int length = objects.length;
        UserStoreManager userStoreManager;
        Properties userStoreProperties;

        for (int i = 0; i < length; i++) {
            userStoreManager = (UserStoreManager) objects[i];
            if (userStoreManager.getDefaultUserStoreProperties() != null) {
                userStoreProperties = userStoreManager.getDefaultUserStoreProperties();
                userStoreManagers.put(userStoreManager.getClass().getName(), userStoreProperties);
                if (log.isDebugEnabled()) {
                    log.debug(
                            "Adding UserStoreManager with name: " + userStoreManager.getClass().getName()+
                                    ". UserStoreManager class: " + userStoreManager);
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug(
                            "The user store manager has no DefaultUserStoreProperties, " +
                                    "and will be skipped. UserStoreManager : " + userStoreManager);
                }
            }

        }
        return userStoreManagers;
    }

    /**
     * Get all available user store manager implementations
     *
     * @return
     */
    public static Set<String> getUserStoreManagerClasses() {
        Set<String> classes;
        classes = getUserStoreManagers().keySet();
        return classes;
    }

    /**
     * Get the list of properties required by the user store manager
     *
     * @param className :name of implementation class of user store manager
     * @return
     */
    public static Properties getUserStoreProperties(String className) {
        Properties properties;
        properties = getUserStoreManagers().get(className);
        return properties;
    }
}
