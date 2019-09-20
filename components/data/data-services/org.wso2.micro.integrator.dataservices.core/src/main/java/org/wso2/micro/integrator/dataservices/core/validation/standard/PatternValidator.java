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
package org.wso2.micro.integrator.dataservices.core.validation.standard;

import java.util.regex.Pattern;

/**
 * Represents a validator which checks if the parameters satisfy the given regular expression.
 */
public class PatternValidator extends GenericValidator {
	
	private Pattern pattern;
	
	public PatternValidator(String regEx) {
		super("Pattern {" + regEx + "} is expected");
		this.pattern = Pattern.compile(regEx);
	}
	
	public Pattern getPattern() {
		return pattern;
	}
	
	protected boolean validateScalar(String value) {
		try {
		    return this.getPattern().matcher(value).matches();
		} catch (Exception e) {
			return false;
		}
	}
}
