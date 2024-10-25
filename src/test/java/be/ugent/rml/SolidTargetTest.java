package be.ugent.rml;

import be.ugent.rml.cli.Main;
import be.ugent.rml.store.QuadStore;
import be.ugent.rml.store.QuadStoreFactory;
import be.ugent.rml.target.SolidTargetHelper;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SolidTargetTest extends TestCore {

    //constant target in subject map
    @Test
    public void solid1() throws Exception {
        doMappingSolid("solid-target/solid1/mapping.ttl",
                "https://pod.playground.solidlab.be/user1/rmlmapper/building",
                "solid-target/solid1/output1.nq",
                "user1");
    }

    //constant target in predicate map
    @Test
    public void solid2() throws Exception {
        doMappingSolid("solid-target/solid2/mapping.ttl",
                "https://pod.playground.solidlab.be/user1/rmlmapper/building",
                "solid-target/solid2/output1.nq",
                "user1");
    }

    //constant target in object map
    @Test
    public void solid3() throws Exception {
        doMappingSolid("solid-target/solid3/mapping.ttl",
                "https://pod.playground.solidlab.be/user1/rmlmapper/building",
                "solid-target/solid3/output1.nq",
                "user1");
    }

    //constant target in subject graph
    @Test
    public void solid4() throws Exception {
        doMappingSolid("solid-target/solid4/mapping.ttl",
                "https://pod.playground.solidlab.be/user1/rmlmapper/building",
                "solid-target/solid4/output1.nq",
                "user1");
    }

    //constant target in po graph
    @Test
    public void solid5() throws Exception {
        doMappingSolid("solid-target/solid5/mapping.ttl",
                "https://pod.playground.solidlab.be/user1/rmlmapper/building",
                "solid-target/solid5/output1.nq",
                "user1");
    }

    //acl for pod1, resourceUrl with .ttl
    @Test
    public void acl1() throws Exception{
        doMappingSolid("solid-target/acl1/mapping.ttl",
                "https://pod.playground.solidlab.be/user1/rmlmapper/building.ttl",
                "solid-target/acl1/output1.nq",
                "user2");
    }

    //acl for pod1, resourceUrl without .ttl
    @Test
    public void acl2() throws Exception{
        doMappingSolid("solid-target/acl2/mapping.ttl",
                "https://pod.playground.solidlab.be/user1/rmlmapper/building",
                "solid-target/acl2/output1.nq",
                "user2");
    }

    void doMappingSolid(String mapPath, String resourceUrl, String outPath, String user) throws Exception {
        doMappingSolid(mapPath, new String[]{resourceUrl}, new String[]{outPath},new String[]{user});
    }

    void doMappingSolid(String mapPath, String[] resourceUrls, String[] outPaths, String[] users) throws Exception {
        Main.run(("-m " + mapPath).split(" "));
        SolidTargetHelper helper = new SolidTargetHelper();
        int i = 0;
        while (i < resourceUrls.length) {
            Map<String, String> solidTargetInfo = getSolidTargetInfo(users[i], resourceUrls[i]);
            compareResourceWithOutput(outPaths[i], solidTargetInfo);
            helper.deleteResource(getSolidTargetInfo("user1", resourceUrls[i]));
            i++;
        }
    }

    // get solidTargetInfo including authentication details of testpods
    private Map<String, String> getSolidTargetInfo(String user, String resourceUrl){
        Map<String, String> solidTargetInfo = new HashMap<>();
        solidTargetInfo.put("email", user + "@pod.playground.solidlab.be");
        solidTargetInfo.put("password",user);
        solidTargetInfo.put("serverUrl", "https://pod.playground.solidlab.be/");
        solidTargetInfo.put("webId", "https://pod.playground.solidlab.be/" + user + "/profile/card#me");
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

    private static void replaceStringInFile(Path filePath, String target, String replacement) {
        try {
            List<String> lines = Files.readAllLines(filePath, StandardCharsets.UTF_8);
            for (int i = 0; i < lines.size(); i++) {
                lines.set(i, lines.get(i).replace(target, replacement));
            }
            Files.write(filePath, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
