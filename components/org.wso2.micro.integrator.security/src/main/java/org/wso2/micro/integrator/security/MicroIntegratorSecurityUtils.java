/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.micro.integrator.security;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.micro.integrator.security.internal.DataHolder;
import org.wso2.micro.integrator.security.internal.ServiceComponent;
import org.wso2.micro.integrator.security.user.api.RealmConfiguration;
import org.wso2.micro.integrator.security.user.api.UserStoreException;
import org.wso2.micro.integrator.security.user.api.UserStoreManager;
import org.wso2.micro.integrator.security.user.core.UserRealm;
import org.wso2.micro.integrator.security.user.core.claim.ClaimManager;
import org.wso2.micro.integrator.security.user.core.profile.ProfileConfigurationManager;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Map;

/**
 * This class contains utils required by the micro integrator security component.
 */
public class MicroIntegratorSecurityUtils {

    private static Log log = LogFactory.getLog(MicroIntegratorSecurityUtils.class);

    /**
     * This method initializes the user store manager class
     *
     * @param className   class name of the user store manager class
     * @param realmConfig realm configuration defined
     * @return initialized UserStoreManager class
     * @throws UserStoreException
     */
    public static Object createObjectWithOptions(String className, RealmConfiguration realmConfig) throws UserStoreException {
        /*
            Since different User Store managers contain constructors requesting different sets of arguments, this method
            tries to invoke the constructor with different combinations of arguments
         */
        Class[] initClassOpt0 = new Class[]{RealmConfiguration.class, Map.class, ClaimManager.class,
                ProfileConfigurationManager.class, UserRealm.class, Integer.class, boolean.class};
        Object[] initObjOpt0 = new Object[]{realmConfig, new Hashtable<String, Object>(), null, null, null, -1234, true};
        Class[] initClassOpt1 = new Class[]{RealmConfiguration.class, ClaimManager.class, ProfileConfigurationManager.class};
        Object[] initObjOpt1 = new Object[]{realmConfig, null, null};
        Class[] initClassOpt2 = new Class[]{RealmConfiguration.class, int.class};
        Object[] initObjOpt2 = new Object[]{realmConfig, -1234};
        Class[] initClassOpt3 = new Class[]{RealmConfiguration.class};
        Object[] initObjOpt3 = new Object[]{realmConfig};
        Class[] initClassOpt4 = new Class[]{};
        Object[] initObjOpt4 = new Object[]{};
        try {
            Class clazz = Class.forName(className);
            Object newObject = null;
            if (log.isDebugEnabled()) {
                log.debug("Start initializing the UserStoreManager class with first option");
            }

            Constructor constructor;
            try {
                constructor = clazz.getConstructor(initClassOpt0);
                newObject = constructor.newInstance(initObjOpt0);
                return newObject;
            } catch (NoSuchMethodException e) {
                if (log.isDebugEnabled()) {
                    log.debug("Cannont initialize " + className + " trying second option");
                }
            }

            try {
                constructor = clazz.getConstructor(initClassOpt1);
                newObject = constructor.newInstance(initObjOpt1);
                return newObject;
            } catch (NoSuchMethodException e) {
                if (log.isDebugEnabled()) {
                    log.debug("Cannont initialize " + className + " trying second option");
                }
            }

            try {
                constructor = clazz.getConstructor(initClassOpt2);
                newObject = constructor.newInstance(initObjOpt2);
                return newObject;
            } catch (NoSuchMethodException e) {
                if (log.isDebugEnabled()) {
                    log.debug("Cannont initialize " + className + " using the option 2");
                }
            }

            try {
                constructor = clazz.getConstructor(initClassOpt3);
                newObject = constructor.newInstance(initObjOpt3);
                return newObject;
            } catch (NoSuchMethodException e) {
                if (log.isDebugEnabled()) {
                    log.debug("Cannont initialize " + className + " using the option 3");
                }
            }

            try {
                constructor = clazz.getConstructor(initClassOpt4);
                newObject = constructor.newInstance(initObjOpt4);
                return newObject;
            } catch (NoSuchMethodException e) {
                if (log.isDebugEnabled()) {
                    log.debug("Cannont initialize " + className + " using the option 4");
                }
                throw new UserStoreException(e.getMessage(), e);
            }
        } catch (Throwable e) {
            if (log.isDebugEnabled()) {
                log.debug("Cannot create " + className, e);
            }
            throw new UserStoreException(e.getMessage() + "Type " + e.getClass(), e);
        }
    }

    /**
     * This method converts a given stacktrace array to a string
     *
     * @param arr The stack trace array
     * @return the string generated from the stack trace array
     */
    public static String stackTraceToString(StackTraceElement[] arr) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : arr) {
            sb.append("\t" + element.toString() + "\n");
        }
        return sb.toString();
    }

    /**
     * Utility function to retrieve RealmConfiguration of the server
     *
     * @return RealmConfiguration
     */
    public static RealmConfiguration getRealmConfiguration() throws UserStoreException {
        DataHolder dataHolder = DataHolder.getInstance();
        if (dataHolder.getRealmConfig() == null) {
            // If lazy loading enabled initialize security parameter
            if (log.isDebugEnabled()) {
                log.debug("Lazy loading security parameters");
            }
            ServiceComponent.initSecurityParams();
        }
        return dataHolder.getRealmConfig();
    }

    /**
     * Utility function to retrieve UserStoreManager of the server
     *
     * @return RealmConfiguration
     */
    public static UserStoreManager getUserStoreManager() throws UserStoreException {
        DataHolder dataHolder = DataHolder.getInstance();
        if (dataHolder.getUserStoreManager() == null) {
            // If lazy loading enabled initialize security parameter
            if (log.isDebugEnabled()) {
                log.debug("Lazy loading security parameters");
            }
            ServiceComponent.initSecurityParams();
        }
        return dataHolder.getUserStoreManager();
    }

    /**
     * Method to assert if a user is an admin
     *
     * @param user the user to be validated as an admin
     * @return true if the admin role is assigned to the user
     * @throws UserStoreException if any error occurs while retrieving the user store manager or reading the user realm
     *                            configuration
     */
    public static boolean isAdmin(String user) throws UserStoreException {
        String[] roles = getUserStoreManager().getRoleListOfUser(user);
        return containsAdminRole(roles);
    }

    /**
     * Method to assert if the admin role is contained within a list of roles
     *
     * @param rolesList the list of roles assigned to a user
     * @return true if the admin role is present in the list of roles provided
     * @throws UserStoreException if any error occurs while reading the realm configuration
     */
    public static boolean containsAdminRole(String[] rolesList) throws UserStoreException {
        return Arrays.asList(rolesList).contains(getRealmConfiguration().getAdminRoleName());
    }
}
