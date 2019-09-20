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
package org.wso2.micro.integrator.dataservices.sql.driver.processor.reader;

import java.sql.Connection;
import java.sql.SQLException;

import org.wso2.micro.integrator.dataservices.sql.driver.TConnection;
import org.wso2.micro.integrator.dataservices.sql.driver.parser.Constants;

public class DataReaderFactory {

    public static DataReader createDataReader(Connection connection) throws SQLException {
        if (!(connection instanceof TConnection)) {
            throw new SQLException("Connection cannot be casted to 'TConnection'");
        }
        String connectionType = ((TConnection) connection).getType();
        if (Constants.EXCEL.equals(connectionType)) {
            return new ExcelDataReader(connection);
        } else if (Constants.GSPREAD.equals(connectionType)) {
            return new GSpreadDataReader(connection);
        } else if (Constants.CUSTOM.equals(connectionType)) {
            return new CustomDataReader(connection);
        } else {
            throw new SQLException("Unsupported config type");
        }
    }

}
