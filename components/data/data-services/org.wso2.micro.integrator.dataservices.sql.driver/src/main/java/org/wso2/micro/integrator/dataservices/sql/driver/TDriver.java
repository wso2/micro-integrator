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
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Enumeration;
import java.util.Properties;
import java.util.logging.Logger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.micro.integrator.dataservices.sql.driver.parser.Constants;

public class TDriver implements Driver {

    private boolean isFilePath;

    private static final Log log = LogFactory.getLog(Driver.class);

    public boolean isFilePath() {
        return isFilePath;
    }

    static {
        try {
            DriverManager.registerDriver(new TDriver());
        } catch (SQLException e) {
            log.error("Error in registering the driver", e);
        }
    }

    public Connection connect(String url, Properties info) throws SQLException {
        Properties props = getProperties(url, info);
        String conType = props.getProperty(Constants.DRIVER_PROPERTIES.DATA_SOURCE_TYPE);
        return TConnectionFactory.createConnection(conType, props);
    }

    public boolean acceptsURL(String url) throws SQLException {
        return false;
    }

    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
        return new DriverPropertyInfo[0];
    }

    public int getMajorVersion() {
        return 0;
    }

    public int getMinorVersion() {
        return 0;
    }

    public boolean jdbcCompliant() {
        return false;
    }

    @SuppressWarnings("unchecked")
    private Properties getProperties(String url, Properties info) throws SQLException {
        if (url == null) {
            throw new SQLException("JDBC URL cannot be NULL");
        }
        Properties props = new Properties();
        for (Enumeration<String> e = (Enumeration<String>) info.propertyNames();
             e.hasMoreElements();) {
            String key = e.nextElement();
            String value = info.getProperty(key);
            if (value != null) {
                props.setProperty(key.toUpperCase(), value);
            }
        }
        int pos = 0;
        StringBuilder token = new StringBuilder();
        pos = getNextTokenPos(url, pos, token);
        if (!Constants.JDBC_PREFIX.equalsIgnoreCase(token.toString())) {
            throw new SQLException("Malformed URL");
        }
        pos = getNextTokenPos(url, pos, token);
        if (!Constants.PROVIDER_PREFIX.equalsIgnoreCase(token.toString())) {
            throw new SQLException("Malformed URL");
        }
        pos = getNextTokenPos(url, pos, token);
        if (!Constants.EXCEL_PREFIX.equalsIgnoreCase(token.toString()) &&
                !Constants.GSPRED_PREFIX.equalsIgnoreCase(token.toString())) {
            throw new SQLException("Malformed URL");
        }
        props.setProperty(Constants.DRIVER_PROPERTIES.DATA_SOURCE_TYPE, token.toString());
        pos = getNextTokenPos(url, pos, token);
        if (Constants.DRIVER_PROPERTIES.FILE_PATH.equals(token.toString())) {
            isFilePath = true;
            pos = getNextTokenPos(url, pos, token);
            String propValue = token.toString();
            if (propValue == null || "".equals(propValue)) {
                throw new SQLException("File path attribute is missing");
            }
            props.setProperty(Constants.DRIVER_PROPERTIES.FILE_PATH, propValue);
        }

        Object dsType = props.getProperty(Constants.DRIVER_PROPERTIES.DATA_SOURCE_TYPE);
        if (dsType != null && Constants.GSPRED_PREFIX.equals(dsType.toString())) {
            pos = getNextTokenPos(url, pos, token);
            if (Constants.DRIVER_PROPERTIES.SHEET_NAME.equals(token.toString())) {
                pos = getNextTokenPos(url, pos, token);
                String propValue = token.toString();
                if (propValue == null || "".equals(propValue)) {
                    throw new SQLException("Sheet name attribute is missing");
                }
                props.setProperty(Constants.DRIVER_PROPERTIES.SHEET_NAME, propValue);
            }
        }
        Properties optionalProps = getOptionalProperties(url, pos, token);
        /* check for maxColumns property */
        this.checkForHasHeaderProperty(optionalProps);

        for (Enumeration<String> e = (Enumeration<String>) optionalProps.propertyNames();
             e.hasMoreElements();) {
            String key = e.nextElement();
            props.setProperty(key, optionalProps.getProperty(key));
        }
        return props;
    }

    private Properties getOptionalProperties(String url, int pos,
                                             StringBuilder token) throws SQLException {
        Properties optionalProps = new Properties();
        token.setLength(0);
        while (pos < url.length()) {
            char c = url.charAt(pos++);
            if (c != ';') {
                token.append(c);
            } else {
                addProperty(optionalProps, token);
                token.setLength(0);
            }
        }
        if (token.length() > 0) {
            addProperty(optionalProps, token);
            token.setLength(0);
        }
        return optionalProps;
    }

    private void checkForHasHeaderProperty(Properties optionalProps) throws SQLException {
        String hasHeader = (String) optionalProps.get(Constants.DRIVER_PROPERTIES.HAS_HEADER);
        if (hasHeader != null && !Boolean.parseBoolean(hasHeader)) {
            String maxColumns = (String) optionalProps.get(Constants.DRIVER_PROPERTIES.MAX_COLUMNS);
            if (maxColumns == null) {
                throw new SQLException("'hasHeader' attribute should be accompanied by the " +
                        "attribute 'maxColumns'");
            }
            try {
                Integer.parseInt(maxColumns);
            } catch (Exception e) {
                throw new SQLException("Invalid value specified for the attribute 'maxColumns'", e);
            }
        }
    }

    private void addProperty(Properties props, StringBuilder token) throws SQLException {
        String propName = token.substring(0, token.indexOf(Constants.EQUAL));
        if (!TDriverUtil.getAvailableDriverProperties().contains(propName)) {
            throw new SQLException("Invalid driver property '" + propName + "' specified");
        }
        String propValue =
                token.substring(token.indexOf(Constants.EQUAL) + 1, token.length());
        props.setProperty(propName, propValue);
    }

    private int getNextTokenPos(String url, int pos, StringBuilder token) {
        token.setLength(0);
        while (pos < url.length()) {
            char c = url.charAt(pos++);
            if (c == ':') {
                if (!isFilePath()) {
                    break;
                }
            }
            if (c == ';') {
                if (isFilePath()) {
                    isFilePath = false;
                    break;
                } else {
                    break;
                }
            }
            if (c == '/') {
                if (!isFilePath()) {
                    break;
                }
            }
            if (c == '=') {
                if (!isFilePath()) {
                    break;
                }
            }
            token.append(c);
        }
        if ("".equals(token.toString()) && pos < url.length()) {
            return getNextTokenPos(url, pos, token);
        }
        return pos;
    }

    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException("This method is not supported");
    }

}
