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
package org.wso2.micro.integrator.dataservices.sql.driver.query;

import java.sql.SQLException;
import java.sql.Statement;

import org.wso2.micro.integrator.dataservices.sql.driver.TConnection;
import org.wso2.micro.integrator.dataservices.sql.driver.TPreparedStatement;
import org.wso2.micro.integrator.dataservices.sql.driver.query.create.ExcelCreateQuery;
import org.wso2.micro.integrator.dataservices.sql.driver.query.create.GSpreadCreateQuery;
import org.wso2.micro.integrator.dataservices.sql.driver.query.delete.CustomDeleteQuery;
import org.wso2.micro.integrator.dataservices.sql.driver.query.delete.ExcelDeleteQuery;
import org.wso2.micro.integrator.dataservices.sql.driver.query.delete.GSpreadDeleteQuery;
import org.wso2.micro.integrator.dataservices.sql.driver.query.drop.ExcelDropQuery;
import org.wso2.micro.integrator.dataservices.sql.driver.query.drop.GSpreadDropQuery;
import org.wso2.micro.integrator.dataservices.sql.driver.query.insert.CustomInsertQuery;
import org.wso2.micro.integrator.dataservices.sql.driver.query.insert.ExcelInsertQuery;
import org.wso2.micro.integrator.dataservices.sql.driver.query.insert.GSpreadInsertQuery;
import org.wso2.micro.integrator.dataservices.sql.driver.query.select.CustomSelectQuery;
import org.wso2.micro.integrator.dataservices.sql.driver.query.select.ExcelSelectQuery;
import org.wso2.micro.integrator.dataservices.sql.driver.query.select.GSpreadSelectQuery;
import org.wso2.micro.integrator.dataservices.sql.driver.query.update.CustomUpdateQuery;
import org.wso2.micro.integrator.dataservices.sql.driver.query.update.ExcelUpdateQuery;
import org.wso2.micro.integrator.dataservices.sql.driver.query.update.GSpreadUpdateQuery;

public class QueryFactory {

    public enum QueryFactoryTypes {
        SELECT, INSERT, UPDATE, DELETE, CREATE, DROP
    }

    public enum QueryTypes {
        EXCEL, GSPREAD, CUSTOM
    }

    public static Query createQuery(Statement stmt) throws SQLException {
        String queryType = ((TPreparedStatement) stmt).getQueryType();
        QueryFactoryTypes types = QueryFactoryTypes.valueOf(queryType);
        switch (types) {
            case SELECT:
                return createSelectQuery(stmt);
            case INSERT:
                return createInsertQuery(stmt);
            case UPDATE:
                return createUpdateQuery(stmt);
            case DELETE:
                return createDeleteQuery(stmt);
            case CREATE:
                return createCreateQuery(stmt);
            case DROP:
                return createDropQuery(stmt);
            default:
                throw new SQLException("Unsupport query type");
        }
    }

    private static Query createDropQuery(Statement stmt) throws SQLException {
        String connectionType =
                ((TConnection)(((TPreparedStatement)stmt).getConnection())).getType();
        QueryTypes types = QueryTypes.valueOf(connectionType);
        switch (types) {
            case EXCEL:
                return new ExcelDropQuery(stmt);
            case GSPREAD:
                return new GSpreadDropQuery(stmt);
            default:
                throw new SQLException("Unsupported type");
        }
    }

    private static Query createCreateQuery(Statement stmt) throws SQLException {
        String connectionType =
                ((TConnection)(((TPreparedStatement)stmt).getConnection())).getType();
        QueryTypes types = QueryTypes.valueOf(connectionType);
        switch (types) {
            case EXCEL:
                return new ExcelCreateQuery(stmt);
            case GSPREAD:
                return new GSpreadCreateQuery(stmt);
            default:
                throw new SQLException("Unsupported type");
        }
    }

    private static Query createDeleteQuery(Statement stmt) throws SQLException {
        String connectionType =
                ((TConnection) (((TPreparedStatement) stmt).getConnection())).getType();
        QueryTypes types = QueryTypes.valueOf(connectionType);
        switch (types) {
            case EXCEL:
                return new ExcelDeleteQuery(stmt);
            case GSPREAD:
                return new GSpreadDeleteQuery(stmt);
            case CUSTOM:
                return new CustomDeleteQuery(stmt);
            default:
                throw new SQLException("Unsupported type");
        }
    }

    public static Query createInsertQuery(Statement stmt) throws SQLException {
        String connectionType =
                ((TConnection) (((TPreparedStatement) stmt).getConnection())).getType();
        QueryTypes types = QueryTypes.valueOf(connectionType);
        switch (types) {
            case EXCEL:
                return new ExcelInsertQuery(stmt);
            case GSPREAD:
                return new GSpreadInsertQuery(stmt);
            case CUSTOM:
                return new CustomInsertQuery(stmt);
            default:
                throw new SQLException("Unsupported type");
        }
    }

    private static Query createSelectQuery(Statement stmt) throws SQLException {
        String connectionType =
                ((TConnection) (((TPreparedStatement) stmt).getConnection())).getType();
        QueryTypes types = QueryTypes.valueOf(connectionType);
        switch (types) {
            case EXCEL:
                return new ExcelSelectQuery(stmt);
            case GSPREAD:
                return new GSpreadSelectQuery(stmt);
            case CUSTOM:
                return new CustomSelectQuery(stmt);
            default:
                throw new SQLException("Unsupported type");
        }
    }

    private static Query createUpdateQuery(Statement stmt) throws SQLException {
        String connectionType =
                ((TConnection) (((TPreparedStatement) stmt).getConnection())).getType();
        QueryTypes types = QueryTypes.valueOf(connectionType);
        switch (types) {
            case EXCEL:
                return new ExcelUpdateQuery(stmt);
            case GSPREAD:
                return new GSpreadUpdateQuery(stmt);
            case CUSTOM:
                return new CustomUpdateQuery(stmt);
            default:
                throw new SQLException("Unsupported type");
        }
    }

}
