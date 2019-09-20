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
package org.wso2.micro.integrator.mediator.publishevent;

import junit.framework.Assert;
import org.apache.axiom.om.OMElement;
import org.apache.synapse.config.SynapseConfigUtils;
import org.custommonkey.xmlunit.XMLTestCase;
import org.wso2.micro.integrator.core.internal.MicroIntegratorBaseConstants;

import java.io.File;
import java.util.Properties;

/**
 *  This class is used to test PublishEvent mediator's synchronous behavior
 */
public class PublishEventMediatorSyncTest extends XMLTestCase {

    private static final String mediatorXml =
            "<publishEvent async=\"false\" xmlns=\"http://ws.apache.org/ns/synapse\">\n" +
                    "    <eventSink>bam_event_sink</eventSink>\n" +
                    "    <streamName>stream_3</streamName>\n" +
                    "    <streamVersion>1.0.0</streamVersion>\n" +
                    "    <attributes>\n" +
                    "        <meta>\n" +
                    "            <attribute name=\"method\" type=\"STRING\" defaultValue=\"rre\" expression=\"get-property('axis2', 'HTTP_METHOD')\"/>\n" +
                    "            <attribute name=\"to\" type=\"STRING\" defaultValue=\"deffff\" expression=\"get-property('To')\" />\n" +
                    "        </meta>\n" +
                    "        <correlation>\n" +
                    "            <attribute name=\"date\" type=\"STRING\" defaultValue=\"rre\" expression=\"get-property('SYSTEM_DATE')\" />\n" +
                    "        </correlation>\n" +
                    "        <payload>\n" +
                    "            <attribute name=\"symbol\" type=\"STRING\" defaultValue=\"AAPL\" expression=\"$body/m0:getQuote/m0:request/m0:symbol\" xmlns:m0=\"http://services.samples\" />\n" +
                    "            <attribute name=\"by_value\" type=\"INTEGER\" defaultValue=\"100\" value=\"1001\" />\n" +
                    "        </payload>\n" +
                    "    </attributes>\n" +
                    "</publishEvent>";

    private static final String mediatorXml2 =
            "<publishEvent async=\"false\" timeout=\"2000\" xmlns=\"http://ws.apache.org/ns/synapse\">\n" +
                    "    <eventSink>bam_event_sink</eventSink>\n" +
                    "    <streamName>stream_3</streamName>\n" +
                    "    <streamVersion>1.0.0</streamVersion>\n" +
                    "    <attributes>\n" +
                    "        <meta>\n" +
                    "            <attribute name=\"method\" type=\"STRING\" defaultValue=\"rre\" expression=\"get-property('axis2', 'HTTP_METHOD')\"/>\n" +
                    "            <attribute name=\"to\" type=\"STRING\" defaultValue=\"deffff\" expression=\"get-property('To')\" />\n" +
                    "        </meta>\n" +
                    "        <correlation>\n" +
                    "            <attribute name=\"date\" type=\"STRING\" defaultValue=\"rre\" expression=\"get-property('SYSTEM_DATE')\" />\n" +
                    "        </correlation>\n" +
                    "        <payload>\n" +
                    "            <attribute name=\"symbol\" type=\"STRING\" defaultValue=\"AAPL\" expression=\"$body/m0:getQuote/m0:request/m0:symbol\" xmlns:m0=\"http://services.samples\" />\n" +
                    "            <attribute name=\"by_value\" type=\"INTEGER\" defaultValue=\"100\" value=\"1001\" />\n" +
                    "        </payload>\n" +
                    "    </attributes>\n" +
                    "</publishEvent>";

    /**
     *  Tests whether the async property is set correctly to false in the mediator
     */
    public void testMediatorFactorySyncFalse() {
        OMElement mediatorElement = SynapseConfigUtils.stringToOM(mediatorXml);
        System.setProperty(MicroIntegratorBaseConstants.CARBON_HOME,
                System.getProperty("user.dir") + File.separator + "src" + File.separator + "test" + File.separator
                        + "resources");

        PublishEventMediatorFactory factory = new PublishEventMediatorFactory();
        PublishEventMediator mediator = (PublishEventMediator) factory
                .createSpecificMediator(mediatorElement, new Properties());
        Assert.assertFalse("isAsync property is true", mediator.isAsync());
    }

    /**
     *  Tests whether the timeout property is unset when async is set to to false
     */
    public void testMediatorFactorySyncFalseWithTimeout() {
        OMElement mediatorElement = SynapseConfigUtils.stringToOM(mediatorXml2);
        System.setProperty(MicroIntegratorBaseConstants.CARBON_HOME,
                System.getProperty("user.dir") + File.separator + "src" + File.separator + "test" + File.separator
                        + "resources");

        PublishEventMediatorFactory factory = new PublishEventMediatorFactory();
        PublishEventMediator mediator = (PublishEventMediator) factory
                .createSpecificMediator(mediatorElement, new Properties());
        Assert.assertEquals("timeout value should not affect sync publishing", 0, mediator.getAsyncTimeout());
    }
}
