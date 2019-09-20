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
package org.wso2.micro.integrator.dataservices.sql.driver;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import org.wso2.micro.integrator.dataservices.sql.driver.parser.Constants;

public class TConnectionFactory {

    public static Connection createConnection(String type, Properties props) throws SQLException {
        type = type.toUpperCase();
        if (Constants.EXCEL.equals(type)) {
            return new TExcelConnection(props);
        } else if (Constants.GSPREAD.equals(type)) {
            return new TGSpreadConnection(props);
        } else if (Constants.CUSTOM.equals(type)) {
            return new TCustomConnection(props);
        } else {
            throw new SQLException("Unsupported datasource type");
        }

    }

}
