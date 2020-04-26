package ski.crunch.activity;

import ski.crunch.testhelpers.IntegrationTestHelper;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * helper class to bulk submit all the fit files in the resources folder to the PutActivity endpoint
 */
public class BatchUploader {
    private String userId;
    private String jwtToken;
    private IntegrationTestHelper helper;

    public BatchUploader() throws Exception {
        helper = new IntegrationTestHelper();
        this.userId = helper.signup().orElseThrow(() -> new RuntimeException("Error occurred signing up"));
        this.jwtToken = helper.retrieveAccessToken();
    }


    public boolean batchUploadActivities(List<String> resources) throws IOException {


        String endpoint = helper.getApiEndpoint();

        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(endpoint).path("activity");

        List<String> responses = new ArrayList<>();

        for (String resource : resources) {
            InputStream is = getResourceAsStream("../resources/" + resource);
            Entity payload = Entity.entity(is, MediaType.valueOf("application/fit"));

            Response response = target.request(MediaType.APPLICATION_OCTET_STREAM_TYPE)
                    .accept("application/fit")
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/fit")
                    .header("Authorization", this.jwtToken)
                    .buildPut(payload).invoke();

            responses.add(response.readEntity(String.class));
            is.close();
        }

        for (String response : responses) {
            System.out.println("response = " + response);
        }


        return true;
    }

    public List<String> getResourceFitFiles() throws IOException {
        List<String> filenames = new ArrayList<>();

        try (
                InputStream in = getResourceAsStream("../resources/");
                BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
            String resource;

            while ((resource = br.readLine()) != null) {
                filenames.add(resource);
            }
        }

        return filenames;
    }


    public void cleanupResources() {
        helper.destroySignupUser();
        helper.removeUserSettings(this.userId);
    }

    private InputStream getResourceAsStream(String resource) {
        final InputStream in
                = getContextClassLoader().getResourceAsStream(resource);

        return in == null ? getClass().getResourceAsStream(resource) : in;
    }

    private ClassLoader getContextClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }


    public static void main(String[] args) {
        BatchUploader batchUploader = null;
        try {
            batchUploader = new BatchUploader();
            List<String> files = batchUploader.getResourceFitFiles().stream().filter
                    (x -> x.endsWith(".fit")).collect(Collectors.toList()
            );
            batchUploader.batchUploadActivities(files);

        } catch (Exception ex) {
            ex.printStackTrace();

        } finally {
            batchUploader.cleanupResources();
        }
    }
}
