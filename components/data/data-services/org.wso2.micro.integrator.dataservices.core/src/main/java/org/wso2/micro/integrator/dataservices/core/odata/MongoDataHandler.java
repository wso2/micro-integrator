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

import com.mongodb.AggregationOptions;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.WriteResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.olingo.server.api.uri.queryoption.OrderByItem;
import org.apache.olingo.server.api.uri.queryoption.OrderByOption;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.jongo.Jongo;
import org.json.JSONObject;
import org.wso2.micro.integrator.dataservices.core.description.query.MongoQuery;
import org.wso2.micro.integrator.dataservices.core.engine.DataEntry;

/**
 * This class implements MongoDB datasource related operations for ODataDataHandler.
 */
public class MongoDataHandler implements ODataDataHandler {

    /**
     * configuration ID is the ID given for the data service, at the time
     * when the particular service is created.
     */

    private final String configId;

    /**
     * DocumentId/ObjectId s of the Collections
     */
    private Map<String, List<String>> primaryKeys;

    /**
     * List of Collections in the Database.
     */
    private List<String> tableList;

    /**
     * Metadata of the Collections
     */
    private Map<String, Map<String, DataColumn>> tableMetaData;
    private Jongo jongo;
    private static final String ETAG = "ETag";
    private static final String DOCUMENT_ID = "_id";

    /**
     * Preferred chunk size.
     */
    private final int chunkSize;

    /**
     * Number of entities to be skipped during the current read iteration.
     */
    private int skipEntityCount;

    public MongoDataHandler(String configId, Jongo jongo) {
        this.configId = configId;
        this.jongo = jongo;
        this.tableList = generateTableList();
        this.tableMetaData = generateTableMetaData();
        this.primaryKeys = generatePrimaryKeys();
        this.chunkSize = ODataAdapter.getChunkSize();
    }

    /**
     * This method returns database collection metadata.
     * Returns a map with collection name as the key, and the values containing
     * maps with column name as the map key, and the values of the column name
     * map will be a DataColumn object, which represents the column.
     *
     * @return Database Metadata
     * @see DataColumn
     */
    @Override
    public Map<String, Map<String, DataColumn>> getTableMetadata() {
        return this.tableMetaData;
    }

    private Map<String, Map<String, DataColumn>> generateTableMetaData() {
        int ordinalPosition = 1;
        Map<String, Map<String, DataColumn>> metaData = new HashMap<>();
        HashMap<String, DataColumn> column = new HashMap<>();
        for (String tableName : this.tableList) {
            DBCollection readResult = jongo.getDatabase().getCollection(tableName);
            Iterator<DBObject> cursor = readResult.find();
            while (cursor.hasNext()) {
                DBObject doumentData = cursor.next();
                String tempValue = doumentData.toString();
                Iterator<?> keys = new JSONObject(tempValue).keys();
                while (keys.hasNext()) {
                    String columnName = (String) keys.next();
                    DataColumn dataColumn = new DataColumn(columnName, DataColumn.ODataDataType.STRING,
                                                           ordinalPosition, true, 100, columnName.equals(DOCUMENT_ID));
                    column.put(columnName, dataColumn);
                    ordinalPosition++;
                }
                metaData.put(tableName, column);
            }
        }
        return metaData;
    }

    /**
     * This method creates a list of collections available in the DB.
     *
     * @returns the collection list of the DB
     */
    @Override
    public List<String> getTableList() {
        return this.tableList;
    }

    private List<String> generateTableList() {
        return new ArrayList<>(jongo.getDatabase().getCollectionNames());
    }

    /**
     * This method returns the primary keys of all the collections in the database.
     * Return a map with table name as the key, and the values contains a list of column
     * names which act as primary keys in each collection.
     *
     * @return Primary Key Map
     */
    @Override
    public Map<String, List<String>> getPrimaryKeys() {
        return this.primaryKeys;
    }

    private Map<String, List<String>> generatePrimaryKeys() {
        Map<String, List<String>> primaryKeyList = new HashMap<>();
        List<String> tableNames = this.tableList;
        List<String> primaryKey = new ArrayList<>();
        primaryKey.add(DOCUMENT_ID);
        for (String tname : tableNames) {
            primaryKeyList.put(tname, primaryKey);
        }
        return primaryKeyList;
    }

    /**
     * This method reads the data for a given collection.
     * Returns a list of DataEntry objects.
     *
     * @param tableName Name of the table
     * @return EntityCollection
     * @see DataEntry
     */
    @Override
    public List<ODataEntry> readTable(String tableName) {
        List<ODataEntry> entryList = new ArrayList<>();
        DBCollection readResult = jongo.getDatabase().getCollection(tableName);
        Iterator<DBObject> cursor = readResult.find();
        DBObject documentData;
        String tempValue;
        while (cursor.hasNext()) {
            ODataEntry dataEntry;
            documentData = cursor.next();
            tempValue = documentData.toString();
            Iterator<?> keys = new JSONObject(tempValue).keys();
            dataEntry = createDataEntryFromResult(tempValue, keys);
            //Set Etag to the entity
            dataEntry.addValue(ETAG, ODataUtils.generateETag(this.configId, tableName, dataEntry));
            entryList.add(dataEntry);
        }
        return entryList;
    }

    public List<ODataEntry> streamTable(String tableName) {
        DBCollection readResult = jongo.getDatabase().getCollection(tableName);
        Iterator<DBObject> iterator = readResult.find().skip(this.skipEntityCount).limit(this.chunkSize);
        return readStreamResultSet(tableName, iterator);
    }

    public List<ODataEntry> streamTableWithKeys(String tableName, ODataEntry keys) throws ODataServiceFault {
        throw new ODataServiceFault("MongoDB datasources doesn't support navigation.");
    }

    public void initStreaming() {
        this.skipEntityCount = 0;
    }

    public List<ODataEntry> streamTableWithOrder(String tableName, OrderByOption orderByOption) {
        DBCollection readResult = jongo.getDatabase().getCollection(tableName);
        List<BasicDBObject> stages = getSortStage(orderByOption);

        BasicDBObject skip = new BasicDBObject();
        skip.put("$skip", this.skipEntityCount);
        stages.add(skip);
        BasicDBObject limit = new BasicDBObject();
        limit.put("$limit", this.chunkSize);
        stages.add(limit);

        AggregationOptions options = AggregationOptions.builder().outputMode(AggregationOptions.OutputMode.INLINE)
                .build();
        Iterator<DBObject> iterator = readResult.aggregate(stages, options);
        return readStreamResultSet(tableName, iterator);
    }

    /**
     * This method reads the stream result set to generate a list of OData entries.
     *
     * @param tableName Name of the table
     * @param iterator  Iterator of the results set
     * @return
     */
    private List<ODataEntry> readStreamResultSet(String tableName, Iterator<DBObject> iterator) {
        DBObject documentData;
        String tempValue;
        List<ODataEntry> entryList = new ArrayList<>();
        while (iterator.hasNext()) {
            ODataEntry dataEntry;
            documentData = iterator.next();
            tempValue = documentData.toString();
            Iterator<?> keys = new JSONObject(tempValue).keys();
            dataEntry = createDataEntryFromResult(tempValue, keys);
            //Set Etag to the entity
            dataEntry.addValue(ETAG, ODataUtils.generateETag(this.configId, tableName, dataEntry));
            entryList.add(dataEntry);
        }
        this.skipEntityCount += this.chunkSize;
        return entryList;
    }

    /**
     * This method arranges the sort stage of the aggregator.
     *
     * @param orderByOption List of keys to consider when sorting
     * @return List of DBObjects
     * @see BasicDBObject
     */
    private List<BasicDBObject> getSortStage(OrderByOption orderByOption) {
        List<BasicDBObject> stages = new ArrayList<>();
        BasicDBObject sortList = new BasicDBObject();
        BasicDBObject fieldList = new BasicDBObject();

        for (int i = 0; i < orderByOption.getOrders().size(); i++) {
            final OrderByItem item = orderByOption.getOrders().get(i);
            String expr = item.getExpression().toString().replaceAll("[\\[\\]]", "").replaceAll("[\\{\\}]", "");
            String[] exprArr = expr.split(" ");
            int order = item.isDescending() ? -1 : 1;
            if (exprArr.length == 1) {
                sortList.put(exprArr[0], order);
            } else if (exprArr.length == 2) {
                BasicDBObject length = new BasicDBObject();
                length.put("$strLenCP", "$" + exprArr[1]);
                fieldList.put(exprArr[1] + "Len", length);
                sortList.put(exprArr[1] + "Len", order);
            }
        }
        BasicDBObject addFields = new BasicDBObject();
        addFields.put("$addFields", fieldList);
        BasicDBObject sort = new BasicDBObject();
        sort.put("$sort", sortList);
        stages.add(addFields);
        stages.add(sort);
        return stages;
    }

    /**
     * This method reads the collection data for a given key(i.e. _id).
     * Returns a list of DataEntry object which has been wrapped the entity.
     *
     * @param tableName Name of the table
     * @param keys      Keys to check
     * @return EntityCollection
     * @throws ODataServiceFault
     * @see DataEntry
     */
    @Override
    public List<ODataEntry> readTableWithKeys(String tableName, ODataEntry keys) throws ODataServiceFault {
        List<ODataEntry> entryList = new ArrayList<>();
        ODataEntry dataEntry;
        for (String keyName : keys.getData().keySet()) {
            String keyValue = keys.getValue(keyName);
            String projectionResult = jongo.getCollection(tableName).findOne(new ObjectId(keyValue)).
                map(MongoQuery.MongoResultMapper.getInstance());
            if (projectionResult == null) {
                throw new ODataServiceFault(DOCUMENT_ID + keyValue + " does not exist in collection: "
                    + tableName + " .");
            }
            Iterator<?> key = new JSONObject(projectionResult).keys();
            dataEntry = createDataEntryFromResult(projectionResult, key);
            //Set Etag to the entity
            dataEntry.addValue(ETAG, ODataUtils.generateETag(this.configId, tableName, dataEntry));
            entryList.add(dataEntry);
        }
        return entryList;
    }

    /**
     * This method creates an OData DataEntry for a given individual database record.
     * Returns a DataEntry object which has been wrapped in the entity.
     *
     * @param readResult DB result
     * @param keys       Keys set of the DB result
     * @return EntityCollection
     * @see DataEntry
     */
    private ODataEntry createDataEntryFromResult(String readResult, Iterator<?> keys) {
        ODataEntry dataEntry = new ODataEntry();
        while (keys.hasNext()) {
            String columnName = (String) keys.next();
            String columnValue = new JSONObject(readResult).get(columnName).toString();
            if (columnName.equals(DOCUMENT_ID)) {
                Iterator<?> idField = new JSONObject(columnValue).keys();
                while (idField.hasNext()) {
                    String idName = idField.next().toString();
                    String idValue = new JSONObject(columnValue).get(idName).toString();
                    dataEntry.addValue(columnName, idValue);
                }
            } else {
                dataEntry.addValue(columnName, columnValue);
            }
        }
        return dataEntry;
    }

    /**
     * This method inserts a given entity to the given collection.
     *
     * @param tableName Name of the table
     * @param entity    Entity
     * @throws ODataServiceFault
     */
    @Override
    public ODataEntry insertEntityToTable(String tableName, ODataEntry entity) {
        ODataEntry createdEntry = new ODataEntry();
        final Document document = new Document();
        for (String columnName : entity.getData().keySet()) {
            String columnValue = entity.getValue(columnName);
            document.put(columnName, columnValue);
            entity.addValue(columnName, columnValue);
        }
        ObjectId objectId = new ObjectId();
        document.put(DOCUMENT_ID, objectId);
        jongo.getCollection(tableName).insert(document);
        String documentIdValue = objectId.toString();
        createdEntry.addValue(DOCUMENT_ID, documentIdValue);
        //Set Etag to the entity
        createdEntry.addValue(ODataConstants.E_TAG, ODataUtils.generateETag(this.configId, tableName, entity));
        return createdEntry;
    }

    /**
     * This method deletes the entity from the collection for a given key.
     *
     * @param tableName Name of the table
     * @param entity    Entity
     * @throws ODataServiceFault
     */
    @Override
    public boolean deleteEntityInTable(String tableName, ODataEntry entity) throws ODataServiceFault {
        String documentId = entity.getValue(DOCUMENT_ID);
        WriteResult delete = jongo.getCollection(tableName).remove(new ObjectId(documentId));
        int wasDeleted = delete.getN();
        if (wasDeleted == 1) {
            return delete.wasAcknowledged();
        } else {
            throw new ODataServiceFault("Document ID: " + documentId + " does not exist in "
                    + "collection: " + tableName + ".");
        }
    }

    /**
     * This method updates the given entity in the given collection.
     *
     * @param tableName     Name of the table
     * @param newProperties New Properties
     * @throws ODataServiceFault
     */
    @Override
    public boolean updateEntityInTable(String tableName, ODataEntry newProperties) throws ODataServiceFault {
        List<String> primaryKeyList = this.primaryKeys.get(tableName);
        String newPropertyObjectKeyValue = newProperties.getValue(DOCUMENT_ID);
        StringBuilder mongoUpdate = new StringBuilder();
        mongoUpdate.append("{$set: {");
        boolean propertyMatch = false;
        for (String column : newProperties.getData().keySet()) {
            if (!primaryKeyList.contains(column)) {
                if (propertyMatch) {
                    mongoUpdate.append("', ");
                }
                String propertyValue = newProperties.getValue(column);
                mongoUpdate.append(column).append(": '").append(propertyValue);
                propertyMatch = true;
            }
        }
        mongoUpdate.append("'}}");
        String query = mongoUpdate.toString();
        WriteResult update = jongo.getCollection(tableName).update(new ObjectId(newPropertyObjectKeyValue)).with(query);
        int wasUpdated = update.getN();
        if (wasUpdated == 1) {
            return update.wasAcknowledged();
        } else {
            throw new ODataServiceFault("Document ID: " + newPropertyObjectKeyValue
                    + " does not exist in collection: " + tableName + ".");
        }
    }

    /**
     * This method updates the entity in table when transactional update is necessary.
     *
     * @param tableName     Table Name
     * @param oldProperties Old Properties
     * @param newProperties New Properties
     * @throws ODataServiceFault
     */
    @Override
    public boolean updateEntityInTableTransactional(String tableName, ODataEntry oldProperties,
                                                    ODataEntry newProperties) throws ODataServiceFault {
        String oldPropertyObjectKeyValue = oldProperties.getValue(DOCUMENT_ID);
        StringBuilder updateNewProperties = new StringBuilder();
        updateNewProperties.append("{$set: {");
        boolean propertyMatch = false;
        for (String column : newProperties.getData().keySet()) {
            if (propertyMatch) {
                updateNewProperties.append("', ");
            }
            String propertyValue = newProperties.getValue(column);
            updateNewProperties.append(column).append(": '").append(propertyValue);
            propertyMatch = true;
        }
        updateNewProperties.append("'}}");
        String query = updateNewProperties.toString();
        WriteResult update = jongo.getCollection(tableName).update(new ObjectId(oldPropertyObjectKeyValue)).with(query);
        int wasUpdated = update.getN();
        if (wasUpdated == 1) {
            return update.wasAcknowledged();
        } else {
            throw new ODataServiceFault("Error occured while updating the entity to collection :"
                    + tableName + ".");
        }
    }

    @Override
    public Map<String, NavigationTable> getNavigationProperties() {
        return null;
    }

    private ThreadLocal<Boolean> transactionAvailable = new ThreadLocal<Boolean>() {
        protected synchronized Boolean initialValue() {
            return false;
        }
    };

    /**
     * This method opens the transaction.
     */
    @Override
    public void openTransaction() {
        this.transactionAvailable.set(true);
        // doesn't support
    }

    /**
     * This method commits the transaction.
     */
    @Override
    public void commitTransaction() {
        this.transactionAvailable.set(false);
        // doesn't support
    }

    /**
     * This method rollbacks the transaction.
     */
    @Override
    public void rollbackTransaction() {
        this.transactionAvailable.set(false);
        // doesn't support
    }

    /**
     * This method updates the references of the table where the keys were imported.
     *
     * @param rootTableName       Root - Table Name
     * @param rootTableKeys       Root - Entity keys (Primary Keys)
     * @param navigationTable     Navigation - Table Name
     * @param navigationTableKeys Navigation - Entity Name (Primary Keys)
     * @throws ODataServiceFault
     */
    @Override
    public void updateReference(String rootTableName, ODataEntry rootTableKeys, String navigationTable,
                                ODataEntry navigationTableKeys) throws ODataServiceFault {
        throw new ODataServiceFault("MongoDB datasources do not support references.");
    }

    /**
     * This method deletes the references of the table where the keys were imported.
     *
     * @param rootTableName       Root - Table Name
     * @param rootTableKeys       Root - Entity keys (Primary Keys)
     * @param navigationTable     Navigation - Table Name
     * @param navigationTableKeys Navigation - Entity Name (Primary Keys)
     * @throws ODataServiceFault
     */

    @Override
    public void deleteReference(String rootTableName, ODataEntry rootTableKeys, String navigationTable,
                                ODataEntry navigationTableKeys) throws ODataServiceFault {
        throw new ODataServiceFault("MongoDB datasources do not support references.");
    }

    @Override
    public int getEntityCount(String tableName) {
        DBCollection readResult = jongo.getDatabase().getCollection(tableName);
        int rowCount = (int) readResult.getCount();
        return rowCount;
    }

    @Override
    public int getEntityCountWithKeys(String tableName, ODataEntry keys) throws ODataServiceFault {
        throw new ODataServiceFault("MongoDB datasources doesn't support navigation.");
    }
}   
