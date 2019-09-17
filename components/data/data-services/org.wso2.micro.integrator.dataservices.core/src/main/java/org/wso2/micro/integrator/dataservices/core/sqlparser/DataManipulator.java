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
package org.wso2.micro.integrator.dataservices.core.sqlparser;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class DataManipulator {

//    public Object[] retrieveColumnData(List<Object[]> joinedTableData, int index) {
//        Object[] columnData = new Object[joinedTableData.size()];
//
//        for (int i = 0; i < joinedTableData.size(); i++) {
//            columnData[i] = joinedTableData.get(i)[index];
//        }
//        return columnData;
//    }

//    public List<Integer> doUnion(List<Integer> retrievedIndices, List<Integer> listIndices) {
//        List<Integer> tempIndex = new ArrayList<Integer>();
//
//        for (Integer selectedRow : retrievedIndices) {
//            if (!listIndices.contains(selectedRow)) {
//               tempIndex.add(selectedRow);
//            }
//        }
//        for (Integer newItem : tempIndex) {
//            listIndices.add(newItem);
//        }
//        Collections.sort(listIndices);
//
//        return listIndices;
//    }

//    public List<Integer> doIntersection(List<Integer> retrievedList, List<Integer> listIndices) {
//        List<Integer> tempIndex = new ArrayList<Integer>();
//
//        for (Integer selectedRow : retrievedList) {
//            if (listIndices.contains(selectedRow)) {
//                tempIndex.add(selectedRow);
//            }
//        }
//        return tempIndex;
//    }

    public float processMax(Queue<String> processedTokens) {
        List<String> columnData = retrieveColumnData(processedTokens.poll());

        float max = Float.parseFloat(columnData.get(0));
        for (int i = 1; i < columnData.size(); i++) {
            max = Math.max(max, Float.parseFloat(columnData.get(i)));
        }
        return max;
    }

    public float processMin(Queue<String> processedTokens) {
        List<String> columnData = retrieveColumnData(processedTokens.poll());

        float min = Float.parseFloat(columnData.get(0));
        for (int i = 1; i < columnData.size(); i++) {
            min = Math.min(min, Float.parseFloat(columnData.get(i)));
        }
        return min;
    }

    public int processCount(Queue<String> processedTokens) {
        List<String> columnData = retrieveColumnData(processedTokens.poll());
        return columnData.size();
    }

    public float processSum(Queue<String> processedTokens) {
        float sum = 0;
        List<String> columnData = retrieveColumnData(processedTokens.poll());

        for (String columnDataCell : columnData) {
            sum += Float.parseFloat(columnDataCell);
        }
        return sum;
    }

    public float processAvg(Queue<String> processedTokens) {
        float sum = 0;
        List<String> columnData = retrieveColumnData(processedTokens.poll());

        for (String columnDataCell : columnData) {
            sum += Float.parseFloat(columnDataCell);
        }
        return (sum / columnData.size());
    }

    public List<String> concatDataFunction(
            List<String> columnData, String concatOperator, List<String> concatData) {
        List<String> concatedData = new ArrayList<String>();

        if (concatOperator != null) {
            for (String columnDataElement : columnData) {
                concatedData.add(columnDataElement + concatOperator);
            }
        } else {
            for (int i = 0; i < columnData.size(); i++) {
                concatedData.add(concatData.get(i) + columnData.get(i));
            }
        }
        return concatedData;
    }

    public List<String> retrieveColumnData(String columnName) {
        return new ArrayList<String>();
    }

}
