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

package org.wso2.micro.integrator.dataservices.core.odata;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeException;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.EdmProperty;
import org.apache.olingo.commons.core.Encoder;
import org.apache.olingo.commons.core.edm.primitivetype.EdmPrimitiveTypeFactory;
import org.apache.olingo.commons.core.edm.primitivetype.EdmString;

import java.util.Iterator;
import java.util.UUID;

/**
 * Utility class for OData.
 */
public class ODataUtils {

    /**
     * This method generates an unique ETag for each data row entry.
     *
     * @param tableName Name of the table
     * @param entry     Data row entry
     * @return E Tag
     */
    public static String generateETag(String configID, String tableName, ODataEntry entry) {
        StringBuilder uniqueString = new StringBuilder();
        uniqueString.append(configID).append(tableName);
        for (String columnName : entry.getNames()) {
            uniqueString.append(columnName).append(entry.getValue(columnName));
        }
        return UUID.nameUUIDFromBytes((uniqueString.toString()).getBytes()).toString();
    }

    /**
     * This method creates access uri for the entity.
     *
     * @param baseURL      base URL
     * @param entity       entity
     * @param enitySetName entity Set Name
     * @param type         entity Type
     * @return Entity URI
     * @throws EdmPrimitiveTypeException
     */
    public static String buildLocation(String baseURL, Entity entity, String enitySetName, EdmEntityType type)
            throws EdmPrimitiveTypeException {
        StringBuilder location = new StringBuilder();
        location.append(baseURL).append("/").append(enitySetName);
        int i = 0;
        boolean usename = type.getKeyPredicateNames().size() > 1;
        location.append("(");

        String value;
        for (Iterator var7 = type.getKeyPredicateNames().iterator(); var7.hasNext(); location.append(value)) {
            String key = (String) var7.next();
            if (i > 0) {
                location.append(",");
            }

            ++i;
            if (usename) {
                location.append(key).append("=");
            }

            String propertyType = entity.getProperty(key).getType();
            Object propertyValue = entity.getProperty(key).getValue();
            if (propertyType.startsWith("Edm.")) {
                propertyType = propertyType.substring(4);
            }

            EdmPrimitiveTypeKind kind = EdmPrimitiveTypeKind.valueOf(propertyType);
            EdmProperty property = type.getStructuralProperty(key);
            value = EdmPrimitiveTypeFactory.getInstance(kind)
                                           .valueToString(propertyValue, property.isNullable(), property.getMaxLength(),
                                                          property.getPrecision(), property.getScale(),
                                                          property.isUnicode());
            if (kind == EdmPrimitiveTypeKind.String) {
                value = EdmString.getInstance().toUriLiteral(Encoder.encode(value));
            }
        }

        location.append(")");
        return location.toString();
    }

}
