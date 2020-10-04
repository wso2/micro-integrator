/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.micro.integrator.business.messaging.hl7.common;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import javax.xml.stream.XMLStreamException;

/**
 * Utility class for HL7 operations.
 */
public class HL7Utils {

    public static OMElement generateHL7MessageElement(String hl7XmlMessage) throws XMLStreamException {
        SOAPFactory fac = OMAbstractFactory.getSOAP11Factory();
        OMElement hl7Element = AXIOMUtil.stringToOM(hl7XmlMessage);
        OMNamespace ns = fac.createOMNamespace(HL7Constants.HL7_NAMESPACE, HL7Constants.HL7_ELEMENT_NAME);
        OMElement messageEl = fac.createOMElement(HL7Constants.HL7_MESSAGE_ELEMENT_NAME, ns);
        messageEl.addChild(hl7Element);
        return messageEl;
    }

    public static String streamToString(InputStream in) throws IOException {
        byte[] buff = new byte[10240];
        int i;
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream(10240);
        while ((i = in.read(buff)) > 0) {
            byteOut.write(buff, 0, i);
        }
        byteOut.close();
        return byteOut.toString();
    }

    public static String streamToString(InputStream in, String charSetValue) throws IOException {
        if (charSetValue == null) {
            return streamToString(in);
        } else {
            Charset charSet;
            try {
                charSet = Charset.forName(charSetValue);
            } catch (UnsupportedCharsetException e) {
                throw new AxisFault("Unsupported charset during HL7 message build: " + e.getMessage(), e);
            } catch (IllegalCharsetNameException e) {
                throw new AxisFault("Illegal charset name found during HL7 message build: " + e.getMessage(), e);
            }

            ByteBuffer buffer = ByteBuffer.wrap(IOUtils.toByteArray(in));
            String encStr = new String(buffer.array(), 0, buffer.limit(), charSet);

            return encStr;
        }
    }

}
