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
package org.wso2.micro.integrator.dataservices.core.datasource;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class represents an implementation of <code>DataRow</code> interface,
 * which is a data row which has a fixed data amount.
 */
public class FixedDataRow implements DataRow {

	private Map<String, String> values;
	
	public FixedDataRow(Map<String, String> values) {
		this.values = new ConcurrentHashMap<String, String>(values);
	}
	
	public Map<String, String> getValues() {
		return values;
	}

	@Override
	public String getValueAt(String column) {
		return this.getValues().get(column);
	}
	
	public void update(Map<String, String> values) {
		this.getValues().putAll(values);
	}
	
}
