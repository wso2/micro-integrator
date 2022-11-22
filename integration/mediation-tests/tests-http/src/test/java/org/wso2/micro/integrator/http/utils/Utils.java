/*
 * Copyright (c) 2022, WSO2 LLC (http://www.wso2.com).
 *
 * WSO2 LLC licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.micro.integrator.http.utils;

import org.awaitility.Awaitility;
import org.wso2.esb.integration.common.utils.CPUMonitor;

import java.util.concurrent.TimeUnit;

import static org.wso2.micro.integrator.http.utils.Constants.CPU_POLL_INTERVAL;
import static org.wso2.micro.integrator.http.utils.Constants.CPU_POLL_TIMEOUT;

/**
 * This util class contains the helper methods used in HTTP Core test cases.
 */
public class Utils {

    /**
     * Get the request payload according to the payload size.
     *
     * @param payloadSize The required request size defined according to PayloadSize
     * @return string payload
     */
    public static String getPayload(PayloadSize payloadSize) {

        if (PayloadSize.LARGE.equals(payloadSize)) {
            return SamplePayloads.LARGE_PAYLOAD;
        } else if (PayloadSize.SMALL.equals(payloadSize)) {
            return SamplePayloads.SMALL_PAYLOAD;
        }
        return "";
    }

    /**
     * This method will poll the cpu usage with an interval CPU_POLL_INTERVAL for a maximum time of CPU_POLL_TIMEOUT
     * to check the CPU is settled.
     *
     * @param cpuMonitor The CPU Monitor instance which is used by the test case
     * @param alias      Alias to track the assertion was done before or after closing the socket
     */
    public static void checkCPUUsage(CPUMonitor cpuMonitor, String alias) {

        Awaitility.await(alias)
                .pollInterval(CPU_POLL_INTERVAL, TimeUnit.SECONDS).atMost(CPU_POLL_TIMEOUT,
                TimeUnit.SECONDS)
                .until(cpuMonitor.isCPUSettled());
    }
}
