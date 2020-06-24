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
package org.wso2.micro.integrator.dataservices.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMText;
import org.wso2.micro.integrator.dataservices.common.DBConstants;
import org.wso2.micro.integrator.dataservices.common.DBConstants.FaultCodes;
import org.wso2.micro.integrator.dataservices.core.engine.DataService;
import org.wso2.micro.integrator.dataservices.core.engine.ParamValue;

/**
 * This class represents exceptions that occur in data services. 
 */
public class DataServiceFault extends Exception {

	private static final long serialVersionUID = 1L;
	
	/**
	 * The type of data service fault, this can be used to identify what kind
	 * of an error condition occurred.
	 */
	private String code;
	
	/**
	 * The detailed explanation of the data service fault.
	 */
	private String dsFaultMessage;
	
	/**
	 * The originating data service of the exception, if available. 
	 */
	private DataService sourceDataService;
	
	/**
	 * The on-going operation/resource name when the data service fault occurs, if available.
	 */
	private String currentRequestName;
	
	/**
	 * The current parameters of the current operation/resource, if available.
	 */
	private Map<String, ParamValue> currentParams;

    /**
     * This map contains all the properties related to data services fault message
     */
    private Map<String, Object> propertyMap = new HashMap<String, Object>();
	
	public DataServiceFault(Throwable nestedException, String code, String dsFaultMessage) {
		super(nestedException);
		this.code = code;
		this.dsFaultMessage = dsFaultMessage;
		if (this.code == null) {
			this.code = extractFaultCode(nestedException);
		}
	}

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static OMElement extractFaultMessage(Throwable throwable) {
        if (throwable instanceof DataServiceFault) {
            if (throwable.getCause() instanceof XMLStreamException) {
                return extractFaultMessage(((XMLStreamException) throwable.getCause()).getNestedException());
            }
            OMFactory fac = OMAbstractFactory.getOMFactory();
            OMElement root = fac.createOMElement(new QName(
                    DBConstants.WSO2_DS_NAMESPACE, DBConstants.DS_FAULT_ELEMENT));
            OMNamespace ns = root.getNamespace();
            for (Map.Entry<String, Object> rootEntry : ((DataServiceFault) throwable).getPropertyMap().entrySet()) {
                OMElement keyElement = fac.createOMElement(rootEntry.getKey(), ns);
                if (rootEntry.getValue() instanceof Map) {
                    for (Map.Entry dataServiceEntry : (Set<Map.Entry>) ((Map) rootEntry.getValue()).entrySet()) {
                        OMElement dataServiceKeyElement = fac.createOMElement(
                                dataServiceEntry.getKey().toString(), ns);
                        OMText dataServiceValueElement = fac.createOMText(
                                dataServiceKeyElement, dataServiceEntry.getValue().toString());
                        dataServiceKeyElement.addChild(dataServiceValueElement);
                        keyElement.addChild(dataServiceKeyElement);
                    }
                } else {
                    OMText valueElement = fac.createOMText(
                            keyElement, rootEntry.getValue().toString());
                    keyElement.addChild(valueElement);
                }
                root.addChild(keyElement);
            }
            return root;
        } else if (throwable instanceof XMLStreamException) {
            return extractFaultMessage(((XMLStreamException) throwable).getNestedException());
        } else if (throwable != null) {
            Throwable cause = throwable.getCause();
            if (cause != null) {
                return extractFaultMessage(cause);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }
	
	public static String extractFaultCode(Throwable throwable) {
		if (throwable instanceof DataServiceFault) {
			return ((DataServiceFault) throwable).getCode();
		} else if (throwable instanceof XMLStreamException) { 
			return extractFaultCode(((XMLStreamException) throwable).getNestedException());
		} else if (throwable != null) {
			Throwable cause = throwable.getCause();
			if (cause != null) {
				return extractFaultCode(cause);
			} else {
				return FaultCodes.UNKNOWN_ERROR;
			}
		} else {
			return FaultCodes.UNKNOWN_ERROR; 
		}
	}
	
	public DataServiceFault(Throwable nestedException) {
		this(nestedException, null, null);
	}
	
	public DataServiceFault(Throwable nestedException, String dsFaultMessage) {
		this(nestedException, null, dsFaultMessage);
	}
	
	public DataServiceFault(String code, String dsFaultMessage) {
		this(null, code, dsFaultMessage);
	}
	
	public DataServiceFault(String dsFaultMessage) {
		this(null, null, dsFaultMessage);
	}
	
	public String getCode() {
		return code;
	}
	
	public String getDsFaultMessage() {
		return dsFaultMessage;
	}
	
	@Override
	public String getMessage() {
		return this.getFullMessage();
	}
	
	/**
	 * Returns a detailed description of the data service fault.
	 */
	public String getFullMessage() {
		StringBuffer buff = new StringBuffer();
		if (this.getDsFaultMessage() != null) {
			buff.append("DS Fault Message: " + this.getDsFaultMessage() + "\n");
		}
		if (this.getCode() != null) {
			buff.append("DS Code: " + this.getCode() + "\n");
            getPropertyMap().put(DBConstants.FaultParams.DS_CODE, this.getCode());
		}
		if (this.getSourceDataService() != null) {
			buff.append("Source Data Service:-\n");
			buff.append(this.getSourceDataService().toString());
            Map<String, String> sourcePropertyMap = new HashMap<String, String>();
            sourcePropertyMap.put(DBConstants.FaultParams.DATA_SERVICE_NAME, this.getSourceDataService().getName());
            sourcePropertyMap.put(DBConstants.FaultParams.LOCATION, this.getSourceDataService().getRelativeDsLocation());
            sourcePropertyMap.put(DBConstants.FaultParams.DESCRIPTION, this.getSourceDataService().getDescription() != null ?
                    this.getSourceDataService().getDescription() : "N/A");
            sourcePropertyMap.put(DBConstants.FaultParams.DEFAULT_NAMESPACE, this.getSourceDataService().getDefaultNamespace());
            getPropertyMap().put(DBConstants.FaultParams.SOURCE_DATA_SERVICE, sourcePropertyMap);
		}
		if (this.getCurrentRequestName() != null) {
			buff.append("Current Request Name: " + this.getCurrentRequestName() + "\n");
            getPropertyMap().put(DBConstants.FaultParams.CURRENT_REQUEST_NAME, this.getCurrentRequestName());
		}
		if (this.getCurrentParams() != null && !("true".equalsIgnoreCase(DBUtils.getCurrentParamsDisabledProperty()))) {
			buff.append("Current Params: " + this.getCurrentParams() + "\n");
            getPropertyMap().put(DBConstants.FaultParams.CURRENT_PARAMS, this.getCurrentParams().toString());
		}
		if (this.getCause() != null) {			
			buff.append("Nested Exception:-\n" + this.getCause() + "\n");
            getPropertyMap().put(DBConstants.FaultParams.NESTED_EXCEPTION, this.getCause().toString());
		}
		return buff.toString();
	}
	
	@Override
	public String toString() {
		return this.getFullMessage();
	}

	public Map<String, ParamValue> getCurrentParams() {
		return currentParams;
	}

	public void setCurrentParams(Map<String, ParamValue> currentParams) {
		this.currentParams = currentParams;
	}

	public String getCurrentRequestName() {
		return currentRequestName;
	}

	public void setCurrentRequestName(String currentRequestName) {
		this.currentRequestName = currentRequestName;
	}

	public DataService getSourceDataService() {
		return sourceDataService;
	}

	public void setSourceDataService(DataService sourceDataService) {
		this.sourceDataService = sourceDataService;
	}

    public Map<String, Object> getPropertyMap() {
        return propertyMap;
    }
}
