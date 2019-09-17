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
 * Represents a validator which check a double value range.
 */
public class DoubleRangeValidator extends GenericValidator {

	private double minimum;

	private double maximum;
	
	private boolean hasMin;
	
	private boolean hasMax;

	public DoubleRangeValidator(double minimum, double maximum, boolean hasMin, boolean hasMax) {
		super("The double value range expected is [" + minimum + "," + maximum + "]");
		this.minimum = minimum;
		this.maximum = maximum;
		this.hasMin = hasMin;
		this.hasMax = hasMax;
	}

	public double getMinimum() {
		return minimum;
	}

	public double getMaximum() {
		return maximum;
	}

	public boolean isHasMin() {
		return hasMin;
	}

	public boolean isHasMax() {
		return hasMax;
	}

	protected boolean validateScalar(String value) {
		try {
			double doubleVal = Double.parseDouble(value);
			double min = this.getMinimum();
			if (this.isHasMin()) {
				if (doubleVal < min) {
					return false;
				}
			}
			double max = this.getMaximum();
			if (this.isHasMax()) {
				if (doubleVal > max) {
					return false;
				}
			}
			return true;
		} catch (Exception e) {
			return false;
		}
	}

}
