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
package org.wso2.micro.integrator.dataservices.core.description.config;

import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.IFeed;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.micro.integrator.dataservices.common.DBConstants.DataSourceTypes;
import org.wso2.micro.integrator.dataservices.common.DBConstants.GSpread;
import org.wso2.micro.integrator.dataservices.core.DBUtils;
import org.wso2.micro.integrator.dataservices.core.DataServiceFault;
import org.wso2.micro.integrator.dataservices.core.engine.DataService;
import org.wso2.micro.integrator.dataservices.core.odata.ODataDataHandler;
import org.wso2.micro.integrator.dataservices.core.odata.ODataServiceFault;
import org.wso2.micro.integrator.dataservices.sql.driver.util.GSpreadFeedProcessor;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Map;

/**
 * This class represents a Google Spreadsheet based data source configuration.
 */
public class GSpreadConfig extends Config {

	private static final Log log = LogFactory.getLog(
            GSpreadConfig.class);

	public static final String BASE_WORKSHEET_URL = "https://spreadsheets.google.com/feeds/worksheets/";

	public static final String BASE_REGISTRY_AUTH_TOKEN_PATH = "/repository/components/org.wso2.carbon.dataservices.core/services/";

	private String key;

    private GSpreadFeedProcessor feedProcessor;

	public GSpreadConfig(DataService dataService, String configId, Map<String, String> properties, boolean odataEnable)
            throws DataServiceFault {
		super(dataService, configId, DataSourceTypes.GDATA_SPREADSHEET, properties, odataEnable);

        this.key = extractKey(this.getProperty(GSpread.DATASOURCE));

        String clientId = DBUtils.resolvePasswordValue(this.getDataService(), this.getProperty(GSpread.CLIENT_ID));
        String clientSecret = DBUtils.resolvePasswordValue(this.getDataService(), this.getProperty(GSpread.CLIENT_SECRET));
        String refreshToken = DBUtils.resolvePasswordValue(this.getDataService(), this.getProperty(GSpread.REFRESH_TOKEN));
        String visibility = this.getProperty(GSpread.VISIBILITY);

        try {
            this.feedProcessor = new GSpreadFeedProcessor(clientId, clientSecret, refreshToken,
                                                          visibility, BASE_REGISTRY_AUTH_TOKEN_PATH);
        } catch (SQLException e) {
            throw new DataServiceFault(e, "Error initialising GSpread feed Processor, " + e.getMessage());
        }
		if (!dataService.isServiceInactive()) {
            this.feedProcessor.setService(new SpreadsheetService(this.getDataService().getName() +
                                                                 ":" + this.getConfigId()));
		}
	}

	public static String extractKey(String documentURL) throws DataServiceFault {
		URI documentURI;
		try {
			documentURI = new URI(documentURL);
		} catch (URISyntaxException e) {
			String message = "Document URL Syntax error:" + documentURL;
			log.warn(message,e);
			throw new DataServiceFault(e, message);
		}
		String extractedQuery = documentURI.getQuery();
        if (extractedQuery == null) {
            return getKeyForNewSpreadsheetURLFormat(documentURL);
        }
        int i1 = extractedQuery.lastIndexOf("key=");
        int i2 = extractedQuery.indexOf("&", i1);
        if (i1 == -1) {
            return getKeyForNewSpreadsheetURLFormat(documentURL);
        } else if (i2 < 0) {
			return extractedQuery.substring(i1 + 4);
		} else {
			return extractedQuery.substring(i1 + 4, i2);
		}
	}

    private static String getKeyForNewSpreadsheetURLFormat(String documentURI) throws DataServiceFault {
        String [] params = documentURI.split("/");
        String resultKey = null;
        for (int i = 0; i < params.length; i++) {
            if ("d".equals(params[i])) {
                resultKey = params[i+1];
            }
        }
        if (resultKey == null) {
            throw new DataServiceFault("Invalid URL format");
        }
        return resultKey;
    }

	public URL generateWorksheetFeedURL() throws MalformedURLException {
        return this.feedProcessor.generateWorksheetFeedURL(key);
	}

	public String getKey() {
		return key;
	}


    public <F extends IFeed> F getFeed(URL feedUrl, Class<F> feedClass) throws Exception {
        return feedProcessor.getFeed(feedUrl,feedClass);
    }

	@Override
	public boolean isActive() {
		return this.feedProcessor.getService() != null;
	}

	public void close() {
		/* nothing to close */
	}

	@Override
	public ODataDataHandler createODataHandler() throws ODataServiceFault {
		throw new ODataServiceFault("Expose as OData Service feature doesn't support for the " + getConfigId() +
		                           " Datasource.");
	}

	@Override
	public boolean isResultSetFieldsCaseSensitive() {
		return false;
	}

}
