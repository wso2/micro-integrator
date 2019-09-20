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

import org.wso2.micro.integrator.dataservices.common.DBConstants.DataTypes;
import org.wso2.micro.integrator.dataservices.common.DBConstants.QueryTypes;
import org.wso2.micro.integrator.dataservices.common.DBConstants.XSDTypes;
import org.wso2.micro.integrator.dataservices.common.DBConstants.QueryParamTypes;
import org.wso2.micro.integrator.dataservices.core.DataServiceFault;
import org.wso2.micro.integrator.dataservices.core.validation.Validator;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Query param is an input parameter associated with a query,
 * e.g. INTEGER, STRING.
 */
public class QueryParam {

	/** name of parameter */
	private String name; 
	
	/** i.e. STRING, INTEGER, etc.. */
	private String sqlType;  

	/** i.e. IN, OUT, INOUT */
	private String type; 
	
	/** i.e. SCALAR, ARRAY */
	private String paramType; 
		
	/** ordinals of the parameter */
	private Set<Integer> ordinals; 

	/** default value of the parameter */
    private ParamValue defaultValue;
    
    /** validators assigned to this param */
    private List<Validator> validators;

    private String structType;

    private boolean forceDefault;

    private boolean optional;

	public QueryParam(String name, String sqlType, String type, String paramType,
                      int ordinal, ParamValue defaultValue, String structType,
                      List<Validator> validators, boolean forceDefault, boolean optional) throws DataServiceFault {
		this.name = name;
		this.sqlType = sqlType;
		this.type = type;
		this.paramType = paramType;
		this.ordinals = new TreeSet<Integer>();
		this.ordinals.add(ordinal);
        this.defaultValue = defaultValue;
        this.structType = structType;
        this.validators = validators;
        this.forceDefault = forceDefault;
        this.optional = optional;
        /* validate the current query param */
        this.validateQueryParam();
	}
	
	private void validateQueryParam() throws DataServiceFault {
		try {
			/* validate the sqlType, i.e. STRING, INTEGER .. */
			Class<DataTypes> typesInterface = DataTypes.class;
			Field[] typeFields = typesInterface.getFields();
			Class<XSDTypes> xsdTypesInterface = XSDTypes.class;
			Field[] xsdTypeFields = xsdTypesInterface.getFields();
			boolean typeFound = false;
			for (Field typeField : typeFields) {
				if (typeField.get(null).equals(this.getSqlType())) {
					typeFound = true;
					break;
				}
			}
			for (Field xsdtypeField : xsdTypeFields) {
				if (xsdtypeField.get(null).equals(this.getSqlType())) {
					typeFound = true;
					break;
				}
			}
			if (!typeFound) {
				throw new DataServiceFault("Invalid query param sqlType: '" +
						this.getSqlType() + "'.");
			}
			/* validate type, i.e. IN, INOUT, OUT */
			if (!(QueryTypes.IN.equals(this.getType()) ||
					QueryTypes.OUT.equals(this.getType()) ||
					QueryTypes.INOUT.equals(this.getType()))) {
				throw new DataServiceFault("Invalid query param type: '" + this.getType() + "'.");
			}
			/* validate paramType, i.e. SCALAR, ARRAY .. */
			if (!(QueryParamTypes.SCALAR.equals(this.getParamType()) ||
					QueryParamTypes.ARRAY.equals(this.getParamType()))) {
				throw new DataServiceFault("Invalid query param type: '" +
						this.getParamType() + "'.");
			}
            /* validate SQL Type struct. Here, if the SQL type is defined as STRUCT, it should
            * carry the mandatory attribute named structType*/
            if (DataTypes.STRUCT.equals(this.getSqlType()) &&
                    ("".equals(this.getStructType()) || this.getStructType() == null)) {
                throw new DataServiceFault("Unable to find SQL type name corresponding to the " +
                        "specified Struct");
            }
		} catch (Exception e) {
			throw new DataServiceFault(e);
		}
	}
	
	public List<Validator> getValidators() {
		return validators;
	}

	public String getName() {
		return name;
	}

	public int getOrdinal() {
		return this.ordinals.iterator().next();
	}
	
	public Set<Integer> getOrdinals() {
		return ordinals;
	}

	public String getSqlType() {
		return sqlType;
	}
	
	public String getType() {
		return type;
	}
	
	public String getParamType() {
		return paramType;
	}

    public ParamValue getDefaultValue() {
        return defaultValue;
    }

    public String getStructType() {
        return structType;
    }

    public boolean isForceDefault() {
	return forceDefault;
    }

    public boolean isOptional() {
	return optional;
	}

    public boolean hasDefaultValue() {
    	return this.getDefaultValue() != null;
    }

    /**
     * This method is here to retain backward compatibility.
     */
    public void setOrdinal(int ordinal) {
        this.clearOrdinals();
        this.ordinals.add(ordinal);
    }
		
    public void addOrdinal(int ordinal) {
    	this.ordinals.add(ordinal);
    }
    
    public void clearOrdinals() {
    	this.ordinals.clear();
    }
    
}
