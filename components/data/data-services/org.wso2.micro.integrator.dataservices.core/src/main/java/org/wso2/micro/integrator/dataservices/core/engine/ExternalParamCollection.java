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
package org.wso2.micro.integrator.dataservices.core.engine;

import java.util.HashMap;
import java.util.Map;

import org.wso2.micro.integrator.dataservices.common.DBConstants.DBSFields;

/**
 * Represents a collection of ExternalParam objects.
 */
public class ExternalParamCollection {

	/**
	 * External parameters for columns
	 */
	private Map<String, ExternalParam> columnEntries;

	/**
	 * External parameters for query params
	 */
	private Map<String, ExternalParam> queryParamEntries;

	/**
	 * Temp params used for situations such as, default values etc..
	 */
	private Map<String, ParamValue> tmpEntries;

	public ExternalParamCollection() {
		this.columnEntries = new HashMap<String, ExternalParam>();
		this.queryParamEntries = new HashMap<String, ExternalParam>();
		this.tmpEntries = new HashMap<String, ParamValue>();
	}

	public Map<String, ParamValue> getTempEntries() {
		return tmpEntries;
	}

	public void addParam(ExternalParam param) {
		if (DBSFields.COLUMN.equals(param.getType())) {
			this.columnEntries.put(param.getName(), param);
		} else {
			this.queryParamEntries.put(param.getName(), param);
		}
	}

	public void addTempParam(String name, ParamValue value) {
		this.getTempEntries().put(name, value);
	}

	public ParamValue getTempParam(String name) {
		return this.getTempEntries().get(name);
	}

	public void clearTempValues() {
		this.getTempEntries().clear();
	}

	public ExternalParam getParam(String type, String name) {
		if (DBSFields.COLUMN.equals(type)) {
			return this.columnEntries.get(name);
		} else {
			return this.queryParamEntries.get(name);
		}
	}

    public ExternalParam getParam(String name) {
        ExternalParam param = this.getParam(DBSFields.QUERY_PARAM, name);
        if (param == null) {
            param = this.getParam(DBSFields.COLUMN, name);
        }
        return param;
    }
    
}
