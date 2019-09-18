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

package org.wso2.micro.integrator.security.user.core.claim;

import java.io.Serializable;

/**
 * Represents a claim that is associated with an entity usually a user. Claims
 * describe the capabilities associated with some entity in the system. A claim
 * is the expression of a right with respect to a particular value. Hence a
 * claim has a uri, display name, value and many other properties. This class
 * models the properties of a claim.
 */
public class Claim extends org.wso2.micro.integrator.security.user.api.Claim implements Serializable {
}
