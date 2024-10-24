package be.ugent.rml;

import be.ugent.rml.cli.Main;
import be.ugent.rml.store.QuadStore;
import be.ugent.rml.store.QuadStoreFactory;
import be.ugent.rml.target.SolidTargetHelper;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

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
        // We need a fixed host port because the port is part of the pod uri (also of the webid, acl file)
        // Test containers advise against the use of a fixed host port, and deprecated the related solutions
        // Finally, found a workaround here: https://github.com/testcontainers/testcontainers-java/issues/256
        PortBinding portBinding = new PortBinding(Ports.Binding.bindPort(3000), new ExposedPort(3000));
        try (GenericContainer<?> container = new GenericContainer<>(DockerImageName.parse("elsdvlee/solid-testpods:latest"))
                .withCreateContainerCmdModifier(cmd -> cmd.withHostConfig(new HostConfig().withPortBindings(portBinding)))
                .withExposedPorts(3000)
                .withCommand("npm", "start")
                .waitingFor(Wait.forHealthcheck()).withStartupTimeout(Duration.ofSeconds(200))){
            container.start();
            Main.run(("-m " + mapPath).split(" "));
            int i = 0;
            while (i < resourceUrls.length) {
                Map<String, String> solidTargetInfo = getSolidTargetInfo(users[i], resourceUrls[i]);
                compareResourceWithOutput(outPaths[i], solidTargetInfo);
                i++;
            }
        }
    }

    // get solidTargetInfo including authentication details of testpods
    private Map<String, String> getSolidTargetInfo(String user, String resourceUrl){
        Map<String, String> solidTargetInfo = new HashMap<String,String>();
        solidTargetInfo.put("email", "hello@" + user + ".com");
        solidTargetInfo.put("password","abc123");
        solidTargetInfo.put("serverUrl", "http://localhost:3000/");
        solidTargetInfo.put("webId", "http://localhost:3000/" + user + "/profile/card#me");
        solidTargetInfo.put("resourceUrl", resourceUrl);
        return solidTargetInfo;
    }

    private void compareResourceWithOutput(String outPath, Map<String,String> solidTargetInfo) throws Exception {
        // retrieve resource from solid pod
        SolidTargetHelper helper = new SolidTargetHelper();
        String response = helper.getResource(solidTargetInfo);
        InputStream responseStream = new ByteArrayInputStream(response.getBytes());
        QuadStore result = QuadStoreFactory.read(responseStream, RDFFormat.NQUADS);
        // compare result to expected output
        result.removeDuplicates();
        compareStores(filePathToStore(outPath), result);
    }
}

