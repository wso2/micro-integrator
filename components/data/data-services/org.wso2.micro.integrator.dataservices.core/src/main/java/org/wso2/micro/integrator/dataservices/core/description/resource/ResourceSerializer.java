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
package org.wso2.micro.integrator.dataservices.core.description.resource;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.wso2.micro.integrator.dataservices.common.DBConstants.DBSFields;
import org.wso2.micro.integrator.dataservices.core.DBUtils;
import org.wso2.micro.integrator.dataservices.core.description.query.QuerySerializer;

/**
 * This class represents the serializing functionality of a Resource.
 * @see Resource
 */
public class ResourceSerializer {

	public static OMElement serializeResource(Resource resource) {
		OMFactory fac = DBUtils.getOMFactory();
		OMElement resEl = fac.createOMElement(new QName(DBSFields.RESOURCE));
		Resource.ResourceID resourceId = resource.getResourceId();
		resEl.addAttribute(DBSFields.PATH, resourceId.getPath(), null);
		resEl.addAttribute(DBSFields.METHOD, resourceId.getMethod(), null);
		String description = resource.getDescription();
		if (!DBUtils.isEmptyString(description)) {
			OMElement desEl = fac.createOMElement(new QName(DBSFields.DESCRIPTION));
			desEl.setText(description);
			resEl.addChild(desEl);
		}
		QuerySerializer.serializeCallQuery(resource.getCallQuery(), resEl, fac);
		return resEl;
	}
	
}
