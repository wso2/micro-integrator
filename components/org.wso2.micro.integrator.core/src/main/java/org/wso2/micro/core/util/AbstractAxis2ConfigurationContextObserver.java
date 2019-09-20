/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.micro.core.util;

import org.apache.axis2.context.ConfigurationContext;
//import org.wso2.micro.core.util.Axis2ConfigurationContextObserver;

/**
 * This is an abstract implementation of the Axis2ConfigurationContextObserver interface. It
 * does not perform any action on the events fired by the above interface and exists for the
 * sole purpose of providing an extension point.
 */
public abstract class AbstractAxis2ConfigurationContextObserver implements
                                                                Axis2ConfigurationContextObserver {

    public void creatingConfigurationContext(int tenantId) {

    }

    public void createdConfigurationContext(ConfigurationContext configContext) {

    }

    public void terminatingConfigurationContext(ConfigurationContext configCtx) {

    }

    public void terminatedConfigurationContext(ConfigurationContext configCtx) {

    }
}
