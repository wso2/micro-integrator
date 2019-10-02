/*
 * Copyright 2005-2007 WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.micro.core.util;

/**
 * This class will filter admin services and modules. WSAS specific services are hard
 * coded and rest is plugable via services.xml.
 */
public class SystemFilter {

    //TODO this doen't give filtered modules now, but used by service archive create 
    public static boolean isFilteredOutModule(String moduleName) {
        return false;
    }

}
