/*
 * Copyright 2005-2007 WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.micro.core.transports.util;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.engine.AxisConfiguration;
import org.wso2.micro.core.util.SystemFilter;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

public class ServiceHTMLProcessor {

    public static String printServiceHTML(String serviceName,
                                          ConfigurationContext configurationContext) {
        StringBuffer temp = new StringBuffer();
        try {
            AxisConfiguration axisConfig = configurationContext.getAxisConfiguration();
            AxisService axisService = axisConfig.getService(serviceName);
            if (axisService != null) {
                if (!axisService.isActive()) {
                    temp.append("<b>Service ").append(serviceName).
                            append(" is inactive. Cannot display service information.</b>");
                } else {
                    temp.append("<h3>").append(axisService.getName()).append("</h3>");
                    temp.append("<a href=\"").append(axisService.getName()).append("?wsdl\">wsdl</a> : ");
                    temp.append("<a href=\"").append(axisService.getName()).append("?xsd\">schema</a> : ");
                    temp.append("<a href=\"").append(axisService.getName()).append("?policy\">policy</a><br/>");
                    temp.append("<i>Service Description :  ").
                            append(axisService.getDocumentation()).append("</i><br/><br/>");

                    for (Iterator pubOpIter = axisService.getPublishedOperations().iterator();
                         pubOpIter.hasNext();) {
                        temp.append("Published operations <ul>");
                        for (; pubOpIter.hasNext();) {
                            AxisOperation axisOperation = (AxisOperation) pubOpIter.next();
                            temp.append("<li>").
                                    append(axisOperation.getName().getLocalPart()).append("</li>");
                        }
                        temp.append("</ul>");
                    }
                }
            } else {
                temp.append("<b>Service ").append(serviceName).
                        append(" not found. Cannot display service information.</b>");
            }
            return "<html><head><title>Service Information</title></head>" + "<body>" + temp
                   + "</body></html>";
        }
        catch (AxisFault axisFault) {
            return "<html><head><title>Error Occurred</title></head>" + "<body>"
                   + "<hr><h2><font color=\"blue\">" + axisFault.getMessage() + "</font></h2></body></html>";
        }
    }
}
