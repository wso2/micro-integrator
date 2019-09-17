/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.micro.integrator.dataservices.core.odata;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class stores the all the paths(relations) in a table and related keys.
 *
 * @see NavigationKeys
 */
public class NavigationTable {

    private Map<String, List<NavigationKeys>> columns;

    public NavigationTable() {
        this.columns = new HashMap<>();
    }

    public Set<String> getTables() {
        return this.columns.keySet();
    }

    public List<NavigationKeys> getNavigationKeys(String table) {
        return this.columns.get(table);
    }

    public void addNavigationKeys(String table, List<NavigationKeys> keys) {
        this.columns.put(table, keys);
    }
}
