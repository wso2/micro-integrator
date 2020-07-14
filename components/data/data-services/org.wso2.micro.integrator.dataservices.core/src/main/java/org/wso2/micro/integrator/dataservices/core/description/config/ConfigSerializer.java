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

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.wso2.micro.integrator.dataservices.common.DBConstants;
import org.wso2.micro.integrator.dataservices.common.DBConstants.DBSFields;

import java.util.Map.Entry;
import javax.xml.namespace.QName;

/**
 * This class represents the serializing functionality of a Config.
 * @see Config
 */
public class ConfigSerializer {

	private static final String PWD_MASKED_VALUE = "*****";

	public static OMElement serializeConfig(Config config, boolean isUiSerialization) {
		OMFactory fac = OMAbstractFactory.getOMFactory();
		OMElement configEl = fac.createOMElement(new QName(DBSFields.CONFIG));
		String configId = config.getConfigId();
		if (configId != null) {
			configEl.addAttribute(DBSFields.ID, config.getConfigId(), null);
		}
		OMElement propEl;
		for (Entry<String, String> entry : config.getProperties().entrySet()) {
			propEl = fac.createOMElement(new QName(DBSFields.PROPERTY));
			propEl.addAttribute(DBSFields.NAME, entry.getKey(), null);
			if (isUiSerialization && entry.getKey().contains(DBConstants.RDBMS.PASSWORD)) {
				propEl.setText(PWD_MASKED_VALUE);
			} else {
				propEl.setText(entry.getValue());
			}
			configEl.addChild(propEl);
		}
		return configEl;
	}	
}
