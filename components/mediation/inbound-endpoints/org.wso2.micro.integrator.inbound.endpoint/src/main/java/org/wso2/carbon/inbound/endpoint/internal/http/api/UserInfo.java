/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.inbound.endpoint.internal.http.api;

/**
 * This class is the DTO for User configs in internal-apis.xml
 */
public class UserInfo {

    private char[] password;
    private boolean admin;

    public UserInfo(char[] password, boolean admin) {

        this.password = password;
        this.admin = admin;
    }

    public char[] getPassword() {

        return password;
    }

    public void setPassword(char[] password) {

        this.password = password;
    }

    public boolean isAdmin() {

        return admin;
    }

    public void setAdmin(boolean admin) {

        this.admin = admin;
    }
}