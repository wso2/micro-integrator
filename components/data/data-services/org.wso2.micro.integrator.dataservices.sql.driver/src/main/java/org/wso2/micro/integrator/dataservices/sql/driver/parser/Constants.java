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
package org.wso2.micro.integrator.dataservices.sql.driver.parser;

public final class Constants {

    public static final String SEMI_COLON = ";";
    public static final String EQUAL = "=";
    public static final String WHITE_SPACE = " ";
    public static final String COMMA = ",";
    public static final String ASTERISK = "*";
    public static final String DOT = ".";
    public static final String HYPHEN = "`";
    public static final String GREATER_THAN = ">";
    public static final String LESS_THAN = "<";
    public static final String LEFT_BRACKET = "(";
    public static final String RIGHT_BRACKET = ")";
    public static final String LEFT_BRACE = "{";
    public static final String RIGHT_BRACE = "}";
    public static final String PLUS = "+";
    public static final String MINUS = "-";
    public static final String UNDERSCORE = "_";
    public static final String DIVISION = "/";
    public static final String FORWARD_SLASH = "/";
    public static final String COLON = ":";
    public static final String SINGLE_QUOTATION = "'";

    public static final String SELECT = "SELECT";
    public static final String DELETE = "DELETE";
    public static final String FROM = "FROM";
    public static final String WHERE = "WHERE";
    public static final String INSERT = "INSERT";
    public static final String INTO = "INTO";
    public static final String GROUP_BY = "GROUP BY";
    public static final String ORDER_BY = "ORDER_BY";
    public static final String COUNT = "COUNT";
    public static final String MAX = "MAX";
    public static final String VALUES = "VALUES";
    public static final String UPDATE = "UPDATE";
    public static final String SET = "SET";
    public static final String DELAYED = "DELAYED";
    public static final String LOW_PRIORITY = "LOW_PRIORITY";
    public static final String HIGH_PRIORITY = "HIGH_PRIORITY";
    public static final String ON = "ON";
    public static final String DUPLICATE = "DUPLICATE";
    public static final String KEY = "KEY";
    public static final String LAST_INSERT_ID = "LAST_INSERT_ID";
    public static final String ALL = "ALL";
    public static final String DISTINCT = "DISTINCT";
    public static final String DISTINCTROW = "DISTINCTROW";
    public static final String STRAIGHT_JOIN = "STRAIGHT_JOIN";
    public static final String SQL_SMALL_RESULT = "SQL_SMALL_RESULT";
    public static final String SQL_BIG_RESULT = "SQL_BIG_RESULT";
    public static final String SQL_BUFFER_RESULT = "SQL_BUFFER_RESULT";
    public static final String SQL_CACHE = "SQL_CACHE";
    public static final String SQL_NO_CACHE = "SQL_NO_CACHE";
    public static final String SQL_CALC_FOUND_ROWS = "SQL_CALC_FOUND_ROWS";
    public static final String ASC = "ASC";
    public static final String DESC = "DESC";
    public static final String LIMIT = "LIMIT";
    public static final String OFFSET = "OFFSET";
    public static final String WITH = "WITH";
    public static final String ROLLUP = "ROLLUP";
    public static final String PROCEDURE = "PROCEDURE";
    public static final String OUTFILE = "OUTFILE";
    public static final String DUMPFILE = "DUMPFILE";
    public static final String LOCK = "LOCK";
    public static final String SHARE = "SHARE";
    public static final String MODE = "MODE";
    public static final String CONCAT = "CONCAT";
    public static final String AS = "AS";
    public static final String AVG = "AVG";
    public static final String MIN = "MIN";
    public static final String TRIM = "TRIM";
    public static final String LTRIM = "LTRIM";
    public static final String RTRIM = "RTRIM";
    public static final String SUBSTR = "SUBSTR";
    public static final String NOT = "NOT";
    public static final String IS = "IS";
    public static final String IN = "IN";
    public static final String NULL = "NULL";
    public static final String LIKE = "LIKE";
    public static final String AND = "AND";
    public static final String OR = "OR";
    public static final String JOIN = "JOIN";
    public static final String INNER = "INNER";
    public static final String SUM = "SUM";
    public static final String CREATE = "CREATE";
    public static final String SHEET = "SHEET";
    public static final String DROP = "DROP";

    public static final String COLUMN = "COLUMN";
    public static final String TABLE = "TABLE";
    public static final String OPERATOR = "OPERATOR";
    public static final String OP_VALUE = "OPVALUE";
    public static final String AS_REF = "ASREF";
    public static final String AGGREGATE_FUNCTION = "AGGREGATEFUNC";
    public static final String STRING_FUNCTION = "STRINGFUNC";
    public static final String START_OF_LBRACKET = "START_OF_LBRACKET";
    public static final String START_OF_RBRACKET = "START_OF_RBRACKET";
    public static final String AS_COLUMN = "ASCOLUMN";

    public static final String EXCEL = "EXCEL";
    public static final String GSPREAD = "GSPREAD";
    public static final String CUSTOM = "CUSTOM";

    public static final String VALUE = "VALUE";
    public static final String PARAM_VALUE = "PARAM_VALUE";

    public static final String BASE_WORKSHEET_URL = "https://spreadsheets.google.com/feeds/worksheets/";
    public static final String SPREADSHEET_FEED_BASE_URL = "https://spreadsheets.google.com/feeds/spreadsheets/";
    public static final String SPREADSHEET_SERVICE_NAME = "WSO2SQLDriver";

    public static final String JDBC_PREFIX = "jdbc";
    public static final String EXCEL_PREFIX = "excel";
    public static final String GSPRED_PREFIX = "gspread";
    public static final String PROVIDER_PREFIX = "wso2";

    public static final class DRIVER_PROPERTIES {
        public static final String FILE_PATH = "filePath";
        public static final String DATA_SOURCE_TYPE = "dsType";
        public static final String USER = "USER";
        public static final String PASSWORD = "PASSWORD";
        public static final String VISIBILITY = "visibility";
        public static final String SHEET_NAME = "sheetName";
        public static final String HAS_HEADER = "hasHeader";
        public static final String MAX_COLUMNS = "maxColumns";
    }

    public static final class GSPREAD_PROPERTIES {
        public static final String CLIENT_ID = "clientId";
        public static final String CLIENT_SECRET = "clientSecret";
        public static final String REFRESH_TOKEN = "refreshToken";
    }

    public static final String OUTER = "OUTER";

    public static final String ACCESS_MODE_PRIVATE = "private";
    public static final String ACCESS_MODE_PUBLIC = "public";

    public static final String RETURN = "\r";
    public static final String NEW_LINE = "\n";

}
