/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.micro.integrator.security;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * An in-memory DB which maintains all the security scenarios.
 */
public class SecurityScenarioDatabase {
    private static Map<String, SecurityScenario> scenarios =
            new LinkedHashMap<>();

    private SecurityScenarioDatabase(){}

    public static void put(String scenarioId, SecurityScenario scenario) {
        scenarios.put(scenarioId, scenario);
    }

    public static SecurityScenario get(String scenarioId) {
        return scenarios.get(scenarioId);
    }

    public static Collection<SecurityScenario> getAllScenarios() {
        return scenarios.values();
    }

    public static SecurityScenario getByWsuId(String wsuIdValue) {
        SecurityScenario scenario = null;
        Iterator<SecurityScenario> ite = scenarios.values().iterator();
        while (ite.hasNext()) {
            SecurityScenario temp = ite.next();
            if (wsuIdValue.equals(temp.getWsuId())) {
                scenario = temp;
                break;
            }
        }
        return scenario;
    }
}
