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

import org.wso2.micro.integrator.dataservices.core.DBUtils;
import org.wso2.micro.integrator.dataservices.core.DataServiceFault;

import javax.xml.stream.XMLStreamWriter;
import java.util.Set;

/**
 * Represents an entity which can yield a result, i.e. elements in a result section.
 */
public abstract class OutputElement extends XMLWriterHelper {
    /**
     * name of element/attribute
     */
    private String name;

    /**
     * param value
     */
    private String param;

    /**
     * i.e. column, query-param, value
     */
    private String paramType;

    /**
     * If this element corresponds to a SQLArray, its name
     */
    private String arrayName;

    private Set<String> requiredRoles;

    /**
     * This flag is used to force this output element to be optional, this is used in situations such as,
     * the output element is in the first level of the result, and the result doesn't have a result row,
     * and only a wrapper, in those cases, when there is no result, the output elements will be optional.
     */
    private boolean optionalOverride;

    public OutputElement(String namespace, Set<String> requiredRoles) {
        super(namespace);
        this.requiredRoles = requiredRoles;
    }

    public OutputElement(String name, String namespace, Set<String> requiredRoles,
                         String arrayName) {
        super(namespace);
        this.name = name;
        this.requiredRoles = requiredRoles;
        this.arrayName = arrayName;
    }

    public OutputElement(String name, String namespace, Set<String> requiredRoles, String param,
                         String paramType, String arrayName) {
        super(namespace);
        this.name = name;
        this.requiredRoles = requiredRoles;
        this.param = param;
        this.paramType = paramType;
        this.arrayName = arrayName;
    }

    /**
     * Executes and writes the contents of this element, given the parameters.
     */
    public void execute(XMLStreamWriter xmlWriter, ExternalParamCollection params, int queryLevel, boolean escapeNonPrintableChar)
            throws DataServiceFault {
        if (this.getArrayName() == null) {
            this.executeElement(xmlWriter, params, queryLevel, escapeNonPrintableChar);
        } else {
            ExternalParam exParam = this.getExternalParam(params);
            if (exParam == null) {
                throw new DataServiceFault("The array '" + this.getArrayName() +
                        "' does not exist");
            }
            ParamValue paramValue = exParam.getValue();
            String name = exParam.getName();
            String type = exParam.getType();

            if (!DBUtils.isSQLArray(paramValue)) {
                throw new DataServiceFault("Parameter does not corresponding to an array");
            }

            ExternalParamCollection tmpParams;
            for (ParamValue value : paramValue.getArrayValue()) {
                tmpParams = new ExternalParamCollection();
                tmpParams.addParam(new ExternalParam(name, value, type));
                this.executeElement(xmlWriter, tmpParams, queryLevel, escapeNonPrintableChar);
            }
        }
    }

    protected abstract void executeElement(XMLStreamWriter xmlWriter,
                                           ExternalParamCollection params,
                                           int queryLevel, boolean escapeNonPrintableChar) throws DataServiceFault;

    /**
     * Returns the requires roles to view this element.
     */
    public Set<String> getRequiredRoles() {
        return requiredRoles;
    }

    /**
     * Checks if this element is optional,
     * if so, this has to be mentioned in the schema for WSDL generation.
     */
    public boolean isOptional() {
        return this.isOptionalOverride()
                || (this.getRequiredRoles() != null && this.getRequiredRoles().size() > 0);
    }

    public boolean isOptionalOverride() {
        return optionalOverride;
    }

    public void setOptionalOverride(boolean optionalOverride) {
        this.optionalOverride = optionalOverride;
    }

    public String getArrayName() {
        return arrayName;
    }

    public String getParam() {
        return param;
    }

    public String getName() {
        return name;
    }

    public String getParamType() {
        return paramType;
    }

    /**
     * Extracts out the external parameter corresponds to the user defined object name from the
     * external parameter collection
     *
     * @param params External parameter collection
     * @return External parameter associated with the provided user defined object name
     */
    private ExternalParam getExternalParam(ExternalParamCollection params) {
        ExternalParam exParam = params.getParam(getParamName(this.getArrayName()));
        if (exParam == null) {
            exParam = params.getParam(this.getParamType(), this.getParam());
        }
        return exParam;
    }

    /**
     * Extracts User Defined Column name from the param name
     *
     * @param paramName Original parameter name
     * @return Column name corresponds to the User Defined object
     */
    private static String getParamName(String paramName) {
        String udtObjName = DBUtils.extractUDTObjectName(paramName);
        if (udtObjName != null) {
            return udtObjName.toLowerCase();
        }
        return paramName.toLowerCase();
    }

}
