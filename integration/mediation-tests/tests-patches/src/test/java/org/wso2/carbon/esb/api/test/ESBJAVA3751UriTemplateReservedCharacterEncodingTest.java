package org.wso2.carbon.esb.api.test;

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

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.test.utils.http.client.HttpRequestUtil;
import org.wso2.carbon.automation.test.utils.http.client.HttpResponse;
import org.wso2.esb.integration.common.utils.CarbonLogReader;
import org.wso2.esb.integration.common.utils.ESBIntegrationTest;

public class ESBJAVA3751UriTemplateReservedCharacterEncodingTest extends ESBIntegrationTest {
    private CarbonLogReader carbonLogReader;

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init();
        carbonLogReader = new CarbonLogReader();
        carbonLogReader.start();
    }

    @Test(groups = { "wso2.esb" }, description = "Sending http request with a query param consist of"
            + " reserved character : ")
    public void testURITemplateExpandWithPercentEncoding() throws Exception {
        carbonLogReader.clearLogs();
        boolean isPercentEncoded = false;
        HttpResponse response = HttpRequestUtil
                .sendGetRequest(getApiInvocationURL("services/client/urlEncoded?queryParam=ESB:WSO2"), null);
        isPercentEncoded = carbonLogReader.checkForLog("ESB%3AWSO2", DEFAULT_TIMEOUT);
        Assert.assertTrue(isPercentEncoded,
                "Reserved character should be percent encoded while uri-template expansion");

    }

    @Test(groups = { "wso2.esb" }, description = "Sending http request with a query param consist of reserved "
            + "character : with percent encoding escaped at uri-template expansion")
    public void testURITemplateExpandWithEscapedPercentEncoding() throws Exception {
        boolean isPercentEncoded = false;
        carbonLogReader.clearLogs();
        HttpResponse response = HttpRequestUtil
                .sendGetRequest(getApiInvocationURL("services/client/escapeUrlEncoded?queryParam=ESB:WSO2"), null);
        isPercentEncoded = carbonLogReader.checkForLog("ESB%3AWSO2", DEFAULT_TIMEOUT);
        Assert.assertFalse(isPercentEncoded,
                "Reserved character should not be percent encoded while uri-template expansion as escape enabled");

    }

    @Test(groups = { "wso2.esb" }, description = "Sending http request with a path param consist of"
            + " reserved character : ")
    public void testURITemplateExpandWithPercentEncodingPathParamCase() throws Exception {
        carbonLogReader.clearLogs();
        boolean isPercentEncoded = false;
        HttpResponse response = HttpRequestUtil
                .sendGetRequest(getApiInvocationURL("services/client/urlEncoded/ESB:WSO2"), null);
        isPercentEncoded = carbonLogReader.checkForLog("To: /services/test_2/ESB%3AWSO2", DEFAULT_TIMEOUT);
        Assert.assertTrue(isPercentEncoded,
                "Reserved character should be percent encoded while uri-template expansion");

    }

    @Test(groups = { "wso2.esb" }, description = "Sending http request with a path param consist of reserved "
            + "character : with percent encoding escaped at uri-template expansion")
    public void testURITemplateExpandWithEscapedPercentEncodingPathParam() throws Exception {
        carbonLogReader.clearLogs();
        boolean isPercentEncoded = false;
        HttpResponse response = HttpRequestUtil
                .sendGetRequest(getApiInvocationURL("services/client/escapeUrlEncoded/ESB:WSO2"), null);
        isPercentEncoded = carbonLogReader.checkForLog("To: /services/test_2/ESB%3AWSO2", DEFAULT_TIMEOUT);
        Assert.assertFalse(isPercentEncoded,
                "Reserved character should not be percent encoded while uri-template expansion as escape enabled");

    }

    @Test(groups = { "wso2.esb" }, description = "Sending http request with a query param consist of"
            + " reserved space character ")
    public void testURITemplateParameterDecodingSpaceCharacterCase() throws Exception {
        carbonLogReader.clearLogs();
        boolean isPercentEncoded = false;
        boolean isMessageContextPropertyPercentDecoded = false;
        HttpResponse response = HttpRequestUtil
                .sendGetRequest(getApiInvocationURL("services/client/urlEncoded?queryParam=ESB%20WSO2"), null);
        String decodedMessageContextProperty = "decodedQueryParamValue = ESB WSO2";
        isMessageContextPropertyPercentDecoded =
                carbonLogReader.checkForLog(decodedMessageContextProperty, DEFAULT_TIMEOUT);
        isPercentEncoded = carbonLogReader.checkForLog("ESB%20WSO2", DEFAULT_TIMEOUT);
        Assert.assertTrue(isMessageContextPropertyPercentDecoded,
                "Uri-Template parameters should be percent decoded at message context property");
        Assert.assertTrue(isPercentEncoded,
                "Reserved character should be percent encoded while uri-template expansion");
    }

    @Test(groups = { "wso2.esb" }, description = "Sending http request with a query param consist of"
            + " reserved + character ")
    public void testURITemplateParameterDecodingPlusCharacterCase() throws Exception {
        carbonLogReader.clearLogs();
        boolean isPercentEncoded = false;
        boolean isMessageContextPropertyPercentDecoded = false;
        HttpResponse response = HttpRequestUtil
                .sendGetRequest(getApiInvocationURL("services/client/urlEncoded?queryParam=ESB+WSO2"), null);
        String decodedMessageContextProperty = "decodedQueryParamValue = ESB+WSO2";
        isMessageContextPropertyPercentDecoded =
                carbonLogReader.checkForLog(decodedMessageContextProperty, DEFAULT_TIMEOUT);
        isPercentEncoded = carbonLogReader.checkForLog("ESB%2BWSO2", DEFAULT_TIMEOUT);
        Assert.assertTrue(isMessageContextPropertyPercentDecoded,
                "Uri-Template parameters should be percent decoded at message context property");
        Assert.assertTrue(isPercentEncoded,
                "Reserved character should be percent encoded while uri-template expansion");
    }

    @Test(groups = { "wso2.esb" }, description = "Sending http request with a query param consist of"
            + " reserved + character ")
    public void testURITemplateParameterDecodingWithPercentEncodingEscapedAtExpansion() throws Exception {
        carbonLogReader.clearLogs();
        boolean isPercentEncoded = false;
        boolean isMessageContextPropertyPercentDecoded = false;
        HttpResponse response = HttpRequestUtil
                .sendGetRequest(getApiInvocationURL("services/client/escapeUrlEncoded?queryParam=ESB+WSO2"), null);
        String decodedMessageContextProperty = "decodedQueryParamValue = ESB+WSO2";
        isMessageContextPropertyPercentDecoded = carbonLogReader.checkForLog(decodedMessageContextProperty, DEFAULT_TIMEOUT);
        isPercentEncoded = carbonLogReader.checkForLog("ESB%2BWSO2", DEFAULT_TIMEOUT);
        Assert.assertTrue(isMessageContextPropertyPercentDecoded,
                "Uri-Template parameters should be percent decoded at message context property");
        Assert.assertFalse(isPercentEncoded,
                "Reserved character should not be percent encoded while uri-template expansion");
    }

    @Test(groups = { "wso2.esb" }, description = "Sending http request with a path param consist of"
            + " whole URL including protocol , host , port etc. ")
    public void testURITemplateSpecialCaseVariableWithFullURL() throws Exception {
        carbonLogReader.clearLogs();
        boolean isPercentEncoded = false;
        HttpResponse response = HttpRequestUtil.sendGetRequest(
                getApiInvocationURL("services/client/special_case/http://localhost:8480/services/test_2/special_case"),
                null);
        isPercentEncoded = carbonLogReader.checkForLog("To: /services/test_2/special_case", DEFAULT_TIMEOUT);
        Assert.assertTrue(isPercentEncoded,
                "The Special case of of Full URL expansion should be identified and should not percent encode full URL");
    }

    @AfterClass(alwaysRun = true)
    public void destroy() throws Exception {
        carbonLogReader.stop();
    }
}
