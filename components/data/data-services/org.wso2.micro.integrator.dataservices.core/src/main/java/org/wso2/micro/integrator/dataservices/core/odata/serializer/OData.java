/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.micro.integrator.dataservices.core.odata.serializer;

import java.util.List;

import org.apache.olingo.commons.api.edm.provider.CsdlEdmProvider;
import org.apache.olingo.commons.api.edmx.EdmxReference;
import org.apache.olingo.commons.api.ex.ODataRuntimeException;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.etag.ServiceMetadataETagSupport;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.uri.UriHelper;

public abstract class OData {

    public OData() {
    }

    public static OData newInstance() {
        try {
            OData object = new ODataImpl();
            return object;
        } catch (Exception e) {
            throw new ODataRuntimeException(e);
        }
    }

    /**
     * This method returns a JSON or XML OData serializer according to the given content type.
     *
     * @param contentType
     * @throws SerializerException
     */
    public abstract ODataSerializer createSerializer(ContentType contentType) throws SerializerException;

    /**
     * This method creates a metadata object for the given service when ETag is not defined.
     *
     * @param metaData
     * @param edmxReferenceList
     */
    public abstract ServiceMetadata createServiceMetadata(CsdlEdmProvider metaData,
                                                          List<EdmxReference> edmxReferenceList);

    /**
     * This method creates a metadata object for the given service when ETag is defined.
     *
     * @param edmProvider
     * @param edmxReferenceList
     * @param eTagSupport
     */
    public abstract ServiceMetadata createServiceMetadata(CsdlEdmProvider edmProvider,
                                                          List<EdmxReference> edmxReferenceList,
                                                          ServiceMetadataETagSupport eTagSupport);

    /**
     * This method creates a new URI helper object for performing URI-related tasks.
     */
    public abstract UriHelper createUriHelper();

}
