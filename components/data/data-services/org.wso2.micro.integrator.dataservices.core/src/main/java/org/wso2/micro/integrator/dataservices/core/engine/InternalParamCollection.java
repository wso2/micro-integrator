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

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

/**
 * Represents a collection of InternalParam objects.
 */
public class InternalParamCollection {

	private Map<Integer, InternalParam> data;

	public InternalParamCollection() {
		/* A tree map is used here to sort the entries by the keys, which is required
		 * when getParams() is called. The computation complexity of this, which is log(n)
		 * compared to hash map, which is log(1) will not be much of a difference for smaller
		 * number of entries, and can even be faster even if the size if small enough */
		this.data = new TreeMap<Integer, InternalParam>();
	}

	/**
	 * Returns the parameters sorted by the parameter ordinal.
	 */
	public Collection<InternalParam> getParams() {
		return data.values();
	}

	public Map<Integer, InternalParam> getData() {
		return data;
	}

	public void addParam(InternalParam param) {
		this.getData().put(param.getOrdinal(), param);
	}
	
	public InternalParam getParam(int ordinal) {
		return this.getData().get(ordinal);
	}
	
	public int getSize() {
		return this.data.size();
	}
	
	public void remove(int i) {
	    this.getData().remove(i);
	}
	
}
