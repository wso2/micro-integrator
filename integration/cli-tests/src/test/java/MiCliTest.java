import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
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
                Assert.assertTrue(line.toString().contains("addressEP_Test"));
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
                Assert.assertTrue(line.toString().contains("switchMediatorSwitchByAddressTestProxy"));
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

