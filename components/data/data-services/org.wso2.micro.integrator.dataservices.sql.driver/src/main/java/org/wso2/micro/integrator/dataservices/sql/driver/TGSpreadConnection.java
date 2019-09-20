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

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;
import java.util.List;
import java.util.Properties;

import com.google.gdata.client.spreadsheet.SpreadsheetQuery;
import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.client.spreadsheet.WorksheetQuery;
import com.google.gdata.data.spreadsheet.SpreadsheetEntry;
import com.google.gdata.data.spreadsheet.SpreadsheetFeed;
import com.google.gdata.data.spreadsheet.WorksheetFeed;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.micro.integrator.dataservices.sql.driver.parser.Constants;
import org.wso2.micro.integrator.dataservices.sql.driver.util.GSpreadFeedProcessor;

public class TGSpreadConnection extends TConnection {
    private static final Log log = LogFactory.getLog(TGSpreadConnection.class);

    private String spreadSheetName;

    private SpreadsheetFeed spreadSheetFeed;

    private WorksheetFeed worksheetFeed;

    private GSpreadFeedProcessor feedProcessor;

    public TGSpreadConnection(Properties props) throws SQLException {
        super(props);
        this.spreadSheetName = props.getProperty(Constants.DRIVER_PROPERTIES.SHEET_NAME);

        String visibility = props.getProperty(Constants.DRIVER_PROPERTIES.VISIBILITY);
        visibility = (visibility != null) ? visibility : Constants.ACCESS_MODE_PRIVATE;
        String clientId = props.getProperty(Constants.GSPREAD_PROPERTIES.CLIENT_ID);
        String clientSecret = props.getProperty(Constants.GSPREAD_PROPERTIES.CLIENT_SECRET);
        String refreshToken = props.getProperty(Constants.GSPREAD_PROPERTIES.REFRESH_TOKEN);


        feedProcessor = new GSpreadFeedProcessor(clientId, clientSecret, refreshToken, visibility,
                                                 Constants.SPREADSHEET_FEED_BASE_URL);
        if (feedProcessor.requiresAuth()) {
            try {
                this.feedProcessor.setClientId(URLDecoder.decode(this.feedProcessor.getClientId(), "UTF-8"));
                this.feedProcessor.setClientSecret(URLDecoder.decode(this.feedProcessor.getClientSecret(), "UTF-8"));
                this.feedProcessor.setRefreshToken(URLDecoder.decode(this.feedProcessor.getRefreshToken(), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                throw new SQLException("Error in retrieving Authentication information " + e.getMessage(), e);
            }
        }
        if (spreadSheetName == null) {
            throw new SQLException("Spread Sheet name is not provided");
        }

        SpreadsheetService service = new SpreadsheetService(Constants.SPREADSHEET_SERVICE_NAME);
        service.setCookieManager(null);
        this.feedProcessor.setService(service);

        this.spreadSheetFeed = this.extractSpreadSheetFeed();
        this.worksheetFeed = this.extractWorkSheetFeed();
    }

    public String getSpreadSheetName() {
        return spreadSheetName;
    }

    public GSpreadFeedProcessor getFeedProcessor() {
        return feedProcessor;
    }

    public WorksheetFeed getWorksheetFeed() {
        return worksheetFeed;
    }

    public SpreadsheetFeed getSpreadSheetFeed() {
        return spreadSheetFeed;
    }

    @Override
    public Statement createStatement() throws SQLException {
        return null;
    }

    @Override
    public PreparedStatement prepareStatement(String sql) throws SQLException {
        return new TPreparedStatement(this, sql);
    }

    @Override
    public CallableStatement prepareCall(String sql) throws SQLException {
        throw new SQLFeatureNotSupportedException("CallableStatements are not supported");
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType,
                                              int resultSetConcurrency) throws SQLException {
        return new TPreparedStatement(this, sql);
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType,
                                         int resultSetConcurrency) throws SQLException {
        throw new SQLFeatureNotSupportedException("CallableStatements are not supported");
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType,
                                              int resultSetConcurrency,
                                              int resultSetHoldability) throws SQLException {
        return new TPreparedStatement(this, sql);
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType,
                                         int resultSetConcurrency,
                                         int resultSetHoldability) throws SQLException {
        throw new SQLFeatureNotSupportedException("CallableStatements are not supported");
    }

    @Override
    public PreparedStatement prepareStatement(String sql,
                                              int autoGeneratedKeys) throws SQLException {
        return new TPreparedStatement(this, sql);
    }

    @Override
    public PreparedStatement prepareStatement(String sql,
                                              int[] columnIndexes) throws SQLException {
        return new TPreparedStatement(this, sql);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, String[] columnNames) throws
            SQLException {
        return new TPreparedStatement(this, sql);
    }

    private WorksheetFeed extractWorkSheetFeed() throws SQLException {
        if (this.getSpreadSheetFeed() == null) {
            throw new SQLException("Spread Sheet Feed is null");
        }
        List<SpreadsheetEntry> entries = this.getSpreadSheetFeed().getEntries();
        /* If no SpreadSheetEntry is available in the spreadsheet feed inferred using a
         * SpreadSheetQuery, try getting it directly via a SpreadSheetFeed retrieved via the 
         * SpreadSheetService */
        SpreadsheetEntry spreadsheetEntry =
                (entries != null && entries.size() > 0) ? entries.get(0) :
                        this.extractSpreadSheetEntryFromUrl();
        if (spreadsheetEntry == null) {
            throw new SQLException("No SpreadSheetEntry is available, matching provided " +
                    "connection information");
        }
        WorksheetQuery worksheetQuery =
                TDriverUtil.createWorkSheetQuery(spreadsheetEntry.getWorksheetFeedUrl());
        return this.feedProcessor.getFeed(worksheetQuery, WorksheetFeed.class);
    }

    private SpreadsheetEntry extractSpreadSheetEntryFromUrl() throws SQLException {
        try {
            URL spreadSheetFeedUrl = this.feedProcessor.getSpreadSheetFeedUrl();
            SpreadsheetFeed feed =
                    this.feedProcessor.getFeed(spreadSheetFeedUrl, SpreadsheetFeed.class);
            List<SpreadsheetEntry> entries = feed.getEntries();
            return (entries != null && entries.size() > 0) ? entries.get(0) : null;
        } catch (Exception e) {
            throw new SQLException("Error occurred while extracting spread sheet entry", e);
        }
    }

    private SpreadsheetFeed extractSpreadSheetFeed() throws SQLException {
        URL spreadSheetFeedUrl;
        try {
            spreadSheetFeedUrl = this.feedProcessor.getSpreadSheetFeedUrl();
        } catch (MalformedURLException e) {
            throw new SQLException("Error occurred while constructing the Spread Sheet Feed URL");
        }
        SpreadsheetQuery spreadSheetQuery =
                TDriverUtil.createSpreadSheetQuery(this.getSpreadSheetName(), spreadSheetFeedUrl);
        return this.feedProcessor.getFeed(spreadSheetQuery, SpreadsheetFeed.class);
    }

}
