package be.ugent.rml;

import be.ugent.rml.cli.Main;
import be.ugent.rml.store.QuadStore;
import be.ugent.rml.store.QuadStoreFactory;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

public class SolidTargetTest extends TestCore {

    //constant target in subject map
    @Test
    public void solid1() throws Exception {
        doMappingSolid("solid-target/solid1/mapping.ttl",
                "http://localhost:3000/example/building",
                "solid-target/solid1/output1.nq",
                "example");
    }

    //constant target in predicate map
    @Test
    public void solid2() throws Exception {
        doMappingSolid("solid-target/solid2/mapping.ttl",
                "http://localhost:3000/example/building",
                "solid-target/solid2/output1.nq",
                "example");
    }

    //constant target in object map
    @Test
    public void solid3() throws Exception {
        doMappingSolid("solid-target/solid3/mapping.ttl",
                "http://localhost:3000/example/building",
                "solid-target/solid3/output1.nq",
                "example");
    }

    //constant target in subject graph
    @Test
    public void solid4() throws Exception {
        doMappingSolid("solid-target/solid4/mapping.ttl",
                "http://localhost:3000/example/building",
                "solid-target/solid4/output1.nq",
                "example");
    }

    //constant target in po graph
    @Test
    public void solid5() throws Exception {
        doMappingSolid("solid-target/solid5/mapping.ttl",
                "http://localhost:3000/example/building",
                "solid-target/solid5/output1.nq",
                "example");
    }

    //acl for pod1, resourceUrl with .ttl
    @Test
    public void acl1() throws Exception{
        doMappingSolid("solid-target/acl1/mapping.ttl",
                "http://localhost:3000/example/building.ttl",
                "solid-target/acl1/output1.nq",
                "pod1");
    }

    //acl for pod1, resourceUrl without .ttl
    @Test
    public void acl2() throws Exception{
        doMappingSolid("solid-target/acl2/mapping.ttl",
                "http://localhost:3000/example/building",
                "solid-target/acl2/output1.nq",
                "pod1");
    }

    void doMappingSolid(String mapPath, String resourceUrl, String outPath, String user) throws Exception {
        doMappingSolid(mapPath, new String[]{resourceUrl}, new String[]{outPath},new String[]{user});
    }

    void doMappingSolid(String mapPath, String[] resourceUrls, String[] outPaths, String[] users) throws Exception {
        try {
            GenericContainer<?> container = new GenericContainer<>(DockerImageName.parse("elsdvlee/solid-target-helper-and-testpods:latest"))
                    .withExposedPorts(8080)
                    .withCommand("npm", "start")
                    .waitingFor(Wait.forHealthcheck()).withStartupTimeout(Duration.ofSeconds(200));
            container.start();
            String address = "http://" + container.getHost() + ":" + container.getMappedPort(8080) + "/";
            Main.run(("-m " + mapPath + " -shu " + address).split(" "));
            int i = 0;
            while (i < resourceUrls.length) {
                JSONObject solidTargetInfo = getSolidTargetInfo(users[i], resourceUrls[i]);
                compareResourceWithOutput(outPaths[i], solidTargetInfo, address);
                i++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // get solidTargetInfo including authentication details of testpods
    private JSONObject getSolidTargetInfo(String user, String resourceUrl){
        JSONObject solidTargetInfo = new JSONObject();
        solidTargetInfo.put("email", "hello@" + user + ".com");
        solidTargetInfo.put("password","abc123");
        solidTargetInfo.put("serverUrl", "http://localhost:3000/");
        solidTargetInfo.put("webId", "http://localhost:3000/" + user + "/profile/card#me");
        solidTargetInfo.put("resourceUrl", resourceUrl);
        return solidTargetInfo;
    }

    private void compareResourceWithOutput(String outPath, JSONObject solidTargetInfo, String address) throws Exception {
        // retrieve resource from solid pod
        URL url = new URL(address + "getResource");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("content-type", "application/json");
        connection.setRequestProperty("Accept", "text/plain");
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        OutputStream out = connection.getOutputStream();
        out.write((solidTargetInfo.toString()).getBytes(StandardCharsets.UTF_8));
        out.close();
        // get result
        QuadStore result = QuadStoreFactory.read(connection.getInputStream(), RDFFormat.NQUADS);
        connection.getInputStream().close();

        // compare result to expected output
        result.removeDuplicates();
        compareStores(filePathToStore(outPath), result);
    }
}

