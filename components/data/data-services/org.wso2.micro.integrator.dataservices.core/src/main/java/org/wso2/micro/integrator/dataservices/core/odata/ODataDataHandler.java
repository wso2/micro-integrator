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

import org.apache.olingo.server.api.uri.queryoption.OrderByOption;
import org.wso2.micro.integrator.dataservices.core.engine.DataEntry;

import java.util.List;
import java.util.Map;

/**
 * This class represents an odata data handler used when using odata db handles.
 */
public interface ODataDataHandler {

    /**
     * This method read the table data and return.
     * Return a list of DataEntry object which has been wrapped the entity.
     *
     * @param tableName Name of the table
     * @return EntityCollection
     * @throws ODataServiceFault
     * @see DataEntry
     */
    List<ODataEntry> readTable(String tableName) throws ODataServiceFault;

    /**
     * This method read the table with Keys and return.
     * Return a list of DataEntry object which has been wrapped the entity.
     *
     * @param tableName Name of the table
     * @param keys      Keys to check
     * @return EntityCollection
     * @throws ODataServiceFault
     * @see DataEntry
     */
    List<ODataEntry> readTableWithKeys(String tableName, ODataEntry keys) throws ODataServiceFault;

    /**
     * This method inserts entity to table.
     *
     * @param tableName Name of the table
     * @param entity    Entity
     * @throws ODataServiceFault
     */
    ODataEntry insertEntityToTable(String tableName, ODataEntry entity) throws ODataServiceFault;

    /**
     * This method deletes entity from table.
     *
     * @param tableName Name of the table
     * @param entity    Entity
     * @throws ODataServiceFault
     */
    boolean deleteEntityInTable(String tableName, ODataEntry entity) throws ODataServiceFault;

    /**
     * This method updates entity in table.
     *
     * @param tableName     Name of the table
     * @param newProperties New Properties
     * @throws ODataServiceFault
     */
    boolean updateEntityInTable(String tableName, ODataEntry newProperties) throws ODataServiceFault;

    /**
     * This method updates the entity in table when transactional update is necessary.
     *
     * @param tableName     Table Name
     * @param oldProperties Old Properties
     * @param newProperties New Properties
     * @throws ODataServiceFault
     */
    boolean updateEntityInTableTransactional(String tableName, ODataEntry oldProperties, ODataEntry newProperties)
            throws ODataServiceFault;

    /**
     * This method return database table metadata.
     * Return a map with table name as the key, and the values contains maps with column name as the map key,
     * and the values of the column name map will DataColumn object, which represents the column.
     *
     * @return Database Metadata
     * @see DataColumn
     */
    Map<String, Map<String, DataColumn>> getTableMetadata();

    /**
     * This method return names of all the tables in the database.
     *
     * @return Table list.
     */
    List<String> getTableList();

    /**
     * This method returns the all the primary keys in the database tables.
     * Return a map with table name as the keys, and the values contains a list of column names which are act as primary keys in the table.
     *
     * @return Primary Key Map
     */
    Map<String, List<String>> getPrimaryKeys();

    /**
     * This method returns the navigation property map, which contains the Navigation table which contains the all the navigation paths from the table,
     *
     * @return NavigationProperty Map
     */
    Map<String, NavigationTable> getNavigationProperties();

    /**
     * This method opens the transaction.
     *
     * @throws ODataServiceFault
     */
    void openTransaction() throws ODataServiceFault;

    /**
     * This method commits the transaction.
     *
     * @throws ODataServiceFault
     */
    void commitTransaction() throws ODataServiceFault;

    /**
     * This method rollbacks the transaction.
     *
     * @throws ODataServiceFault
     */
    void rollbackTransaction() throws ODataServiceFault;

    /**
     * This method updates the references of the table where the keys were imported.
     *
     * @param rootTableName       Root - Table Name
     * @param rootTableKeys       Root - Entity keys (Primary Keys)
     * @param navigationTable     Navigation - Table Name
     * @param navigationTableKeys Navigation - Entity Name (Primary Keys)
     * @throws ODataServiceFault
     */
    void updateReference(String rootTableName, ODataEntry rootTableKeys, String navigationTable,
                         ODataEntry navigationTableKeys) throws ODataServiceFault;

    /**
     * This method deletes the references of the table where the keys were imported.
     *
     * @param rootTableName       Root - Table Name
     * @param rootTableKeys       Root - Entity keys (Primary Keys)
     * @param navigationTable     Navigation - Table Name
     * @param navigationTableKeys Navigation - Entity Name (Primary Keys)
     * @throws ODataServiceFault
     */
    void deleteReference(String rootTableName, ODataEntry rootTableKeys, String navigationTable,
                         ODataEntry navigationTableKeys) throws ODataServiceFault;

    /**
     * This method reads a table to the stream buffer.
     * Returns a list of ODataEntry objects.
     *
     * @param tableName Name of the table
     * @return List of OData entries
     * @throws ODataServiceFault
     */
    List<ODataEntry> streamTable(String tableName) throws ODataServiceFault;

    /**
     * This method reads a table with keys to the stream buffer.
     * Returns a list of ODataEntry objects.
     *
     * @param tableName Name of the table
     * @param keys      Keys to check
     * @return List of OData entries
     * @throws ODataServiceFault
     */
    List<ODataEntry> streamTableWithKeys(String tableName, ODataEntry keys) throws ODataServiceFault;

    /**
     * This method reads a sorted table to the stream buffer.
     * Returns a list of ODataEntry objects.
     *
     * @param tableName     Name of the table
     * @param orderByOption Order by options
     * @return List of OData entries
     * @throws ODataServiceFault
     */
    List<ODataEntry> streamTableWithOrder(String tableName, OrderByOption orderByOption) throws ODataServiceFault;

    /**
     * This method returns the number of entities in a table.
     *
     * @param tableName Name of the table
     * @return Entity count
     * @throws ODataServiceFault
     */
    int getEntityCount(String tableName) throws ODataServiceFault;

    /**
     * This method returns the number of entities in a table after applying an OData query.
     *
     * @param tableName Name of the table
     * @param keys      Keys to check
     * @return Entity count
     * @throws ODataServiceFault
     */
    int getEntityCountWithKeys(String tableName, ODataEntry keys) throws ODataServiceFault;

    /**
     * This method initializes the ODataHandlers for streaming.
     */
    void initStreaming();
}
