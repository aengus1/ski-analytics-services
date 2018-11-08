package com.serverless;
import com.sun.jersey.api.client.Client;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import javax.ws.rs.core.MediaType;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class JUnit5ExampleTest {
    private String accessKey = null;
    private AuthenticationHelper helper = null;
    private final String testUserName = "testUser@test.com";
    private final String testPassword = "testPassword123";
    @BeforeAll
    void retrieveAccessKey(){

        //TODO retrieve userPoolID, clientID and region from cloudformation outputs
        String clientId = "755f5d0elsg5ie96116540qm3u";
        String userPoolId = "us-west-2_FrH0UdrNz";
        String region = "us-west-2";
        String profileName = "backend_dev";
        this.helper = new AuthenticationHelper(userPoolId, clientId, "", region, profileName);

        helper.PerformAdminSignup(testUserName, testPassword);

        this.accessKey = helper.PerformSRPAuthentication(testUserName, testPassword);

    }
    @Test
    void justAnExample() {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target("http://foo").path("bar");
        Invocation.Builder invocationBuilder = target.request(MediaType.TEXT_PLAIN_TYPE);
        Response response = invocationBuilder.get();
    }

//    @Test
//    void testAccessKey() {
//        AuthenticationHelper helper = new AuthenticationHelper("us-west-2_FrH0UdrNz",
//                "755f5d0elsg5ie96116540qm3u", "secretkey", "us-west-2");
//
//        String result = helper.PerformSRPAuthentication("aengusmccullough@hotmail.com", "Norwich123");
//        System.out.println("result = " + result);
//
//
//    }

    @AfterAll
    void tearDown() {
        helper.deleteUser(this.testUserName);
    }
}
