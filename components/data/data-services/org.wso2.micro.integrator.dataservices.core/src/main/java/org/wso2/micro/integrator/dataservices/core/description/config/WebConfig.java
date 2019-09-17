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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.webharvest.definition.ScraperConfiguration;
import org.webharvest.runtime.Scraper;
import org.webharvest.runtime.variables.Variable;
import org.wso2.micro.integrator.dataservices.common.DBConstants;
import org.wso2.micro.integrator.dataservices.core.DBUtils;
import org.wso2.micro.integrator.dataservices.core.DataServiceFault;
import org.wso2.micro.integrator.dataservices.core.engine.DataService;
import org.wso2.micro.integrator.dataservices.core.odata.ODataDataHandler;
import org.wso2.micro.integrator.dataservices.core.odata.ODataServiceFault;
import org.xml.sax.InputSource;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;


/*
Create the configuration to create web content as a data service
 */
public class WebConfig extends Config {

    private static final Log log = LogFactory.getLog(
            WebConfig.class);

    /*path of the web harvest configuration exists*/
    private String webHarvestConfigPath;

    public WebConfig(DataService dataService, String configId, Map<String, String> properties, boolean odataEnable) {
        super(dataService, configId, DBConstants.DataSourceTypes.WEB, properties, odataEnable);
        this.webHarvestConfigPath = this.getProperty("web_harvest_config");
    }

    public Scraper getScraperConfig() throws DataServiceFault, IOException {
        Scraper scraper;
        ScraperConfiguration scraperConfiguration;
        InputStream in;
        try {
            /* For the given file path of the web harvest configuration */
            if (!webHarvestConfigPath.trim().startsWith("<config>")) {
                in = DBUtils.getInputStreamFromPath(this.webHarvestConfigPath);
                //scraperConfiguration = new ScraperConfiguration(webHarvestConfigPath);
            } else {
                /* If the Web harvest configuration has provided */
                in = new ByteArrayInputStream(webHarvestConfigPath.getBytes());
            }
            InputSource inputSource = new InputSource(in);
            scraperConfiguration = new ScraperConfiguration(inputSource);
            scraper = new Scraper(scraperConfiguration, "");  
            return scraper;
        } catch (FileNotFoundException e) {
            throw new DataServiceFault(e, "Error in reading web harvest configuration");
        }
    }

    /*executing the web scraper*/
    public Variable getScrapedResult(String queryVariable) throws DataServiceFault {
        try {
            Scraper scraper = getScraperConfig();
            scraper.execute();
            return (Variable) scraper.getContext().get(queryVariable);
        } catch (Exception e) {
            throw new DataServiceFault(e, "Error in Scraper Execution");
        }

    }

    @Override
    public boolean isActive() {
        try {
            Scraper scraper = getScraperConfig();
            return scraper != null;
        } catch (Exception e) {
        	log.error("Error in checking Web config availability", e);
            return false;
        }
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
