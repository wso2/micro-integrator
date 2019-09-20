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
import java.util.Set;

/**
 * Represents one row of data.
 */
public class DataEntry {

	private Map<String, ParamValue> values;

	public DataEntry() {
		this.values = new HashMap<String, ParamValue>();
	}

	public Map<String, ParamValue> getData() {
		return values;
	}

	public void addValue(String name, ParamValue value) {
		this.getData().put(name, value);
	}
	
	public ParamValue getValue(String name) {
		return this.getData().get(name);
	}
	
	public Set<String> getNames() {
		return this.getData().keySet();
	}
	
}
