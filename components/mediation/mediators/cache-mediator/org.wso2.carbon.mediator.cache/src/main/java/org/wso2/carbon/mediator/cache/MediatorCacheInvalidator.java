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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;

/**
 * This class is used for global cache invalidation.
 */
public class MediatorCacheInvalidator implements MediatorCacheInvalidatorMBean {

    /**
     * Log object to use when logging is required in this class.
     */
    private static final Log log = LogFactory.getLog(MediatorCacheInvalidator.class);

    /**
     * This holds the tenant domain which will be used by invalidate cache with respect to the relevant tenant.
     */
    private String tenantDomain;

    /**
     * This holds the tenant ID which will be used by invalidate cache with respect to the relevant tenant.
     */
    private int tenantId;

    /**
     * This holds the message Context required to construct global cache invalidator.
     */
    private MessageContext msgCtx;

    private CacheManager cacheManager;

    /**
     * MediatorCacheInvalidator Constructor which creates MBean to expose operations to invalidate the mediator cache.
     *
     * @param tenantDomain which the  mediator cache should be invalidated.
     * @param tenantId     which the mediator cache should be invalidated.
     * @param msgCtx       which holds the mediator cache.
     */
    public MediatorCacheInvalidator(CacheManager cacheManager, String tenantDomain, int tenantId,
                                    MessageContext msgCtx) {
        this.tenantDomain = tenantDomain;
        this.tenantId = tenantId;
        this.msgCtx = msgCtx;
        this.cacheManager = cacheManager;
    }

    @Override
    public void invalidateTheWholeCache() {
        cacheManager.clean();
        log.info("Total mediator cache has been invalidated.");
    }

    /**
     * This method gives the tenant domain.
     *
     * @return tenant domain.
     */
    public String getTenantDomain() {
        return tenantDomain;
    }

    /**
     * This method sets the tenant domain.
     *
     * @param tenantDomain string value to be set.
     */
    public void setTenantDomain(String tenantDomain) {
        this.tenantDomain = tenantDomain;
    }

    /**
     * This method gives the tenant ID.
     *
     * @return tenant Id as a integer.
     */
    public int getTenantId() {
        return tenantId;
    }

    /**
     * This method sets the tenant ID
     *
     * @param tenantId integer value to be set.
     */
    public void setTenantId(int tenantId) {
        this.tenantId = tenantId;
    }

    /**
     * This method gives the tenant message context.
     *
     * @return message context.
     */
    public MessageContext getMsgCtx() {
        return msgCtx;
    }

    /**
     * This method sets the message context.
     *
     * @param msgCtx message context to be set.
     */
    public void setMsgCtx(MessageContext msgCtx) {
        this.msgCtx = msgCtx;
    }
}
