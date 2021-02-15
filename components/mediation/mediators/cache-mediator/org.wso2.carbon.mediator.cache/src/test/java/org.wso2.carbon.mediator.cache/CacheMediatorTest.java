/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.mediator.cache;

import com.google.common.net.HttpHeaders;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.UUIDGenerator;
import org.apache.axiom.util.UIDGenerator;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.description.InOutAxisOperation;
import org.apache.commons.collections.map.MultiValueMap;
import org.apache.synapse.MessageContext;
import org.apache.synapse.config.SynapseConfigUtils;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.transport.nhttp.NhttpConstants;
import org.custommonkey.xmlunit.XMLTestCase;
import org.custommonkey.xmlunit.XMLUnit;
import org.wso2.carbon.mediator.cache.util.HttpCachingFilter;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Test the functionality of the {@link CacheMediatorFactory} and the {@link CacheMediatorSerializer}
 */
public class CacheMediatorTest extends XMLTestCase {

    private static final String mediatorXml =
            "<cache xmlns=\"http://ws.apache.org/ns/synapse\" collector=\"false\" timeout=\"60\" " +
                    "maxMessageSize=\"1000\">\n" +
                    "            <onCacheHit>\n" +
                    "               <log>\n" +
                    "                  <property name=\"name\" value=\"Riyafa\"/>\n" +
                    "               </log>\n" +
                    "               <respond/>\n" +
                    "            </onCacheHit>\n" +
                    "            <protocol type=\"HTTP\">\n" +
                    "               <methods>POST, GET</methods>\n" +
                    "               <headersToExcludeInHash>ab, abc</headersToExcludeInHash>\n" +
                    "               <headersToIncludeInHash>xy, xyz</headersToIncludeInHash>\n" +                    "               <responseCodes>2|5[0-9][0-9]</responseCodes>\n" +
                    "               <enableCacheControl>true</enableCacheControl>\n" +
                    "               <includeAgeHeader>true</includeAgeHeader>\n" +
                    "               <hashGenerator>org.wso2.carbon.mediator.cache.digest" +
                    ".HttpRequestHashGenerator</hashGenerator>\n" +
                    "            </protocol>\n" +
                    "            <implementation maxSize=\"20\"/>\n" +
                    "         </cache>";
    public static final String CACHE_CONTROL_HEADER = "no-cache, no-store, max-age=80";
    private ConfigurationContext configContext;
    private SynapseConfiguration synapseConfig;

    public CacheMediatorTest(String name) {
        super(name);
    }

    public void testMediatorFactory() {
        OMElement mediatorElement = SynapseConfigUtils.stringToOM(mediatorXml);

        CacheMediatorFactory factory = new CacheMediatorFactory();
        CacheMediator mediator =
                (CacheMediator) factory.createSpecificMediator(mediatorElement, new Properties());

        assertFalse("Incorrect value for the collector", mediator.isCollector());
        assertEquals("Incorrect value for the timeout", mediator.getTimeout(), 60);
        assertEquals("Incorrect value for the protocol type", mediator.getProtocolType(), "HTTP");
        assertTrue("Incorrect value for the httpMethodsToCache",
                Arrays.equals(mediator.getHTTPMethodsToCache(), new String[]{"POST", "GET"}));
        assertTrue("Incorrect value for the headersToExcludeInHash",Arrays.equals(mediator.getHeadersToExcludeInHash(), new String[]{"ab", "abc"}));
        assertTrue("Incorrect value for the headersToIncludeInHash",Arrays.equals(mediator.getHeadersToIncludeInHash(), new String[]{"xy", "xyz"}));
        assertEquals("Incorrect value for the for the responsecodes",mediator.getResponseCodes(), "2|5[0-9][0-9]");
        assertEquals("Incorrect value for the hashGenerator",mediator.getDigestGenerator().getClass().getName(),
                "org.wso2.carbon.mediator.cache.digest.HttpRequestHashGenerator");
        assertEquals("Incorrect value for the maxSize",mediator.getInMemoryCacheSize(), 20);
        assertEquals("Incorrect value for the enableCacheControl",mediator.isCacheControlEnabled(), true);
        assertEquals("Incorrect value for the includeAgeHeader",mediator.isAddAgeHeaderEnabled(), true);
    }


    public void testMediatorSerializer() {
        OMElement mediatorElement = SynapseConfigUtils.stringToOM(mediatorXml);

        CacheMediatorFactory factory = new CacheMediatorFactory();
        CacheMediator mediator =
                (CacheMediator) factory.createSpecificMediator(mediatorElement, new Properties());
        CacheMediatorSerializer serializer = new CacheMediatorSerializer();
        OMElement serializedMediatorElement = serializer.serializeSpecificMediator(mediator);

        XMLUnit.setIgnoreWhitespace(true);

        try {
            assertXMLEqual(serializedMediatorElement.toString(), mediatorXml);
        } catch (Exception ignored) {
        }
    }

    /**
     * Test case for isValidCacheEntry() with no-store header.
     *
     * @throws AxisFault when exception happens on message context creation.
     */
    public void testIsNoStore() throws AxisFault {
        MessageContext synCtx = createMessageContext();
        org.apache.axis2.context.MessageContext msgCtx =  ((Axis2MessageContext) synCtx).getAxis2MessageContext();

        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaders.CACHE_CONTROL, CACHE_CONTROL_HEADER);
        if (msgCtx != null) {
            msgCtx.setProperty("TRANSPORT_HEADERS", headers);
            ((Axis2MessageContext) synCtx).setAxis2MessageContext(msgCtx);
        }
        assertEquals("no-store cache-control does not exist.", HttpCachingFilter.isNoStore(msgCtx), true);
    }

    /**
     * Test case for isValidCacheEntry() with no-cache and ETag headers.
     *
     * @throws AxisFault when exception happens on message context creation.
     */
    public void testIsValidResponseWithNoCache() throws AxisFault {
        CachableResponse cachedResponse = new CachableResponse();
        MessageContext synCtx = createMessageContext();
        ConcurrentHashMap<String, Object> headers = new ConcurrentHashMap<>();
        headers.put(HttpHeaders.CACHE_CONTROL, CACHE_CONTROL_HEADER);
        headers.put(HttpHeaders.ETAG, "2046-64-77-50-35-75-11038-459-486126-71-58");
        cachedResponse.setHeaderProperties(headers);
        assertEquals("no-cache or ETag header does not exist.",
                HttpCachingFilter.isValidCacheEntry(cachedResponse, synCtx), true);
    }

    /**
     * Test case for isValidCacheEntry() with an expired cache.
     *
     * @throws AxisFault when exception happens on message context creation.
     */
    public void testIsValidResponseWithExpiredCache() throws AxisFault {
        CachableResponse cachedResponse = new CachableResponse();
        MessageContext synCtx = createMessageContext();
        ConcurrentHashMap<String, Object> httpHeaders = new ConcurrentHashMap<>();

        httpHeaders.put(HttpHeaders.CACHE_CONTROL, CACHE_CONTROL_HEADER);
        cachedResponse.setHeaderProperties(httpHeaders);

        //Set the response fetched time with an old date time.
        cachedResponse.setResponseFetchedTime(System.currentTimeMillis() - 100000);
        assertEquals("Cached response does not expired.",
                HttpCachingFilter.isValidCacheEntry(cachedResponse, synCtx), true);
    }

    /**
     * Test case for isValidCacheEntry() with a valid cache response.
     *
     * @throws AxisFault when exception happens on message context creation.
     */
    public void testIsValidResponseWithValidCache() throws AxisFault, ParseException {
        CachableResponse cachedResponse = new CachableResponse();
        MessageContext synCtx = createMessageContext();
        Map<String, String> headers = new HashMap<>();
        ConcurrentHashMap<String, Object> httpHeaders = new ConcurrentHashMap<>();

        httpHeaders.put(HttpHeaders.CACHE_CONTROL, CACHE_CONTROL_HEADER);
        cachedResponse.setHeaderProperties(httpHeaders);

        HttpCachingFilter.setResponseCachedTime(headers, cachedResponse);

        assertEquals("Cached response is expired.",
                HttpCachingFilter.isValidCacheEntry(cachedResponse, synCtx), false);
    }

    /**
     * Test case for setAgeHeader() method
     */
    public void testSetAgeHeader() {
        CachableResponse cachedResponse = new CachableResponse();
        cachedResponse.setResponseFetchedTime(System.currentTimeMillis() - 3000);
        org.apache.axis2.context.MessageContext msgCtx = new org.apache.axis2.context.MessageContext();
        HttpCachingFilter.setAgeHeader(cachedResponse, msgCtx);
        Map excessHeaders = (MultiValueMap) msgCtx.getProperty(NhttpConstants.EXCESS_TRANSPORT_HEADERS);
        assertTrue(excessHeaders.get("Age") != null);
    }

    /**
     * Tests case for setResponseCachedTime() method.
     *
     * @throws ParseException on exception while parsing the date
     */
    public void testSetResponseCachedTime() throws ParseException {
        CachableResponse cachedResponse = new CachableResponse();
        Map<String, String> headers = new HashMap<>();
        DateFormat dateFormat = new SimpleDateFormat(CachingConstants.DATE_PATTERN);
        String responseOriginatedTime = dateFormat.format(new Date());
        headers.put("Date", responseOriginatedTime);
        HttpCachingFilter.setResponseCachedTime(headers, cachedResponse);
        assertEquals(dateFormat.format(cachedResponse.getResponseFetchedTime()), responseOriginatedTime);
    }

    /**
     * Create Axis2 Message Context.
     *
     * @return msgCtx created message context.
     * @throws AxisFault when exception happens on message context creation.
     */
    private MessageContext createMessageContext() throws AxisFault {
        MessageContext msgCtx = createSynapseMessageContext();
        org.apache.axis2.context.MessageContext axis2MsgCtx = ((Axis2MessageContext) msgCtx).getAxis2MessageContext();
        axis2MsgCtx.setServerSide(true);
        axis2MsgCtx.setMessageID(UUIDGenerator.getUUID());

        return msgCtx;
    }

    /**
     * Create Synapse Context.
     *
     * @return mc created message context.
     * @throws AxisFault when exception happens on message context creation.
     */
    private MessageContext createSynapseMessageContext() throws AxisFault {
        org.apache.axis2.context.MessageContext axis2MC = new org.apache.axis2.context.MessageContext();
        axis2MC.setConfigurationContext(this.configContext);
        ServiceContext svcCtx = new ServiceContext();
        OperationContext opCtx = new OperationContext(new InOutAxisOperation(), svcCtx);
        axis2MC.setServiceContext(svcCtx);
        axis2MC.setOperationContext(opCtx);
        Axis2MessageContext mc = new Axis2MessageContext(axis2MC, this.synapseConfig, null);
        mc.setMessageID(UIDGenerator.generateURNString());
        mc.setEnvelope(OMAbstractFactory.getSOAP12Factory().createSOAPEnvelope());
        mc.getEnvelope().addChild(OMAbstractFactory.getSOAP12Factory().createSOAPBody());

        return mc;
    }
}
