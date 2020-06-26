/*
Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.micro.integrator.initializer.handler.transaction.store.connector;

/**
 * This class contains all the data base queries related to transaction counting.
 */
class TransactionQueryHelper {

    static final String INSERT_RAW = "INSERT INTO CURRENT_STATS (TIME_STAMP, NODE_ID, TRANSACTION_COUNT," +
            "TRANSACTION_COUNT_ENCRYPTED) VALUES (?,?,?,?)";
    static final String GET_TRAN_COUNT = "SELECT TRANSACTION_COUNT FROM CURRENT_STATS WHERE TIME_STAMP=?" +
            " AND NODE_ID=?";
    static final String UPDATE_TRAN_COUNT = "UPDATE CURRENT_STATS SET TRANSACTION_COUNT=?, " +
            "TRANSACTION_COUNT_ENCRYPTED=? WHERE NODE_ID=? AND TIME_STAMP=?";
    static final String TRAN_COUNT_OF_MONTH =
            "SELECT SUM(TRANSACTION_COUNT) FROM CURRENT_STATS WHERE TIME_STAMP =?";
    static final String GET_TRAN_COUNT_DATA_FOR_A_TIME_PERIOD =
            "SELECT * FROM CURRENT_STATS WHERE TIME_STAMP BETWEEN ? AND ?";
}
