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
package org.wso2.micro.integrator.dataservices.core.test.util;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.impl.builder.StAXBuilder;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

public class TestUtils {

	private static final String XSD_SCHEMA = "http://www.w3.org/2001/XMLSchema";

	public static String DEFAULT_DS_WS_NAMESPACE = "http://ws.wso2.org/dataservice";

	public static final String CUSTOMER_XSD_PATH = "./src/test/resources/xsd/customers.xsd";

	public static final String PRODUCTS_XSD_PATH = "./src/test/resources/xsd/products.xsd";

	public static final String ORDER_COUNT_XSD_PATH = "./src/test/resources/xsd/order_count.xsd";
	
	public static final String PAYMENTS_XSD_PATH = "./src/test/resources/xsd/payments.xsd";
	
	public static final String PAYMENTS_WITH_DATETIME_XSD_PATH = "./src/test/resources/xsd/payments_with_date_time.xsd";
	
	public static final String ORDERS_XSD_PATH = "./src/test/resources/xsd/orders.xsd";
	
	public static final String ORDERS_WITH_DATETIME_PATH = "./src/test/resources/xsd/orders_with_date_time.xsd";
	
	public static final String PAYMENT_INFO_NESTED_XSD_PATH = "./src/test/resources/xsd/payment_info_nested.xsd";
	
	public static final String PAYMENT_INFO_NESTED_WITH_DATE_TIME_XSD_PATH = "./src/test/resources/xsd/payment_info_nested_with_date_time.xsd";
	
	public static final String ORDER_DETAILS_NESTED_WITH_DATETIME_XSD_PATH = "./src/test/resources/xsd/order_details_nested_with_date_time.xsd";
	
	public static final String ORDER_DETAILS_NESTED_XSD_PATH = "./src/test/resources/xsd/order_details_nested.xsd";
	
	public static final String OFFICES_XSD_PATH = "./src/test/resources/xsd/offices.xsd";

	/**
	 * Calls an operation of a target web service with the given parameters and
	 * returns the result.
	 * 
	 * @param epr
	 *            End point reference of the service
	 * @param opName
	 *            Operation to be called in the service
	 * @param params
	 *            Parameters of the service call
	 * @return Service results
	 * @throws AxisFault
	 */
	public static OMElement callOperation(String epr, String opName,
			Map<String, String> params) throws AxisFault {
		EndpointReference targetEPR = new EndpointReference(epr);
		OMElement payload = getPayload(opName, params);
		Options options = new Options();
		options.setTo(targetEPR);
		options.setAction("urn:" + opName);
		ServiceClient sender = new ServiceClient();
		sender.setOptions(options);
		OMElement result = sender.sendReceive(payload);
		return result;
	}

        /**
         * Calls an update operation of a target web service with the given parameters and  does not
         * returns the result.
         *
         * @param epr
         *            End point reference of the service
         * @param opName
         *            Operation to be called in the service
         * @param params
         *            Parameters of the service call
         * @throws AxisFault
         */

        public static void callUpdateOperation(String epr, String opName,
                                              Map<String, String> params) {
                EndpointReference targetEPR = new EndpointReference(epr);
                OMElement payload = getPayload(opName, params);
                Options options = new Options();
                options.setTo(targetEPR);
                options.setAction("urn:" + opName);
                ServiceClient sender = null;
                try {
                        sender = new ServiceClient();
                        sender.setOptions(options);
                        sender.sendRobust(payload);
                } catch (AxisFault axisFault) {
                        axisFault.printStackTrace();
                }
        }
	
	/**
	 * Calls an operation of a target web service with the given parameters and
	 * returns the result, support array types.
	 * 
	 * @param epr
	 *            End point reference of the service
	 * @param opName
	 *            Operation to be called in the service
	 * @param params
	 *            Parameters of the service call
	 * @return Service results
	 * @throws AxisFault
	 */
	public static OMElement callOperationWithArray(String epr, String opName,
			List<List<String>> params) throws AxisFault {
		EndpointReference targetEPR = new EndpointReference(epr);
		OMElement payload = getPayload(opName, params);
		Options options = new Options();
		options.setTo(targetEPR);
		options.setAction("urn:" + opName);
		ServiceClient sender = new ServiceClient();
		sender.setOptions(options);
		OMElement result = sender.sendReceive(payload);
		return result;
	}
	
	/**
	 * Calls a REST operation of a target web service with the given parameters and
	 * returns the result.
	 * @param epr
	 *            End point reference of the service
	 * @param opName
	 *            Operation to be called in the service
	 * @param params
	 *            Parameters of the service call
	 * @param requestMethod
	 *            HTTP request method - only supports GET/POST.
	 * @return Service results
	 * @throws Exception
	 */
	public static OMElement getAsResource(String epr, String opName,
			Map<String, String> params, String requestMethod) throws Exception {
		String requestUrl = epr;
		if (!requestUrl.endsWith("/")) {
			requestUrl += "/";
		}
		requestUrl += opName;
		if (params != null) {
			String paramsStr = "";
			for (String key : params.keySet()) {
				paramsStr += "/" + params.get(key);
			}
			paramsStr = URLEncoder.encode(paramsStr, "UTF-8");
			requestUrl += paramsStr;
		}
		if (requestMethod.toUpperCase().equals("GET")) {
			URL url = new URL(requestUrl);
			URLConnection urlConn = url.openConnection();
			urlConn.setDoInput(true);
			StAXBuilder builder = new StAXOMBuilder(urlConn.getInputStream());
			OMElement omEl = builder.getDocumentElement();
			builder.close();
			return omEl;
		} else if (requestMethod.toUpperCase().equals("POST")) {
			URL url = new URL(requestUrl);
			URLConnection urlConn = url.openConnection();
			urlConn.setDoOutput(true);
			urlConn.getOutputStream().close();
			StAXBuilder builder = new StAXOMBuilder(urlConn.getInputStream());
			OMElement omEl = builder.getDocumentElement();
			builder.close();
			return omEl;
		} else {
			throw new IllegalArgumentException(
					"getAsResource() - HTTP method '" + requestMethod + "' not supported");
		}
	}

	private static OMElement getPayload(String opName,
			Map<String, String> params) {
		OMFactory omFac = OMAbstractFactory.getOMFactory();
		OMNamespace omNs = omFac.createOMNamespace(
				"http://example1.org/example1", "example1");
		OMElement method = omFac.createOMElement(opName, omNs);

		if (params != null) {
			OMElement paramEl = null;
			for (String key : params.keySet()) {
				paramEl = omFac.createOMElement(key, omNs);
				paramEl.setText(params.get(key));
				method.addChild(paramEl);
			}
		}
		return method;
	}
	
	private static OMElement getPayload(String opName,
			List<List<String>> params) {
		OMFactory omFac = OMAbstractFactory.getOMFactory();
		OMNamespace omNs = omFac.createOMNamespace(
				"http://example1.org/example1", "example1");
		OMElement method = omFac.createOMElement(opName, omNs);

		if (params != null) {
			OMElement paramEl = null;
			for (List<String> list : params) {
				paramEl = omFac.createOMElement(list.get(0), omNs);
				paramEl.setText(list.get(1));
				method.addChild(paramEl);
			}
		}
		return method;
	}

	/**
	 * prints out the given message
	 * 
	 * @param msg
	 *            message to be printed
	 */
	public static void showMessage(String msg) {
		System.out.println("\n###########################################");
		System.out.println("Testing service : " + msg);
		System.out.println("###########################################");
	}

	public static String getFirstValue(OMElement el, String path, String ns)
			throws Exception {
		AXIOMXPath xpathe = formatXPath(path, ns);
		return ((OMElement) xpathe.selectSingleNode(el)).getText();
	}
	
	public static boolean evalExpression(OMElement el, String path, String ns)
			throws Exception {
		AXIOMXPath xpathe = formatXPath(path, ns);
		return xpathe.booleanValueOf(el);
	}

	private static AXIOMXPath formatXPath(String path, String ns)
			throws Exception {
		if (!path.startsWith("//")) {
			path = "/" + path;
		}
		path = path.substring(0, 1) + path.substring(1).replaceAll("/", "/ns:");
		AXIOMXPath xpathe = new AXIOMXPath(path);
		xpathe.addNamespace("ns", ns);
		return xpathe;
	}

	public static boolean validateResultStructure(OMElement result,
			String schemaPath) throws Exception {
		SchemaFactory fac = SchemaFactory.newInstance(XSD_SCHEMA);
		Source source = new StreamSource(new FileInputStream(schemaPath));
		Schema schema = fac.newSchema(source);
		Validator validator = schema.newValidator();
		try {
			validator.validate(new StreamSource(new StringReader(result
					.toString())));
		} catch (Exception e) {
			return false;
		}
		return true;
	}

    public static void checkForService(String url) throws AxisFault {
        int defaultTimeout = 5000; //5seconds
        checkForService(url, defaultTimeout);
    }

    /**
     * method to wait until the given service is up and running
     *
     * @param url
     * @param timeout
     * @throws AxisFault
     */
    public static void checkForService(String url, int timeout) throws AxisFault {
        url = url.replaceFirst("^https", "http");
        url = url + "?wsdl";
        System.out.println("Checking availability of url " + url);
        int count = 1;
        while (true) {
            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(timeout);
                connection.setReadTimeout(timeout);
                int responseCode = connection.getResponseCode();
                if (200 <= responseCode && responseCode <= 399) {
                    System.out.println("Url " + url + " is available");
                    break;
                }
                System.out.println("Connection attempt no " + count + " unsuccessful");
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e1) {
                    throw new AxisFault("Thread interuptted", e1);
                }
            } catch (IOException exception) {
                System.out.println("Connection attempt no " + count + " failed due to " + exception.getMessage());
            }
            count++;
            if (count > 30) {
                int timeInMins = (count * 10000) / 60000;
                System.out.println("Connection failed to url " + url + " for " + timeInMins + " minutes");
                throw new AxisFault("Connection failed to url " + url + " for " + timeInMins + " minutes");
            }
        }

    }

    /**
     * method to kill existing servers which are bind to the given port
     *
     * @param port
     */
    public static void shutdownFailsafe(int port) {
        try {
            System.out.println("method to kill already existing servers in port " + port);
            Process p = Runtime.getRuntime().exec("lsof -Pi tcp:" + port);
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    p.getInputStream()));
            String line;
            reader.readLine();
            line = reader.readLine();
            if (line != null) {
                line = line.trim();
                String processId = line.split(" +")[1];
                System.out.println("There is already a process using " + port + ", process id is - " + processId);
                if (processId != null) {
                    String killStr = "kill -9 " + processId;
                    System.out.println("kill string to kill the process - " + killStr);
                    Runtime.getRuntime().exec(killStr);

                    System.out.println("process " + processId + " killed successfully, which was running on port " + port);
                }
            } else {
                System.out.println("There are no existing processes running on port "+ port);
            }
        } catch (Exception e) {
            System.out.println("Error killing the process which uses the port " + port);
        }
    }

}
