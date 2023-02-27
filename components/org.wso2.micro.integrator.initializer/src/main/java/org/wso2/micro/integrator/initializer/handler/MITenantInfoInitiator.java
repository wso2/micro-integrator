/*
 *  Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.micro.integrator.initializer.handler;

import org.apache.synapse.commons.util.ext.TenantInfoInitiator;

public class MITenantInfoInitiator implements TenantInfoInitiator {

    @Override
    public void initTenantInfo(){
        //Nothing to do here, since MI does not have tenancy
    }

    /**
     * initialize tenant information based on the request URI
     * @param uri request URI
     */
    @Override
    public void initTenantInfo(String uri) {
        //Nothing to do here, since MI does not have tenancy
    }

    @Override
    public void cleanTenantInfo() {
        //Nothing to do here, since MI does not have tenancy
    }
}
