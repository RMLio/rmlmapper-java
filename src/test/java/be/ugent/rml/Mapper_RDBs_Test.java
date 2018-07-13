package be.ugent.rml;

;
import ch.vorburger.exec.ManagedProcessException;
import ch.vorburger.mariadb4j.DBConfigurationBuilder;
import org.junit.After;
import org.junit.Test;
import org.junit.Test;

import ch.vorburger.mariadb4j.DB;


// IMPORTANT FOR TESTING:
//  USE DYNAMIC PORT NUMBER AS DESCRIBED BELOW OR USE STATIC PORT NUMBERS IN MAPPING FILE's JDBC DSN
public class Mapper_RDBs_Test extends TestCore {

    private static final int PORTNUMBER = 50898;

    private DB currentDB;

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

    @After
    public void stopDB() throws ManagedProcessException {
        if (currentDB != null) {
            currentDB.stop();
        }
    } 

    @Test
    public void evaluate_0000_RDBs() throws Exception {
        String resourcePath = "./test-cases/RMLTC0000-MySQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0000-MySQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0000-MySQL/output.ttl";
        currentDB = startDB(resourcePath, mappingPath);
        doMapping(mappingPath, outputPath);
        
    }


    @Test
    public void evaluate_0001a_RDBs() throws Exception {
        String resourcePath = "./test-cases/RMLTC0001a-MySQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0001a-MySQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0001a-MySQL/output.ttl";
        currentDB = startDB(resourcePath, mappingPath);
        doMapping(mappingPath, outputPath);
        
    }

    @Test
    public void evaluate_0001b_RDBs() throws Exception {
        String resourcePath = "./test-cases/RMLTC0001b-MySQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0001b-MySQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0001b-MySQL/output.ttl";
        currentDB = startDB(resourcePath, mappingPath);
        doMapping(mappingPath, outputPath);
        
    }

    @Test
    public void evaluate_0002a_RDBs() throws Exception {
        String resourcePath = "./test-cases/RMLTC0002a-MySQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0002a-MySQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0002a-MySQL/output.ttl";
        currentDB = startDB(resourcePath, mappingPath);
        doMapping(mappingPath, outputPath);
        
    }

    @Test
    public void evaluate_0002b_RDBs() throws Exception {
        String resourcePath = "./test-cases/RMLTC0002b-MySQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0002b-MySQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0002b-MySQL/output.ttl";
        currentDB = startDB(resourcePath, mappingPath);
        doMapping(mappingPath, outputPath);
        
    }

    @Test(expected = Error.class)
    public void evaluate_0002c_RDBs() throws Exception {
        String resourcePath = "./test-cases/RMLTC0002c-MySQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0002c-MySQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0002c-MySQL/output.ttl";
        currentDB = startDB(resourcePath, mappingPath);
        doMapping(mappingPath, outputPath);
        
    }

    @Test(expected = Error.class)
    public void evaluate_0002e_RDBs() throws Exception {
        String resourcePath = "./test-cases/RMLTC0002e-MySQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0002e-MySQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0002e-MySQL/output.ttl";
        currentDB = startDB(resourcePath, mappingPath);
        doMapping(mappingPath, outputPath);
        
    }

    @Test
    public void evaluate_0002g_RDBs() throws Exception {
        String resourcePath = "./test-cases/RMLTC0002g-MySQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0002g-MySQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0002g-MySQL/output.ttl";
        currentDB = startDB(resourcePath, mappingPath);
        doMapping(mappingPath, outputPath);
        
    }

    @Test
    public void evaluate_0002h_RDBs() throws Exception {
        String resourcePath = "./test-cases/RMLTC0002h-MySQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0002h-MySQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0002h-MySQL/output.ttl";
        currentDB = startDB(resourcePath, mappingPath);
        doMapping(mappingPath, outputPath);
        
    }

    @Test(expected = Error.class)
    public void evaluate_0002i_RDBs() throws Exception {
        String resourcePath = "./test-cases/RMLTC0002i-MySQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0002i-MySQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0002i-MySQL/output.ttl";
        currentDB = startDB(resourcePath, mappingPath);
        doMapping(mappingPath, outputPath);
        
    }

    @Test
    public void evaluate_0002j_RDBs() throws Exception {
        String resourcePath = "./test-cases/RMLTC0002j-MySQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0002j-MySQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0002j-MySQL/output.ttl";
        currentDB = startDB(resourcePath, mappingPath);
        doMapping(mappingPath, outputPath);
        
    }

    @Test(expected = Error.class)
    public void evaluate_0003a_RDBs() throws Exception {
        String resourcePath = "./test-cases/RMLTC0003a-MySQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0003a-MySQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0003a-MySQL/output.ttl";
        currentDB = startDB(resourcePath, mappingPath);
        doMapping(mappingPath, outputPath);
        
    }

    @Test
    public void evaluate_0003b_RDBs() throws Exception {
        String resourcePath = "./test-cases/RMLTC0003b-MySQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0003b-MySQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0003b-MySQL/output.ttl";
        currentDB = startDB(resourcePath, mappingPath);
        doMapping(mappingPath, outputPath);
        
    }

    @Test
    public void evaluate_0003c_RDBs() throws Exception {
        String resourcePath = "./test-cases/RMLTC0003c-MySQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0003c-MySQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0003c-MySQL/output.ttl";
        currentDB = startDB(resourcePath, mappingPath);
        doMapping(mappingPath, outputPath);
        
    }

    @Test
    public void evaluate_0004a_RDBs() throws Exception {
        String resourcePath = "./test-cases/RMLTC0004a-MySQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0004a-MySQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0004a-MySQL/output.ttl";
        currentDB = startDB(resourcePath, mappingPath);
        doMapping(mappingPath, outputPath);
        
    }

    @Test
    public void evaluate_0004b_RDBs() throws Exception {
        String resourcePath = "./test-cases/RMLTC0004b-MySQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0004b-MySQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0004b-MySQL/output.ttl";
        currentDB = startDB(resourcePath, mappingPath);
        doMapping(mappingPath, outputPath);
        
    }

    @Test
    public void evaluate_0006a_RDBs() throws Exception {
        String resourcePath = "./test-cases/RMLTC0006a-MySQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0006a-MySQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0006a-MySQL/output.nq";
        currentDB = startDB(resourcePath, mappingPath);
        doMapping(mappingPath, outputPath);
        
    }

    @Test
    public void evaluate_0007a_RDBs() throws Exception {
        String resourcePath = "./test-cases/RMLTC0007a-MySQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0007a-MySQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0007a-MySQL/output.ttl";
        currentDB = startDB(resourcePath, mappingPath);
        doMapping(mappingPath, outputPath);
        
    }

    @Test
    public void evaluate_0007b_RDBs() throws Exception {
        String resourcePath = "./test-cases/RMLTC0007b-MySQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0007b-MySQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0007b-MySQL/output.nq";
        currentDB = startDB(resourcePath, mappingPath);
        doMapping(mappingPath, outputPath);
        
    }

    @Test
    public void evaluate_0007c_RDBs() throws Exception {
        String resourcePath = "./test-cases/RMLTC0007c-MySQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0007c-MySQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0007c-MySQL/output.ttl";
        currentDB = startDB(resourcePath, mappingPath);
        doMapping(mappingPath, outputPath);
        
    }

    @Test
    public void evaluate_0007d_RDBs() throws Exception {
        String resourcePath = "./test-cases/RMLTC0007d-MySQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0007d-MySQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0007d-MySQL/output.ttl";
        currentDB = startDB(resourcePath, mappingPath);
        doMapping(mappingPath, outputPath);
        
    }

    @Test
    public void evaluate_0007e_RDBs() throws Exception {
        String resourcePath = "./test-cases/RMLTC0007e-MySQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0007e-MySQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0007e-MySQL/output.nq";
        currentDB = startDB(resourcePath, mappingPath);
        doMapping(mappingPath, outputPath);
        
    }

    @Test
    public void evaluate_0007f_RDBs() throws Exception {
        String resourcePath = "./test-cases/RMLTC0007f-MySQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0007f-MySQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0007f-MySQL/output.nq";
        currentDB = startDB(resourcePath, mappingPath);
        doMapping(mappingPath, outputPath);
        
    }

    @Test
    public void evaluate_0007g_RDBs() throws Exception {
        String resourcePath = "./test-cases/RMLTC0007g-MySQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0007g-MySQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0007g-MySQL/output.ttl";
        currentDB = startDB(resourcePath, mappingPath);
        doMapping(mappingPath, outputPath);
        
    }

    @Test
    public void evaluate_0007h_RDBs() throws Exception {
        String resourcePath = "./test-cases/RMLTC0007h-MySQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0007h-MySQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0007h-MySQL/output.nq";
        currentDB = startDB(resourcePath, mappingPath);
        doMapping(mappingPath, outputPath);
        
    }

    @Test
    public void evaluate_0008a_RDBs() throws Exception {
        String resourcePath = "./test-cases/RMLTC0008a-MySQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0008a-MySQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0008a-MySQL/output.nq";
        currentDB = startDB(resourcePath, mappingPath);
        doMapping(mappingPath, outputPath);
        
    }

    @Test
    public void evaluate_0008b_RDBs() throws Exception {
        String resourcePath = "./test-cases/RMLTC0008b-MySQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0008b-MySQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0008b-MySQL/output.ttl";
        currentDB = startDB(resourcePath, mappingPath);
        doMapping(mappingPath, outputPath);
        
    }

    @Test
    public void evaluate_0008c_RDBs() throws Exception {
        String resourcePath = "./test-cases/RMLTC0008c-MySQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0008c-MySQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0008c-MySQL/output.ttl";
        currentDB = startDB(resourcePath, mappingPath);
        doMapping(mappingPath, outputPath);
        
    }

    @Test
    public void evaluate_0009a_RDBs() throws Exception {
        String resourcePath = "./test-cases/RMLTC0009a-MySQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0009a-MySQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0009a-MySQL/output.ttl";
        currentDB = startDB(resourcePath, mappingPath);
        doMapping(mappingPath, outputPath);
        
    }

    @Test
    public void evaluate_0010a_RDBs() throws Exception {
        String resourcePath = "./test-cases/RMLTC0010a-MySQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0010a-MySQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0010a-MySQL/output.ttl";
        currentDB = startDB(resourcePath, mappingPath);
        doMapping(mappingPath, outputPath);
        
    }

    @Test
    public void evaluate_0010b_RDBs() throws Exception {
        String resourcePath = "./test-cases/RMLTC0010b-MySQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0010b-MySQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0010b-MySQL/output.ttl";
        currentDB = startDB(resourcePath, mappingPath);
        doMapping(mappingPath, outputPath);
        
    }

    @Test
    public void evaluate_0010c_RDBs() throws Exception {
        String resourcePath = "./test-cases/RMLTC0010c-MySQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0010c-MySQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0010c-MySQL/output.ttl";
        currentDB = startDB(resourcePath, mappingPath);
        doMapping(mappingPath, outputPath);
        
    }

    @Test
    public void evaluate_0011b_RDBs() throws Exception {
        String resourcePath = "./test-cases/RMLTC0011b-MySQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0011b-MySQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0011b-MySQL/output.ttl";
        currentDB = startDB(resourcePath, mappingPath);
        doMapping(mappingPath, outputPath);
        
    }

    @Test
    public void evaluate_0012a_RDBs() throws Exception {
        String resourcePath = "./test-cases/RMLTC0012a-MySQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0012a-MySQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0012a-MySQL/output.ttl";
        currentDB = startDB(resourcePath, mappingPath);
        doMapping(mappingPath, outputPath);
        
    }

    @Test
    public void evaluate_0012b_RDBs() throws Exception {
        String resourcePath = "./test-cases/RMLTC0012b-MySQL/resource.sql";
        String mappingPath = "./test-cases/RMLTC0012b-MySQL/mapping.ttl";
        String outputPath = "test-cases/RMLTC0012b-MySQL/output.ttl";
        currentDB = startDB(resourcePath, mappingPath);
        doMapping(mappingPath, outputPath);
        
    }
}
