/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.micro.base;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This class contains the singleton static references that hold the base of the
 * Carbon Context Holder. This class is supposed to maintain a single static
 * instance spanning across multiple class-loaders. The OSGi servlet bridge will
 * expose this class into the OSGi world.
 */
@SuppressWarnings("unused")
public final class CarbonContextHolderBase {

    private static final Log log = LogFactory.getLog(CarbonContextHolderBase.class);
    /**
     * The reference to the ws discovery service provider of the current tenant,
     * as visible to a user.
     */
    private static final AtomicReference<DiscoveryService> discoveryServiceProvider = new AtomicReference<>();
    // stores the current CarbonContext local to the running thread.
    private static ThreadLocal<CarbonContextHolderBase> currentContextHolderBase = ThreadLocal
            .withInitial(CarbonContextHolderBase::new);

    // stores references to the existing CarbonContexts when starting tenant
    // flows. These
    // references will be popped back, when a tenant flow is ended.
    private static ThreadLocal<Stack<CarbonContextHolderBase>> parentContextHolderBaseStack = ThreadLocal
            .withInitial(Stack::new);

    private Map<String, Object> properties;

    /**
     * Default constructor to disallow creation of the CarbonContext.
     */
    private CarbonContextHolderBase() {
        this.properties = new HashMap<>();
    }

    /**
     * Constructor that can be used to create clones.
     *
     * @param carbonContextHolder the CarbonContext holder instance of which the clone will be
     *                            created from.
     */
    public CarbonContextHolderBase(CarbonContextHolderBase carbonContextHolder) {

        this.properties = new HashMap<>(carbonContextHolder.properties);
    }

    /**
     * Method to obtain an instance to the Discovery Service.
     *
     * @return instance of the Discovery Service
     */
    public static DiscoveryService getDiscoveryServiceProvider() {
        return discoveryServiceProvider.get();
    }

    /**
     * Method to define the instance of the Discovery Service.
     *
     * @param discoveryServiceProvider the Discovery Service instance.
     */
    public static void setDiscoveryServiceProvider(DiscoveryService discoveryServiceProvider) {
        CarbonContextHolderBase.discoveryServiceProvider.set(discoveryServiceProvider);
    }

    /**
     * Method to obtain the current carbon context holder's base.
     *
     * @return the current carbon context holder's base.
     */
    public static CarbonContextHolderBase getCurrentCarbonContextHolderBase() {
        return currentContextHolderBase.get();
    }

    /**
     * This method will destroy the current CarbonContext holder.
     */
    public static void destroyCurrentCarbonContextHolder() {
        currentContextHolderBase.remove();
        parentContextHolderBaseStack.remove();
    }

    /**
     * Method to obtain a property on this CarbonContext instance.
     *
     * @param name the property name.
     * @return the value of the property by the given name.
     */
    public Object getProperty(String name) {
        return properties.get(name);
    }

    /**
     * Method to set a property on this CarbonContext instance.
     *
     * @param name  the property name.
     * @param value the value to be set to the property by the given name.
     */
    public void setProperty(String name, Object value) {
        log.trace("Setting Property: " + name);
        properties.put(name, value);
    }

    // Method to cleanup all properties.
    private void cleanupProperties() {
        // This method would be called to reclaim memory. Therefore, this might
        // be called on an
        // object which has been partially garbage collected. Even unlikely, it
        // might be possible
        // that the object exists without any field-references, until all
        // WeakReferences are
        // cleaned-up.
        if (properties != null) {
            log.trace("Cleaning up properties.");
            properties.clear();
        }
    }

    // Utility method to restore a CarbonContext.
    private void restore(CarbonContextHolderBase carbonContextHolder) {
        if (carbonContextHolder != null) {

            this.properties = new HashMap<>(carbonContextHolder.properties);
        } else {
            this.properties = new HashMap<>();
        }
    }

}
