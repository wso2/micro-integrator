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

import org.wso2.micro.integrator.dataservices.core.engine.ParamValue;
import org.wso2.micro.integrator.dataservices.core.validation.ValidationContext;
import org.wso2.micro.integrator.dataservices.core.validation.ValidationException;
import org.wso2.micro.integrator.dataservices.core.validation.Validator;

import java.util.List;

/**
 * Abstract validator class implementation which contains the mostly used validator functionality.
 */
public abstract class GenericValidator implements Validator {

	private String message;

	public GenericValidator(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	public void validate(ValidationContext context, String name,
                         ParamValue value) throws ValidationException {
		if (value.getValueType() == ParamValue.PARAM_VALUE_SCALAR) {
			if (!validateScalar(value.getScalarValue())) {
                throw new ValidationException(this.getMessage(), name, value);
			}
		} else if (value.getValueType() == ParamValue.PARAM_VALUE_ARRAY) {
			List<ParamValue> arrayVal = value.getArrayValue();
			int n = arrayVal.size();
			for (int i = 0; i < n; i++) {
				if (!validateScalar(arrayVal.get(i).toString())) {
					throw new ValidationException(this.getMessage()
							+ " at array index " + i, name, value);
				}
			}
		}
	}

	protected abstract boolean validateScalar(String value);

}
