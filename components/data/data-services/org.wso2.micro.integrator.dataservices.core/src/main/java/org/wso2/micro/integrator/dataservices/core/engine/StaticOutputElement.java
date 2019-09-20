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

import org.apache.axis2.databinding.types.NCName;
import org.apache.axis2.databinding.utils.ConverterUtil;
import org.wso2.micro.integrator.dataservices.common.DBConstants;
import org.wso2.micro.integrator.dataservices.common.DBConstants.DBSFields;
import org.wso2.micro.integrator.dataservices.common.DBConstants.FaultCodes;
import org.wso2.micro.integrator.dataservices.core.DBUtils;
import org.wso2.micro.integrator.dataservices.core.DSSessionManager;
import org.wso2.micro.integrator.dataservices.core.DataServiceFault;
import org.wso2.micro.integrator.dataservices.core.boxcarring.TLParamStore;
import org.wso2.micro.integrator.dataservices.core.description.query.SQLQuery;
import org.wso2.micro.integrator.dataservices.core.dispatch.DispatchStatus;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.InputStream;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Struct;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.List;
import java.util.Set;

/**
 * Represents a static entry in a Result element.
 */
public class StaticOutputElement extends OutputElement {

    /**
     * original param value, without any modifications: toLowerCase
     */
    private String originalParam;

    /**
     * i.e. element, attribute
     */
    private String elementType;

    /**
     * i.e. string, decimal, etc..
     */
    private QName xsdType;

    /**
     * i.e. XML, RDF etc..
     */
    private int resultType;

    /**
     * i.e. VALUE, REFERENCE etc..
     */
    private int dataCategory;

    /**
     * Exports the values in this element,
     * these will be saved in a thread local storage,
     * which can be re-used later by other queries
     */
    private String export;

    /**
     * The type of value to be exported, i.e. SCALAR, ARRAY
     */
    private int exportType;

    /**
     * A flag to keep if this output element's value is a constant,
     * i.e. paramType = 'value'
     */
    private boolean hasConstantValue;

    /* Represents whether this element corresponds to a User Defined Object such as UDT or
       SQLArray */
    private boolean isUserDefinedObj;

    /* If this element corresponds to a UDT then that UDT's metadata */
    private UDT udtInfo;

    /**
     * This is the regex to filter out invalid characters w.r.t XML 1.0
     * @see <a href="https://www.w3.orcg/TR/REC-xml/#charsets">https://www.w3.org/TR/REC-xml/#charsets</a>
     */
    private static final String NON_PRINTABLE_CHARS = "[^\u0009\r\n\u0020-\uD7FF\uE000-\uFFFD\ud800\udc00-\udbff\udfff"
            + "]";

    public StaticOutputElement(DataService dataService, String name,
                               String param, String originalParam, String paramType,
                               String elementType, String namespace, QName xsdType,
                               Set<String> requiredRoles, int dataCategory, int resultType,
                               String export, int exportType,
                               String arrayName) throws DataServiceFault {
        super(name, namespace, requiredRoles, param, paramType, arrayName);
        this.originalParam = originalParam;
        this.elementType = elementType;
        this.xsdType = xsdType;
        this.dataCategory = dataCategory;
        this.resultType = resultType;
        this.export = export;
        this.exportType = exportType;
        this.hasConstantValue = DBSFields.VALUE.equals(paramType);
        this.udtInfo = processParamForUserDefinedObjects(this.getParam());
        if (this.getArrayName() != null || this.getUDTInfo() != null) {
            this.isUserDefinedObj = true;
        }

        /* validate element/attribute name */
        if (!NCName.isValid(this.getName())) {
            throw new DataServiceFault("Invalid output " + this.elementType + " name: '" +
                    this.getName() + "', must be an NCName.");
        }
    }

    public boolean hasConstantValue() {
        return hasConstantValue;
    }

    public String getExport() {
        return export;
    }

    public int getExportType() {
        return exportType;
    }

    public int getDataCategory() {
        return dataCategory;
    }

    public int getResultType() {
        return resultType;
    }

    public String getOriginalParam() {
        return originalParam;
    }

    public QName getXsdType() {
        return xsdType;
    }

    public String getElementType() {
        return elementType;
    }

    public boolean isUserDefinedObj() {
        return isUserDefinedObj;
    }

    public UDT getUDTInfo() {
        return udtInfo;
    }

    /**
     * Checks whether this output element corresponds to a UDT and if so an object of UDT
     * class is populated.
     *
     * @param param Initial column name specified for the output element
     * @return An instance of UDT class
     * @throws DataServiceFault If any error occurs while determining the nest indices of a UDT
     */
    private UDT processParamForUserDefinedObjects(String param) throws DataServiceFault {
        String udtColumnName = DBUtils.extractUDTObjectName(param);
        if (udtColumnName != null) {
            List<Integer> indices = DBUtils.getNestedIndices(param.substring(
                    udtColumnName.length() + 1, param.length()));
            return new UDT(udtColumnName, indices);
        }
        return null;
    }

    private ParamValue getParamValue(ExternalParamCollection params) throws DataServiceFault {
        if (this.getParamType().equals(DBSFields.RDF_REF_URI)) {
            return new ParamValue(this.getParam());
        } else {
            ExternalParam paramObj = this.getParamObj(params);
            /* workaround for 'column', 'query-param' mix up */
            if (paramObj == null) {
                if (this.getParamType().equals(DBSFields.COLUMN)) {
                    paramObj = params.getParam(DBSFields.QUERY_PARAM, this.getParam());
                } else if (this.getParamType().equals(DBSFields.QUERY_PARAM)) {
                    paramObj = params.getParam(DBSFields.COLUMN, this.getParam());
                }
            }
            if (paramObj != null) {
                return paramObj.getValue();
            } else if (this.isOptional()) {
            	return null;
            } else {
                throw new DataServiceFault(FaultCodes.INCOMPATIBLE_PARAMETERS_ERROR,
                                           "Error in 'StaticOutputElement.execute', " +
                                "cannot find parameter with type:"
                                + this.getParamType() + " name:" + this.getOriginalParam());
            }
        }
    }

    /**
     * Exports the given parameter.
     *
     * @param exportName The name of the variable to store the exported value
     * @param value      The exported value
     */
    private void exportParam(String exportName, String value, int type) {
        ParamValue paramVal = TLParamStore.getParam(exportName);
        if (paramVal == null || paramVal.getValueType() != type) {
            paramVal = new ParamValue(type);
            TLParamStore.addParam(exportName, paramVal);
        }
        if (type == ParamValue.PARAM_VALUE_ARRAY) {
            paramVal.addToArrayValue(new ParamValue(value));
        } else if (type == ParamValue.PARAM_VALUE_SCALAR) {
            paramVal.setScalarValue(value);
        }
    }

    @Override
    public void executeElement(XMLStreamWriter xmlWriter, ExternalParamCollection params,
                               int queryLevel, boolean escapeNonPrintableChar) throws DataServiceFault {
        ParamValue paramValue;
        if (this.hasConstantValue()) {
            paramValue = new ParamValue(this.getParam());
        } else {
            paramValue = this.getParamValue(params);
        }
        /* if the result is null, this is an optional field then, do not write it out */
        if (paramValue == null) {
        	return;
        }
        if (escapeNonPrintableChar && paramValue.getScalarValue() != null) {
            paramValue.setScalarValue(paramValue.getScalarValue().replaceAll(NON_PRINTABLE_CHARS, "?"));
        }
        /* export it if told, and only if it's boxcarring */
        if (this.getExport() != null && (DSSessionManager.isBoxcarring() || DispatchStatus.isBoxcarringRequest())) {
            this.exportParam(this.getExport(), paramValue.toString(), this.getExportType());
        }
        try {
            /* write element */
            if (this.getElementType().equals(DBSFields.ELEMENT)) {

                this.writeResultElement(xmlWriter, this.getName(), paramValue, this.getXsdType(),
                        this.getDataCategory(), this.getResultType(), params);

            } else if (this.getElementType().equals(DBSFields.ATTRIBUTE)) { /* write attribute */
                this.addAttribute(xmlWriter, this.getName(),
                        paramValue, this.getXsdType(), this.getResultType());
            }
        } catch (XMLStreamException e) {
            throw new DataServiceFault(e, "Error in XML generation at StaticOutputElement.execute");
        }
    }

    private ExternalParam getParamObj(ExternalParamCollection params) throws DataServiceFault {
        ExternalParam exParam = params.getParam(this.getParamType(), this.getParam());
        if (exParam != null) {
            /* Returns an external parameter object corresponds to a SCALAR output value */
            return exParam;
        }
        if (this.isUserDefinedObj()) {
            ParamValue processedParamValue;
            exParam = params.getParam(this.getParamType(), this.getUDTInfo().getUDTObjName());

            /* Retrieves the value of a User Defined Object */
            ParamValue value = exParam.getValue();
            if (DBUtils.isUDT(value)) {
                /* Retrieves value of the desired UDT attribute */
                processedParamValue = getUDTAttributeValue(value,
                        DBConstants.UDT_ATTRIBUTE_INITIAL_INDEX);

                return new ExternalParam(this.getParam(), processedParamValue,
                                         this.getParamType());
            }
            if (DBUtils.isSQLArray(value)) {
                processedParamValue = new ParamValue(ParamValue.PARAM_VALUE_ARRAY);
                this.getExternalParamFromArray(processedParamValue, value);

                return new ExternalParam(this.getParam(), processedParamValue,
                                         this.getParamType());
            }
        }
        return exParam;
    }

    /**
     * Extracts out an External parameter object representing an Array type ParamValue object.
     *
     * @param processedParamValue Processed parameter value
     * @param rawParamValue       Un processed parameter value
     * @throws DataServiceFault Throws when the process is confronted with issues while processing
     *                          the UDT attributes.
     */
    private void getExternalParamFromArray(ParamValue processedParamValue,
                                           ParamValue rawParamValue) throws DataServiceFault {
        for (ParamValue value : rawParamValue.getArrayValue()) {
            if (DBUtils.isUDT(value)) {
                processedParamValue.getArrayValue().add(getUDTAttributeValue(value,
                        DBConstants.UDT_ATTRIBUTE_INITIAL_INDEX));
            }
            if (DBUtils.isSQLArray(value)) {
                this.getExternalParamFromArray(processedParamValue, value);
            } else {
                processedParamValue.getArrayValue().add(value);
            }
        }
    }


    /**
     * This method traverse through the specified indices and recursively retrieves the value of
     * the UDT attribute.
     *
     * @param value   Value of the UDT attribute.
     * @param i       Index to keep track of the number of items process in the index list
     * @return Final value of the desired UDT attribute
     * @throws DataServiceFault DataServiceFault.
     */
    private ParamValue getUDTAttributeValue(ParamValue value, int i) throws DataServiceFault {

        if (this.getUDTInfo() == null) {
            throw new DataServiceFault("Output element '" + this.getName() +
                    "' cannot be resolved to a UDT");
        }
        List<Integer> indices = this.getUDTInfo().getIndices();

        if (DBUtils.isUDT(value)) {
            Object tmpVal;
            try {
                tmpVal = value.getUdt().getAttributes()[indices.get(i)];
            } catch (SQLException e) {
                throw new DataServiceFault(e, "Unable to retrieve UDT attribute value referred " +
                        "by the given index");
            }

            if (tmpVal instanceof Struct) {
                value = new ParamValue((Struct) tmpVal);
            } else if (tmpVal instanceof Array) {
                try {
                    value = DBUtils.processSQLArray((Array) tmpVal,
                                                    new ParamValue(ParamValue.PARAM_VALUE_ARRAY));
                } catch (SQLException e) {
                    throw new DataServiceFault(e, "Unable to process the SQL Array");
                }
            } else {
                try {
                    if (tmpVal == null) {
                        value = new ParamValue((String) null);
                    } else if (tmpVal instanceof Timestamp) {
                        Timestamp timestamp = (Timestamp) tmpVal;
                        value = new ParamValue(SQLQuery.convertToTimestampString(timestamp));
                    } else if (tmpVal instanceof Blob) {
                        value = new ParamValue(SQLQuery.getBase64StringFromInputStream(((Blob) tmpVal).getBinaryStream()));
                    } else if (tmpVal instanceof Time) {
                        Time time = (Time) tmpVal;
                        value = new ParamValue(SQLQuery.convertToTimeString(time));
                    } else if (tmpVal instanceof Date) {
                        Date date = (Date) tmpVal;
                        value = new ParamValue(ConverterUtil.convertToString(date));
                    } else if (tmpVal instanceof InputStream) {
                        InputStream inputStream = (InputStream) tmpVal;
                        value = new ParamValue(SQLQuery.getBase64StringFromInputStream(inputStream));
                    } else {
                        value = new ParamValue(String.valueOf(tmpVal));
                    }
                } catch (SQLException e) {
                    throw new DataServiceFault(e, "Unable to process the SQL UDT attribute value");
                }
            }
        } else if (DBUtils.isSQLArray(value)) {
            ParamValue processedParamValue = new ParamValue(ParamValue.PARAM_VALUE_ARRAY);
            for (ParamValue paramVal : value.getArrayValue()) {
                if (DBUtils.isUDT(paramVal)) {
                    try {
                        processedParamValue.getArrayValue().add(new ParamValue(
                                String.valueOf(paramVal.getUdt().getAttributes()[indices.get(i)])));
                    } catch (SQLException e) {
                        throw new DataServiceFault(e, "Unable to retrieve UDT attribute value " +
                                "referred by " + "the given index");
                    }
                } else {
                    processedParamValue.getArrayValue().add(paramVal);
                }
            }
            value = processedParamValue;
        } else {
            return value;
        }
        if (i <= indices.size()) {
            return getUDTAttributeValue(value, i + 1);
        }
        return value;
    }

    public boolean equals(Object o) {
        return (o instanceof StaticOutputElement) &&
                (((StaticOutputElement) o).getName().equals(this.getName()));
    }
    
    public int hashCode() {
    	return this.getName().hashCode();
    }

    /* Acts as a container for UDT related metadata */

    private class UDT {

        private String udtObjName;

        private List<Integer> indices;

        public UDT(String udtObjName, List<Integer> indices) {
            this.udtObjName = udtObjName;
            this.indices = indices;
        }

        public String getUDTObjName() {
            return udtObjName;
        }

        public List<Integer> getIndices() {
            return indices;
        }

    }


}
