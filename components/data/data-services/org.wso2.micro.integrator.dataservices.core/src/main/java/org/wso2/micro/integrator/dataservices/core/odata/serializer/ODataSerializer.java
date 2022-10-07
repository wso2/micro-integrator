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

import org.apache.olingo.commons.api.data.EntityIterator;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.serializer.EntityCollectionSerializerOptions;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.serializer.SerializerStreamResult;

public interface ODataSerializer {

    /**
     * This method generates the output response.
     *
     * @param metadata
     * @param edmEntityType
     * @param iterator
     * @param options
     * @return
     * @throws SerializerException
     */
    SerializerStreamResult entityCollectionStreamed(ServiceMetadata metadata, EdmEntityType edmEntityType,
                                                    EntityIterator iterator, EntityCollectionSerializerOptions options)
            throws SerializerException;

}
