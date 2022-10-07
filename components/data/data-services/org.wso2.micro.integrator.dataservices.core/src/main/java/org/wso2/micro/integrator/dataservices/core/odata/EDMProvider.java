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

import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlAbstractEdmProvider;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainer;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainerInfo;
import org.apache.olingo.commons.api.edm.provider.CsdlEntitySet;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationPropertyBinding;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlPropertyRef;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * This class implements Olingo CsdlAbstractEdmProvider, OData EDM provider which contains the service metadata.
 *
 * @see CsdlAbstractEdmProvider
 */
public class EDMProvider extends CsdlAbstractEdmProvider {

    /**
     * Namespace of the OData service ( Same As DSS DataService ).
     */
    private String namespace;

    /**
     * FullQualifiedName of the OData container.
     */
    private FullQualifiedName containerFullQName;

    /**
     * Map of One-To-Many relations list.
     */
    private Map<String, HashSet<String>> oneToManyRelationList;

    /**
     * Map of Many-To-One relations list.
     */
    private Map<String, HashSet<String>> manyToOneRelationList;

    /**
     * Map of OData entity types.
     */
    private Map<String, CsdlEntityType> csdlEntityTypesMap;

    /**
     * List of OData schemas.
     */
    private List<CsdlSchema> csdlSchemaList;

    /**
     * OData entity container.
     */
    private CsdlEntityContainer csdlEntityContainer;

    /**
     * OData entity container info.
     */
    private CsdlEntityContainerInfo csdlEntityContainerInfo;

    /**
     * Map of OData entity sets for quick retrieval.
     */
    private Map<String, CsdlEntitySet> csdlEntitySetMap;

    public EDMProvider(List<String> tableList, String containerName, String namespace,
                       Map<String, List<CsdlProperty>> propertiesMap, Map<String, List<CsdlPropertyRef>> pkeys,
                       List<String> entitySet, Map<String, NavigationTable> navigationProperties) {
        this.containerFullQName = new FullQualifiedName(namespace, containerName);
        this.namespace = namespace;
        if (navigationProperties != null) {
            this.manyToOneRelationList = generateManyToOneRelationships(navigationProperties);
            this.oneToManyRelationList = generateOneToManyRelationships(navigationProperties);
        } else {
            this.manyToOneRelationList = null;
            this.oneToManyRelationList = null;
        }
        this.csdlEntityTypesMap = generateEntityTypes(propertiesMap, tableList, pkeys);
        this.csdlEntitySetMap = generateEntitySets(entitySet);
        this.csdlEntityContainer = generateCsdlEntityContainer();
        this.csdlEntityContainerInfo = generateCsdlEntityContainerInfo();
        this.csdlSchemaList = generateSchemaList();
    }

    /**
     * This method creates a map of Csdl entity types.
     *
     * @param properties Properties
     * @param entityList EntityList
     * @param pKeyList   Primary keys list
     * @return List of Csdl entity types (Common Schema Definition Language Entity Type)
     * @see CsdlEntityType
     */
    private Map<String, CsdlEntityType> generateEntityTypes(Map<String, List<CsdlProperty>> properties,
                                                            List<String> entityList,
                                                            Map<String, List<CsdlPropertyRef>> pKeyList) {
        Map<String, CsdlEntityType> csdlEntityTypesMap = new HashMap<>();
        for (String entityTypeName : entityList) {
            CsdlEntityType entity = new CsdlEntityType();
            entity.setName(entityTypeName);
            for (CsdlProperty property : properties.get(entityTypeName)) {
                if (EdmPrimitiveTypeKind.Stream.getFullQualifiedName().getFullQualifiedNameAsString()
                                               .equals(property.getType())) {
                    entity.setHasStream(true);
                    break;
                }
            }
            List<CsdlPropertyRef> keys = pKeyList.get(entityTypeName);
            //Adding Keys
            if (keys.size() != 0) {
                entity.setKey(keys);
            }
            //Adding Properties (Columns)
            entity.setProperties(properties.get(entityTypeName));
            //Adding Navigation
            List<CsdlNavigationProperty> navigationProperties = new ArrayList<>();
            //if oneToManyRelationList is null, Obviously manyToOneRelationList should be NULL.
            if (oneToManyRelationList != null) {
                // One To Many relations
                if (oneToManyRelationList.get(entityTypeName) != null) {
                    for (String navigation : oneToManyRelationList.get(entityTypeName)) {
                        CsdlNavigationProperty navProp = new CsdlNavigationProperty();
                        navProp.setName(navigation);
                        navProp.setType(new FullQualifiedName(namespace, navigation));
                        navProp.setCollection(true);
                        navProp.setPartner(entityTypeName);
                        navigationProperties.add(navProp);
                    }
                }
                //Many to one relations
                if (manyToOneRelationList.get(entityTypeName) != null) {
                    for (String navigation : manyToOneRelationList.get(entityTypeName)) {
                        CsdlNavigationProperty navProp = new CsdlNavigationProperty();
                        navProp.setName(navigation);
                        navProp.setType(new FullQualifiedName(namespace, navigation));
                        navProp.setPartner(entityTypeName);
                        navigationProperties.add(navProp);
                    }
                }
                if (!navigationProperties.isEmpty()) {
                    entity.setNavigationProperties(navigationProperties);
                }
            }
            csdlEntityTypesMap.put(entityTypeName, entity);
        }
        return csdlEntityTypesMap;
    }

    /**
     * This method creates a map of Csdl Entity set for the easy retrieve.
     *
     * @param entitySetList List of entity sets
     * @return Map of Csdl entity set
     * @see CsdlEntitySet
     */
    private Map<String, CsdlEntitySet> generateEntitySets(List<String> entitySetList) {
        Map<String, CsdlEntitySet> csdlEntitySetsMap = new HashMap<>();
        for (String entitySetName : entitySetList) {
            CsdlEntitySet entitySet = new CsdlEntitySet();
            entitySet.setType(new FullQualifiedName(namespace, entitySetName));
            entitySet.setName(entitySetName);
            //if oneToManyRelationList is null, Obviously manyToOneRelationList should be NULL.
            if (oneToManyRelationList != null) {
                //Set Navigational Bindings to oneToMany
                List<CsdlNavigationPropertyBinding> navPropBindingList = new ArrayList<>();
                if (oneToManyRelationList.get(entitySetName) != null) {
                    for (String navigation : oneToManyRelationList.get(entitySetName)) {
                        CsdlNavigationPropertyBinding navPropBinding = new CsdlNavigationPropertyBinding();
                        // the target entity set, where the navigation property points to
                        navPropBinding.setTarget(navigation);
                        // the path from entity type to navigation property
                        navPropBinding.setPath(navigation);
                        navPropBindingList.add(navPropBinding);
                    }
                }
                if (manyToOneRelationList.get(entitySetName) != null) {
                    for (String navigation : manyToOneRelationList.get(entitySetName)) {
                        CsdlNavigationPropertyBinding navPropBinding = new CsdlNavigationPropertyBinding();
                        // the target entity set, where the navigation property points to
                        navPropBinding.setTarget(navigation);
                        // the path from entity type to navigation property
                        navPropBinding.setPath(navigation);
                        navPropBindingList.add(navPropBinding);
                    }
                }
                if (!navPropBindingList.isEmpty()) {
                    entitySet.setNavigationPropertyBindings(navPropBindingList);
                }
            }
            csdlEntitySetsMap.put(entitySetName, entitySet);
        }
        return csdlEntitySetsMap;
    }

    /**
     * This method creates a list of Csdl schema.
     *
     * @return List of Csdl schema
     * @see CsdlSchema
     */
    private List<CsdlSchema> generateSchemaList() {
        List<CsdlSchema> schemaList = new ArrayList<>();
        CsdlSchema schema = new CsdlSchema();
        schema.setNamespace(namespace);
        // EntityTypes
        if (!csdlEntityTypesMap.isEmpty()) {
            schema.setEntityTypes(new ArrayList<>(csdlEntityTypesMap.values()));
        }
        schema.setEntityContainer(csdlEntityContainer);
        schemaList.add(schema);
        return schemaList;
    }

    /**
     * This method creates a Csdl entity container.
     *
     * @return Csdl entity container
     * @see CsdlEntityContainer
     */
    private CsdlEntityContainer generateCsdlEntityContainer() {
        CsdlEntityContainer container = new CsdlEntityContainer();
        container.setName(containerFullQName.getName());
        container.setEntitySets(new ArrayList<>(csdlEntitySetMap.values()));
        return container;
    }

    /**
     * This method creates Csdl Entity Container Info.
     *
     * @return Csdl Entity Container Info
     */
    private CsdlEntityContainerInfo generateCsdlEntityContainerInfo() {
        return new CsdlEntityContainerInfo().setContainerName(containerFullQName);
    }

    @Override
    public CsdlEntityType getEntityType(final FullQualifiedName entityTypeName) {
        return csdlEntityTypesMap.get(entityTypeName.getName());

    }

    @Override
    public CsdlEntitySet getEntitySet(final FullQualifiedName entityContainer, final String entitySetName) {
        if (containerFullQName.equals(entityContainer)) {
            return csdlEntitySetMap.get(entitySetName);
        }
        return null;
    }

    @Override
    public List<CsdlSchema> getSchemas() {
        return csdlSchemaList;
    }

    @Override
    public CsdlEntityContainer getEntityContainer() {
        return csdlEntityContainer;
    }

    @Override
    public CsdlEntityContainerInfo getEntityContainerInfo(final FullQualifiedName entityContainerName) {
        if (entityContainerName == null || containerFullQName.equals(entityContainerName)) {
            return csdlEntityContainerInfo;
        }
        return null;
    }

    /**
     * This method create a Map with onetoMany relationship,
     * which contains table name as the keys and the values as the table names which has oneToMany relationship.
     *
     * @param navigationProperties Map of Navigation properties in ODataDataHandler
     * @return oneToManyRelationship
     * @see ODataDataHandler#getNavigationProperties()
     */
    private Map<String, HashSet<String>> generateOneToManyRelationships(
            Map<String, NavigationTable> navigationProperties) {
        Map<String, HashSet<String>> relationship = new HashMap<>();
        for (String tableName : navigationProperties.keySet()) {
            relationship.put(tableName, new HashSet<>(navigationProperties.get(tableName).getTables()));
        }
        return relationship;
    }

    /**
     * This method create a Map with manytoOne relationship,
     * which contains table name as the keys and the values as the table names which has manyToOne relationship.
     *
     * @param navigationProperties Map of Navigation properties in ODataDataHandler
     * @return manyToOneRelationship
     * @see ODataDataHandler#getNavigationProperties()
     */
    private Map<String, HashSet<String>> generateManyToOneRelationships(
            Map<String, NavigationTable> navigationProperties) {
        Map<String, HashSet<String>> relationship = new HashMap<>();
        for (String exportedTableName : navigationProperties.keySet()) {
            for (String importedTableName : navigationProperties.get(exportedTableName).getTables()) {
                HashSet<String> tableList = relationship.get(importedTableName);
                if (tableList == null) {
                    tableList = new HashSet<>();
                    relationship.put(importedTableName, tableList);
                }
                tableList.add(exportedTableName);
            }
        }
        return relationship;
    }
}
