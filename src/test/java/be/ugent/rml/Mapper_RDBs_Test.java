package be.ugent.rml;

;
import org.junit.Test;

import ch.vorburger.mariadb4j.DB;


public class Mapper_RDBs_Test extends TestCore {

    public DB startDB(String resourcePath) throws Exception {
        DB database = DB.newEmbeddedDB(3305);
        database.start();
        database.source(resourcePath);
        return database;
    }

    @Test
    public void evaluate_0001a_RDBs() throws Exception {
        DB db = startDB("./test-cases/RMLTC0001a-MySQL/resource.sql");
        doMapping("./test-cases/RMLTC0001a-MySQL/mapping.ttl", "test-cases/RMLTC0001a-MySQL/output.ttl");
        db.stop();
    }

    @Test
    public void evaluate_0001b_RDBs() throws Exception {
        DB db = startDB("./test-cases/RMLTC0001b-MySQL/resource.sql");
        doMapping("test-cases/RMLTC0001b-MySQL/mapping.ttl", "test-cases/RMLTC0001b-MySQL/output.ttl");
        db.stop();
    }

}
