/*
 *Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *WSO2 Inc. licenses this file to you under the Apache License,
 *Version 2.0 (the "License"); you may not use this file except
 *in compliance with the License.
 *You may obtain a copy of the License at
 *
 *http://www.apache.org/licenses/LICENSE-2.0
 *
 *Unless required by applicable law or agreed to in writing,
 *software distributed under the License is distributed on an
 *"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *KIND, either express or implied.  See the License for the
 *specific language governing permissions and limitations
 *under the License.
 */
package org.wso2.carbon.esb.mediator.test.db.dbreport;

import javax.xml.namespace.QName;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.apache.axiom.om.OMElement;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.test.utils.dbutils.H2DataBaseManager;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;
import org.wso2.esb.integration.common.utils.common.XPathConstants;

import static org.testng.Assert.assertEquals;
import static org.wso2.carbon.esb.mediator.test.db.DBTestUtil.getDBPath;

public class DBReportMediatorTestCase extends ESBIntegrationTest {

    private H2DataBaseManager h2DataBaseManager;

    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {
        super.init();
        AutomationContext automationContext = new AutomationContext();
        String DB_USER = automationContext.getConfigurationValue(XPathConstants.DATA_SOURCE_DB_USER_NAME);
        String DB_PASSWORD = automationContext.getConfigurationValue(XPathConstants.DATA_SOURCE_DB_PASSWORD);
        String JDBC_URL = automationContext.getConfigurationValue(XPathConstants.DATA_SOURCE_URL);
        String databasePath = getDBPath("testdb_dbreport");

        JDBC_URL = JDBC_URL + databasePath + ";AUTO_SERVER=TRUE";
        h2DataBaseManager = new H2DataBaseManager(JDBC_URL, DB_USER, DB_PASSWORD);
        h2DataBaseManager.executeUpdate("CREATE TABLE company(price double, name varchar(20))");
        super.init();
    }

    /*  Before a request is sent to the db mediator, the count of price rows greater than 1000 should
        be 3. After the request is gone through db mediator, the count should be zero. Price values
        greater than 1000 will remain with the count of one. */
    @SetEnvironment(executionEnvironments = { ExecutionEnvironment.STANDALONE })
    @Test(groups = "wso2.esb", description = "DBLookup/DBReport mediator should replace a" + " &lt;/&gt; with </>")
    public void DBMediatorReplaceLessThanAndGreaterThanSignTestCase() throws Exception {
        h2DataBaseManager.executeUpdate("INSERT INTO company VALUES(100,'ABC')");
        h2DataBaseManager.executeUpdate("INSERT INTO company VALUES(2000,'XYZ')");
        h2DataBaseManager.executeUpdate("INSERT INTO company VALUES(200,'CDE')");
        h2DataBaseManager.executeUpdate("INSERT INTO company VALUES(300,'MNO')");
        int numOfPrice, numOfPriceGreaterThan;
        numOfPrice = getRecordCount("SELECT price from company WHERE price < 1000 ");
        numOfPriceGreaterThan = getRecordCount("SELECT price from company WHERE price > 1000 ");
        assertEquals(numOfPrice, 3, "Fault, invalid response");
        assertEquals(numOfPriceGreaterThan, 1, "Fault, invalid response");
        axis2Client.sendSimpleStockQuoteRequest(getProxyServiceURLHttp("dbReportMediatorTestProxy"), null, "WSO2");
        numOfPrice = getRecordCount("SELECT price from company WHERE price < 1000 ");
        numOfPriceGreaterThan = getRecordCount("SELECT price from company WHERE price > 1000 ");
        assertEquals(numOfPrice, 0, "Fault, invalid response");
        assertEquals(numOfPriceGreaterThan, 1, "Fault, invalid response");

    }

    /*  Before a request is sent, the database has "200.0"(WSO2_PRICE) as the value corresponding to
        the 'name' "WSO2".
        After the request is sent, the value 200.0 is replaced by the value given by xpath to response
        message content. */
    @SetEnvironment(executionEnvironments = { ExecutionEnvironment.STANDALONE })
    @Test(groups = "wso2.esb", description = "Insert or update DB table using message contents.")
    public void DBReportUseMessageContentTestCase() throws Exception {
        double price = 200.0;
        OMElement response;
        String priceMessageContent;
        h2DataBaseManager.executeUpdate("INSERT INTO company VALUES(100.0,'ABC')");
        h2DataBaseManager.executeUpdate("INSERT INTO company VALUES(2000.0,'XYZ')");
        h2DataBaseManager.executeUpdate("INSERT INTO company VALUES(" + price + ",'WSO2')");
        h2DataBaseManager.executeUpdate("INSERT INTO company VALUES(300.0,'MNO')");

        priceMessageContent = getPrice();
        assertEquals(priceMessageContent, Double.toString(price), "Fault, invalid response");
        response = axis2Client
                .sendSimpleStockQuoteRequest(getProxyServiceURLHttp("dbReportMediatorUsingMessageContentTestProxy"),
                        null, "WSO2");
        priceMessageContent = getPrice();
        OMElement returnElement = response.getFirstElement();
        OMElement lastElement = returnElement.getFirstChildWithName(new QName("http://services.samples/xsd", "last"));
        assertEquals(priceMessageContent, lastElement.getText(), "Fault, invalid response");
    }

    @AfterClass(alwaysRun = true)
    public void close() throws Exception {
        h2DataBaseManager.disconnect();
        h2DataBaseManager = null;
        super.cleanup();
    }

    private int getRecordCount(String sql) throws SQLException {
        ResultSet rs = null;
        Statement stm = null;
        try {
            stm = h2DataBaseManager.getStatement(sql);
            rs = stm.executeQuery(sql);
            int count = 0;
            while (rs.next()) {
                count++;
            }
            return count;
        } finally {
            releaseResources(stm, rs);
        }
    }

    private String getPrice() throws SQLException {
        ResultSet rs = null;
        Statement stm = null;
        try {
            String price = null;
            String sql = "SELECT price from company WHERE name = 'WSO2'";
            stm = h2DataBaseManager.getStatement(sql);
            rs = stm.executeQuery(sql);
            while (rs.next()) {
                price = Double.toString(rs.getDouble("price"));
            }
            return price;
        } finally {
            releaseResources(stm, rs);
        }
    }

    private void releaseResources(Statement stm, ResultSet rs) throws SQLException {
        if (rs != null) {
            rs.close();
        }
        if (stm != null) {
            stm.close();
        }
    }
}
