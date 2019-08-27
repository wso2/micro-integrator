/*
 *Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *WSO2 Inc. licenses this file to you under the Apache License,
 *Version 2.0 (the "License"); you may not use this file except
 *in compliance with the License.
 *You may obtain a copy of the License at
 *
 *http://www.apache.org/licenses/LICENSE-2.0
 *
 *Unless required by applicable law or agreed to in writing,
 *software distributed under the License is distributed on an
 *"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *KIND, either express or implied.  See the License for the
 *specific language governing permissions and limitations
 *under the License.
 */

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Assert;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class MiCliTest {
    /**
     * setup the environment to run the tests
     */
    @BeforeClass
    public static void setupEnv() throws IOException {

        Process process = new ProcessBuilder("../../integration/cli-tests/src/test/java/EnvSetup.sh")
                .start();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            String line;

            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        }
    }

    /**
     * Get information about all the API's
     */
    @Test
    public void miShowApiAllTest() throws IOException {
        Process process = new ProcessBuilder("../../cmd/build/wso2mi-cli--f/bin/mi", "-v", "show", "api")
                .start();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            String line;

            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        }
    }

    /**
     * Get information about single API's
     */
    @Test
    public void miShowApiTest() throws IOException {

        Process process = new ProcessBuilder("../../cmd/build/wso2mi-cli--f/bin/mi", "-v", "show" , "api")
                .start();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            String line;

            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        }
    }

    /**
     * Get information about all the Endpoints
     */
    @Test
    public void miShowEndpointAllTest() throws IOException {
        Process process = new ProcessBuilder("../../cmd/build/wso2mi-cli--f/bin/mi", "-v", "show", "endpoint")
                .start();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            String line;

            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        }

    }

    /**
     * Get information about single Endpoint
     */

    @Test
    public void miShowEndpointTest() throws IOException {

        Process process = new ProcessBuilder("../../cmd/build/wso2mi-cli--f/bin/mi" , "-v", "endpoint", "addressEP_Test")
                .start();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            String line;

            while ((line = reader.readLine()) != null) {
                System.out.println(line);
//                Assert.assertTrue(line.toString().contains("addressEP_Test"));
            }
        }
    }

    /**
     * Get information about all the Proxy services
     */

    @Test
    public void miShowProxyAllTest() throws IOException {
        Process process = new ProcessBuilder("../../cmd/build/wso2mi-cli--f/bin/mi", "-v", "show", "proxyservice")
                .start();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            String line;

            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        }

    }

    /**
     * Get information about single proxy service
     */

    @Test
    public void miShowProxyTest() throws IOException {

        Process process = new ProcessBuilder("../../cmd/build/wso2mi-cli--f/bin/mi", "-v", "endpoint", "switchMediatorSwitchByAddressTestProxy")
                .start();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            String line;

            while ((line = reader.readLine()) != null) {
                System.out.println(line);
//                Assert.assertTrue(line.toString().contains("switchMediatorSwitchByAddressTestProxy"));
            }
        }
    }

    /**
     * Get information about all the Inbound endpoints
     */

    @Test
    public void miShowInboundEPAllTest() throws IOException {
        Process process = new ProcessBuilder("../../cmd/build/wso2mi-cli--f/bin/mi", "-v", "show", "inboundendpoint")
                .start();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            String line;

            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        }

    }

    /**
     * Get information about single Inbound endpoints
     */

    @Test
    public void miShowInboundEPTest() throws IOException {

        Process process = new ProcessBuilder("../../cmd/build/wso2mi-cli--f/bin/mi", "-v", "inboundendpoint", "MQTT_Test_Inbound_EP")
                .start();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            String line;

            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        }
    }

}

