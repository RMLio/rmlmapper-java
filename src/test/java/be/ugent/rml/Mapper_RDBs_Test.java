package be.ugent.rml;

;
import ch.vorburger.mariadb4j.DBConfigurationBuilder;
import org.junit.Test;

import ch.vorburger.mariadb4j.DB;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;


// IMPORTANT FOR TESTING:
//  USE DYNAMIC PORT NUMBER AS DESCRIBED BELOW OR USE STATIC PORT NUMBERS IN MAPPING FILE's JDBC DSN
public class Mapper_RDBs_Test extends TestCore {

    private static final int PORTNUMBER = 50898;

    public DB startDB(String resourcePath, String mappingPath) throws Exception {
        DBConfigurationBuilder configBuilder = DBConfigurationBuilder.newBuilder();
        configBuilder.setPort(PORTNUMBER);
        DB database = DB.newEmbeddedDB(configBuilder.build());
        database.start();
        database.source(resourcePath);

        // To set the correct port number in the mapping file in a dynamic way:
        //  Change PORTNUMBER constant to 0 to automatically detect free port
        //  Change "jdbc:mysql://localhost:50898/test" to "jdbc:mysql://localhost:PORTNUMBER/test" in the mapping file
        /*
            Path path = Paths.get(mappingPath);
            Charset charset = StandardCharsets.UTF_8;
            String content = new String(Files.readAllBytes(path), charset);
            content = content.replaceAll("PORTNUMBER", Integer.toString(configBuilder.getPort()));
            Files.write(path, content.getBytes(charset));
         */


        return database;
    }

    @Test
    public void evaluate_0001a_RDBs() throws Exception {
        String resourcePath = "./test-cases/RMLTC0001a-MySQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0001a-MySQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0001a-MySQL/target_output.ttl";
        DB db = startDB(resourcePath, mappingPath);
        doMapping(mappingPath, outputPath);
        db.stop();
    }

    @Test
    public void evaluate_0001b_RDBs() throws Exception {
        String resourcePath = "./test-cases/RMLTC0001b-MySQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0001b-MySQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0001b-MySQL/target_output.ttl";
        DB db = startDB(resourcePath, mappingPath);
        doMapping(mappingPath, outputPath);
        db.stop();
    }

}
