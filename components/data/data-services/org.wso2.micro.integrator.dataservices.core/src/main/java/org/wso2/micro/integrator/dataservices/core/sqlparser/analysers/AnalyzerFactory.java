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
package org.wso2.micro.integrator.dataservices.core.sqlparser.analysers;

import org.wso2.micro.integrator.dataservices.core.DataServiceFault;
import org.wso2.micro.integrator.dataservices.core.sqlparser.LexicalConstants;

import java.util.Queue;

public class AnalyzerFactory {

    public static KeyWordAnalyzer createAnalyzer(String type,
                                                 Queue<String> tokens) throws DataServiceFault {
        type = type.toUpperCase();
        if (LexicalConstants.SELECT.equals(type)) {
            return new SelectAnalyser(tokens);
        } else if (LexicalConstants.WHERE.equals(type)) {
            return new WhereAnalyzer(tokens);
        } else {
            throw new DataServiceFault("Unsupported keyword '" + type + "'");
        }
    }

}
