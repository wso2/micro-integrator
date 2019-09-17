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
package org.wso2.micro.integrator.dataservices.sql.driver.processor.writer;

import java.util.Map;

import org.wso2.micro.integrator.dataservices.sql.driver.parser.Constants;
import org.wso2.micro.integrator.dataservices.sql.driver.parser.ParserException;
import org.wso2.micro.integrator.dataservices.sql.driver.processor.reader.DataTable;

public class DataWriterFactory {

    public static DataWriter createDataWriter(String type,
                                              Map<String, DataTable> data) throws ParserException {
        if (Constants.EXCEL.equals(type)) {
            return new ExcelDataWriter(null, data);
        } else {
            throw new ParserException("Unsupported data type");
        }
    }

}
