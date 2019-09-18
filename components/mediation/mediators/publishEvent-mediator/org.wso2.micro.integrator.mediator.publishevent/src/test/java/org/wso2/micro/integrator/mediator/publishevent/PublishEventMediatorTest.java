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

import org.apache.axiom.om.OMElement;
import org.apache.synapse.config.SynapseConfigUtils;
import org.custommonkey.xmlunit.XMLTestCase;
import org.custommonkey.xmlunit.XMLUnit;
import org.wso2.micro.integrator.core.internal.MicroIntegratorBaseConstants;

import java.io.File;
import java.util.Properties;

public class PublishEventMediatorTest extends XMLTestCase {

	private static final String mediatorXml =
			"<publishEvent xmlns=\"http://ws.apache.org/ns/synapse\">\n" +
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

	public PublishEventMediatorTest(String name) {
		super(name);
	}

	public void testMediatorFactory() {
		OMElement mediatorElement = SynapseConfigUtils.stringToOM(mediatorXml);
		System.setProperty(MicroIntegratorBaseConstants.CARBON_HOME, System.getProperty("user.dir") + File.separator +
		                                                "src" + File.separator + "test" + File.separator + "resources");

		PublishEventMediatorFactory factory = new PublishEventMediatorFactory();
		PublishEventMediator mediator =
				(PublishEventMediator) factory.createSpecificMediator(mediatorElement, new Properties());

		assertEquals(mediator.getStreamName(), "stream_3");
		assertEquals(mediator.getStreamVersion(), "1.0.0");
		assertEquals(mediator.getEventSinkName(), "bam_event_sink");
		assertEquals(mediator.getMetaProperties().size(), 2);
		assertEquals(mediator.getCorrelationProperties().size(), 1);
		assertEquals(mediator.getPayloadProperties().size(), 2);

		assertEquals(mediator.getMetaProperties().get(0).getKey(), "method");
		assertEquals(mediator.getMetaProperties().get(0).getType(), "STRING");
		assertEquals(mediator.getMetaProperties().get(0).getDefaultValue(), "rre");
		assertEquals(mediator.getMetaProperties().get(0).getExpression().toString(),
		             "get-property('axis2', 'HTTP_METHOD')");

		assertEquals(mediator.getMetaProperties().get(1).getKey(), "to");
		assertEquals(mediator.getMetaProperties().get(1).getType(), "STRING");
		assertEquals(mediator.getMetaProperties().get(1).getDefaultValue(), "deffff");
		assertEquals(mediator.getMetaProperties().get(1).getExpression().toString(), "get-property('To')");

		assertEquals(mediator.getCorrelationProperties().get(0).getKey(), "date");
		assertEquals(mediator.getCorrelationProperties().get(0).getType(), "STRING");
		assertEquals(mediator.getCorrelationProperties().get(0).getDefaultValue(), "rre");
		assertEquals(mediator.getCorrelationProperties().get(0).getExpression().toString(),
		             "get-property('SYSTEM_DATE')");

		assertEquals(mediator.getPayloadProperties().get(0).getKey(), "symbol");
		assertEquals(mediator.getPayloadProperties().get(0).getType(), "STRING");
		assertEquals(mediator.getPayloadProperties().get(0).getDefaultValue(), "AAPL");
		assertEquals(mediator.getPayloadProperties().get(0).getExpression().toString(),
		             "$body/m0:getQuote/m0:request/m0:symbol");

		assertEquals(mediator.getPayloadProperties().get(1).getKey(), "by_value");
		assertEquals(mediator.getPayloadProperties().get(1).getType(), "INTEGER");
		assertEquals(mediator.getPayloadProperties().get(1).getDefaultValue(), "100");
		assertEquals(mediator.getPayloadProperties().get(1).getValue().toString(), "1001");
	}

	public void testDummy() {

	}

	public void _testMediatorSerializer() {
		OMElement mediatorElement = SynapseConfigUtils.stringToOM(mediatorXml);
		System.setProperty(MicroIntegratorBaseConstants.CARBON_HOME, System.getProperty("user.dir") + File.separator +
		                                                "src" + File.separator + "test" + File.separator + "resources");

		PublishEventMediatorFactory factory = new PublishEventMediatorFactory();
		PublishEventMediator mediator =
				(PublishEventMediator) factory.createSpecificMediator(mediatorElement, new Properties());
		PublishEventMediatorSerializer serializer = new PublishEventMediatorSerializer();
		OMElement serializedMediatorElement = serializer.serializeSpecificMediator(mediator);

		XMLUnit.setIgnoreWhitespace(true);

		try {
			assertXMLEqual(serializedMediatorElement.toString(), mediatorXml);
		} catch (Exception e) {
		}
	}
}