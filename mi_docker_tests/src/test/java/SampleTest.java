import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;

public class SampleTest {

    /**
     * Tests to verify hello world sample.
     */

    @Test
    public void sample_test_1() {
        Response response=
                given().
                        when().
                        get("http://localhost:8290/hello-world");
        Assert.assertTrue(response.statusCode()==200);
        System.out.println("Response: "+response.getBody().asString());
    }

}
