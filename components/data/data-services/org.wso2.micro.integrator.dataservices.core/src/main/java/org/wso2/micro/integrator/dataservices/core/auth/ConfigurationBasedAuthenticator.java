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
package org.wso2.micro.integrator.dataservices.core.auth;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.wso2.micro.integrator.dataservices.common.conf.DynamicAuthConfiguration;
import org.wso2.micro.integrator.dataservices.common.conf.DynamicAuthConfiguration.Entry;import org.wso2.micro.integrator.dataservices.core.DataServiceFault;

/**
 * This class represents a dynamic user authenticator based on a static XML configuration.
 */
public class ConfigurationBasedAuthenticator implements DynamicUserAuthenticator {

	private Map<String, String[]> credentialsMap;
	
	public ConfigurationBasedAuthenticator(String xmlConfig) throws DataServiceFault {
		try {
			JAXBContext ctx = JAXBContext.newInstance(DynamicAuthConfiguration.class);
			XMLInputFactory inputFactory = XMLInputFactory.newInstance();
			inputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
			inputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
			XMLStreamReader xmlReader = inputFactory.createXMLStreamReader(new StringReader(xmlConfig));
			Unmarshaller unmarshaller = ctx.createUnmarshaller();
			DynamicAuthConfiguration conf = (DynamicAuthConfiguration) unmarshaller.unmarshal(xmlReader);
			if (conf == null) {
				throw new DataServiceFault("Invalid configuration section " +
						"for dynamic auth configuration:- \n" + xmlConfig);
			}
			this.credentialsMap = new HashMap<String, String[]>();
			for (Entry entry : conf.getEntries()) {
				this.credentialsMap.put(entry.getRequest(), 
						new String[] { entry.getUsername(), entry.getPassword() });
			}
		} catch (Exception e) {
			throw new DataServiceFault(e,
					"Error in creating ConfigurationBasedAuthenticator: " + e.getMessage());
		}
	}
	
	public Map<String, String[]> getCredentialsMap() {
		return credentialsMap;
	}

	@Override
	public String[] lookupCredentials(String user) throws DataServiceFault {
		return this.getCredentialsMap().get(user);
	}

}
