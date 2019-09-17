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
package org.wso2.micro.integrator.dataservices.core.dispatch;

/**
 * This class contains operations to find out the current dispatch status.
 */
public class DispatchStatus {

    private static ThreadLocal<Boolean> batchRequest = new ThreadLocal<Boolean>() {
        @Override
        protected synchronized Boolean initialValue() {
            return false;
        }
    };

    private static ThreadLocal<Boolean> boxcarringRequest = new ThreadLocal<Boolean>() {
        @Override
        protected synchronized Boolean initialValue() {
            return false;
        }
    };

    /**
     * thread local variable to keep the current batch request size
     */
    private static ThreadLocal<Integer> batchRequestCount = new ThreadLocal<Integer>() {
        protected synchronized Integer initialValue() {
            return 0;
        }
    };

    /**
     * thread local variable to keep the current batch request number, 0 based
     */
    private static ThreadLocal<Integer> batchRequestNumber = new ThreadLocal<Integer>() {
        protected synchronized Integer initialValue() {
            return 0;
        }
    };

    public static void clearRequestStatus() {
        batchRequest.set(false);
        boxcarringRequest.set(false);
        batchRequestCount.set(0);
        batchRequestNumber.set(0);
    }

    public static void clearBatchRequestStatus() {
        batchRequest.set(false);
        batchRequestCount.set(0);
        batchRequestNumber.set(0);
    }

    public static void setBatchRequest() {
        batchRequest.set(true);
    }
    
    public static void setBoxcarringRequest() {
        boxcarringRequest.set(true);
    }
    
    public static boolean isBatchRequest() {
        return batchRequest.get();
    }

    public static int getBatchRequestCount() {
        return batchRequestCount.get();
    }

    public static void setBatchRequestCount(int val) {
        batchRequestCount.set(val);
    }

    public static int getBatchRequestNumber() {
        return batchRequestNumber.get();
    }

    public static void setBatchRequestNumber(int val) {
        batchRequestNumber.set(val);
    }

    public static boolean isBoxcarringRequest() {
        return boxcarringRequest.get();
    }
    
    public static boolean isInBatchBoxcarring() {
        return isBatchRequest() || isBoxcarringRequest();
    }
    
    public static boolean isFirstBatchRequest() {
        return batchRequestNumber.get() == 0;
    }
    
    public static boolean isLastBatchRequest() {
        return batchRequestNumber.get() + 1 >= batchRequestCount.get();
    }
    
}
