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

import java.sql.Types;
import java.text.ParseException;
import org.apache.axis2.databinding.utils.ConverterUtil;
import org.apache.commons.codec.binary.Base64;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeException;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.EdmProperty;
import org.apache.olingo.commons.core.Encoder;
import org.apache.olingo.commons.core.edm.primitivetype.EdmPrimitiveTypeFactory;
import org.apache.olingo.commons.core.edm.primitivetype.EdmString;

import java.util.Iterator;
import java.util.UUID;
import org.wso2.micro.integrator.dataservices.common.DBConstants;

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

    /**
     * This method creates primitive type property.
     *
     * @param columnType Data type of the column - java.sql.Types
     * @param name       Name of the column
     * @param paramValue String value
     * @return Property
     * @throws ODataServiceFault
     * @see Types
     * @see Property
     */
    public static Property createPrimitive(final DataColumn.ODataDataType columnType, final String name,
                                           final String paramValue) throws ODataServiceFault, ParseException {
        String propertyType;
        Object value;
        switch (columnType) {
            case INT32:
                propertyType = EdmPrimitiveTypeKind.Int32.getFullQualifiedName().getFullQualifiedNameAsString();
                value = paramValue == null ? null : ConverterUtil.convertToInt(paramValue);
                break;
            case INT16:
                propertyType = EdmPrimitiveTypeKind.Int16.getFullQualifiedName().getFullQualifiedNameAsString();
                value = paramValue == null ? null : ConverterUtil.convertToShort(paramValue);
                break;
            case DOUBLE:
                propertyType = EdmPrimitiveTypeKind.Double.getFullQualifiedName().getFullQualifiedNameAsString();
                value = paramValue == null ? null : ConverterUtil.convertToDouble(paramValue);
                break;
            case STRING:
                propertyType = EdmPrimitiveTypeKind.String.getFullQualifiedName().getFullQualifiedNameAsString();
                value = paramValue;
                break;
            case BOOLEAN:
                propertyType = EdmPrimitiveTypeKind.Boolean.getFullQualifiedName().getFullQualifiedNameAsString();
                value = paramValue == null ? null : ConverterUtil.convertToBoolean(paramValue);
                break;
            case BINARY:
                propertyType = EdmPrimitiveTypeKind.Binary.getFullQualifiedName().getFullQualifiedNameAsString();
                value = paramValue == null ? null : getBytesFromBase64String(paramValue);
                break;
            case BYTE:
                propertyType = EdmPrimitiveTypeKind.Byte.getFullQualifiedName().getFullQualifiedNameAsString();
                value = paramValue;
                break;
            case SBYTE:
                propertyType = EdmPrimitiveTypeKind.SByte.getFullQualifiedName().getFullQualifiedNameAsString();
                value = paramValue;
                break;
            case DATE:
                propertyType = EdmPrimitiveTypeKind.Date.getFullQualifiedName().getFullQualifiedNameAsString();
                value = ConverterUtil.convertToDate(paramValue);
                break;
            case DURATION:
                propertyType = EdmPrimitiveTypeKind.Duration.getFullQualifiedName().getFullQualifiedNameAsString();
                value = paramValue;
                break;
            case DECIMAL:
                propertyType = EdmPrimitiveTypeKind.Decimal.getFullQualifiedName().getFullQualifiedNameAsString();
                value = paramValue == null ? null : ConverterUtil.convertToBigDecimal(paramValue);
                break;
            case SINGLE:
                propertyType = EdmPrimitiveTypeKind.Single.getFullQualifiedName().getFullQualifiedNameAsString();
                value = paramValue == null ? null : ConverterUtil.convertToFloat(paramValue);
                break;
            case TIMEOFDAY:
                propertyType = EdmPrimitiveTypeKind.TimeOfDay.getFullQualifiedName().getFullQualifiedNameAsString();
                value = paramValue == null ? null : ConverterUtil.convertToTime(paramValue).getAsCalendar();
                break;
            case INT64:
                propertyType = EdmPrimitiveTypeKind.Int64.getFullQualifiedName().getFullQualifiedNameAsString();
                value = paramValue == null ? null : ConverterUtil.convertToLong(paramValue);
                break;
            case DATE_TIMEOFFSET:
                propertyType = EdmPrimitiveTypeKind.DateTimeOffset.getFullQualifiedName().getFullQualifiedNameAsString();
                value = ConverterUtil.convertToDateTime(paramValue);
                break;
            case GUID:
                propertyType = EdmPrimitiveTypeKind.Guid.getFullQualifiedName().getFullQualifiedNameAsString();
                value = UUID.fromString(paramValue);
                break;
            case STREAM:
                propertyType = EdmPrimitiveTypeKind.Stream.getFullQualifiedName().getFullQualifiedNameAsString();
                value = paramValue;
                break;
            case GEOGRAPHY:
                propertyType = EdmPrimitiveTypeKind.Geography.getFullQualifiedName().getFullQualifiedNameAsString();
                value = paramValue;
                break;
            case GEOGRAPHY_POINT:
                propertyType = EdmPrimitiveTypeKind.GeographyPoint.getFullQualifiedName().getFullQualifiedNameAsString();
                value = paramValue;
                break;
            case GEOGRAPHY_LINE_STRING:
                propertyType =
                        EdmPrimitiveTypeKind.GeographyLineString.getFullQualifiedName().getFullQualifiedNameAsString();
                value = paramValue;
                break;
            case GEOGRAPHY_POLYGON:
                propertyType = EdmPrimitiveTypeKind.GeographyPolygon.getFullQualifiedName().getFullQualifiedNameAsString();
                value = paramValue;
                break;
            case GEOGRAPHY_MULTIPOINT:
                propertyType =
                        EdmPrimitiveTypeKind.GeographyMultiPoint.getFullQualifiedName().getFullQualifiedNameAsString();
                value = paramValue;
                break;
            case GEOGRAPHY_MULTILINE_STRING:
                propertyType =
                        EdmPrimitiveTypeKind.GeographyMultiLineString.getFullQualifiedName().getFullQualifiedNameAsString();
                value = paramValue;
                break;
            case GEOGRAPHY_MULTIPOLYGON:
                propertyType =
                        EdmPrimitiveTypeKind.GeographyMultiPolygon.getFullQualifiedName().getFullQualifiedNameAsString();
                value = paramValue;
                break;
            case GEOGRAPHY_COLLECTION:
                propertyType =
                        EdmPrimitiveTypeKind.GeographyCollection.getFullQualifiedName().getFullQualifiedNameAsString();
                value = paramValue;
                break;
            case GEOMETRY:
                propertyType = EdmPrimitiveTypeKind.Geometry.getFullQualifiedName().getFullQualifiedNameAsString();
                value = paramValue;
                break;
            case GEOMETRY_POINT:
                propertyType = EdmPrimitiveTypeKind.GeometryPoint.getFullQualifiedName().getFullQualifiedNameAsString();
                value = paramValue;
                break;
            case GEOMETRY_LINE_STRING:
                propertyType =
                        EdmPrimitiveTypeKind.GeometryLineString.getFullQualifiedName().getFullQualifiedNameAsString();
                value = paramValue;
                break;
            case GEOMETRY_POLYGON:
                propertyType = EdmPrimitiveTypeKind.GeometryPolygon.getFullQualifiedName().getFullQualifiedNameAsString();
                value = paramValue;
                break;
            case GEOMETRY_MULTIPOINT:
                propertyType =
                        EdmPrimitiveTypeKind.GeometryMultiPoint.getFullQualifiedName().getFullQualifiedNameAsString();
                value = paramValue;
                break;
            case GEOMETRY_MULTILINE_STRING:
                propertyType =
                        EdmPrimitiveTypeKind.GeographyMultiLineString.getFullQualifiedName().getFullQualifiedNameAsString();
                value = paramValue;
                break;
            case GEOMETRY_MULTIPOLYGON:
                propertyType =
                        EdmPrimitiveTypeKind.GeometryMultiPolygon.getFullQualifiedName().getFullQualifiedNameAsString();
                value = paramValue;
                break;
            case GEOMETRY_COLLECTION:
                propertyType =
                        EdmPrimitiveTypeKind.GeometryCollection.getFullQualifiedName().getFullQualifiedNameAsString();
                value = paramValue;
                break;
            default:
                propertyType = EdmPrimitiveTypeKind.String.getFullQualifiedName().getFullQualifiedNameAsString();
                value = paramValue;
                break;
        }
        return new Property(propertyType, name, ValueType.PRIMITIVE, value);
    }

    public static byte[] getBytesFromBase64String(String base64Str) throws ODataServiceFault {
        try {
            return Base64.decodeBase64(base64Str.getBytes(DBConstants.DEFAULT_CHAR_SET_TYPE));
        } catch (Exception e) {
            throw new ODataServiceFault(e.getMessage());
        }
    }

}
