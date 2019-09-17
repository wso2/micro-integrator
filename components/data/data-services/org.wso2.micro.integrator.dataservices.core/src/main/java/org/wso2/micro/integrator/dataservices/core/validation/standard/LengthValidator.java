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

/**
 * Represents a validator which checks for the length of a given value.
 */
public class LengthValidator extends GenericValidator {

	private int minLength;

	private int maxLength;
	
	private boolean hasMin;
	
	private boolean hasMax;

	public LengthValidator(int minLength, int maxLength, boolean hasMin, boolean hasMax) {
		super("The value length must be between " + minLength + " and "
				+ maxLength);
		this.minLength = minLength;
		this.maxLength = maxLength;
		this.hasMin = hasMin;
		this.hasMax = hasMax;
	}

	public int getMinLength() {
		return minLength;
	}

	public int getMaxLength() {
		return maxLength;
	}

	public boolean isHasMin() {
		return hasMin;
	}

	public boolean isHasMax() {
		return hasMax;
	}

	protected boolean validateScalar(String value) {
		try {
			int min = this.getMinLength();
			if (this.isHasMin()) {
				if (value.length() < min) {
					return false;
				}
			}
			int max = this.getMaxLength();
			if (this.isHasMax()) {
				if (value.length() > max) {
					return false;
				}
			}
			return true;
		} catch (Exception e) {
			return false;
		}
	}

}
