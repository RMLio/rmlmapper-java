package be.ugent.rml;

import be.ugent.rml.access.DatabaseType;
import org.apache.commons.io.IOUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.Arrays;
import java.util.Scanner;

@RunWith(Parameterized.class)
public class Mapper_OracleDB_Test extends DBTestCore {

    protected static String CONNECTIONSTRING = "jdbc:oracle:thin:rmlmapper_test/test@//193.190.127.195:1521/XE";

    @Parameterized.Parameter(0)
    public String testCaseName;

    @Parameterized.Parameter(1)
    public Class<? extends Exception> expectedException;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Parameterized.Parameters(name = "{index}: OracleDB_{0}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][]{
                // scenarios:
//                {"RMLTC0000", null},
                {"RMLTC0001a", null},
//                {"RMLTC0001b", null},
//                {"RMLTC0002a", null},
//                {"RMLTC0002b", null},
//                {"RMLTC0002c", Error.class},
//                {"RMLTC0002d", null},
//                {"RMLTC0002e", Error.class},
//                {"RMLTC0002f", null},
//                {"RMLTC0002g", Error.class},
//                {"RMLTC0002h", Error.class},
                // TODO see issue #130
//                {"RMLTC0002i", Error.class},
//                {"RMLTC0002j", null},
                // TODO see issue #130
//                {"RMLTC0003a", Error.class},
//                {"RMLTC0003b", null},
//                {"RMLTC0003c", null},
//                {"RMLTC0004a", null},
//                {"RMLTC0004b", Error.class},
//                {"RMLTC0005a", null},
//                {"RMLTC0005b", null},
//                {"RMLTC0006a", null},
//                {"RMLTC0007a", null},
//                {"RMLTC0007b", null},
//                {"RMLTC0007c", null},
//                {"RMLTC0007d", null},
//                {"RMLTC0007e", null},
//                {"RMLTC0007f", null},
//                {"RMLTC0007g", null},
//                {"RMLTC0007h", Error.class},
//                {"RMLTC0008a", null},
//                {"RMLTC0008b", null},
//                {"RMLTC0008c", null},
//                {"RMLTC0009a", null},
//                {"RMLTC0009b", null},
//                {"RMLTC0009c", null},
//                {"RMLTC0009d", null},
//                {"RMLTC0010a", null},
//                {"RMLTC0010b", null},
//                {"RMLTC0010c", null},
//                {"RMLTC0011a", null},
//                {"RMLTC0011b", null},
//                {"RMLTC0012a", null},
//                {"RMLTC0012b", null},
//                {"RMLTC0012c", Error.class},
//                {"RMLTC0012d", Error.class},
//                {"RMLTC0012e", null},
//                {"RMLTC0013a", null},
//                {"RMLTC0014d", null},
//                {"RMLTC0015a", null},
//                {"RMLTC0015b", Error.class},
//                {"RMLTC0016a", null},
//                {"RMLTC0016b", null},
//                {"RMLTC0016c", null},
//                {"RMLTC0016d", null},
//                {"RMLTC0016e", null},
//                {"RMLTC0018a", null},
//                {"RMLTC0019a", null},
//                {"RMLTC0019b", null},
//                {"RMLTC0020a", null},
//                {"RMLTC0020b", null},
        });
    }

    @Test
    public void doMapping() throws Exception {
        mappingTest(testCaseName, expectedException);
    }

    private void mappingTest(String testCaseName, Class expectedException) throws Exception {
        String resourcePath = "test-cases/" + testCaseName + "-OracleDB/resource.sql";
        String mappingPath = "./test-cases/" + testCaseName + "-OracleDB/mapping.ttl";
        String outputPath = "test-cases/" + testCaseName + "-OracleDB/output.nq";

        // Create a temporary copy of the mapping file and replace source details
        String tempMappingPath = CreateTempMappingFileAndReplaceDSN(mappingPath, CONNECTIONSTRING);

        File resourceFile = Utils.getFile(resourcePath, null);
        InputStream resourceStream = new FileInputStream(resourceFile);
        //String resourceStr = IOUtils.toString(resourceStream, StandardCharsets.UTF_8);

        //System.out.println(resourceStr);

        Connection connection = DriverManager.getConnection(CONNECTIONSTRING);

        String dropTablesQuery = "select table_name from user_tables";
        Statement selectTablesStmt = connection.createStatement();
        ResultSet rs = selectTablesStmt.executeQuery(dropTablesQuery);

        while (rs.next()) {
            System.out.println(rs.getString("TABLE_NAME"));
            Statement dropTableStmt = connection.createStatement();
            dropTableStmt.executeQuery("drop table " + rs.getString("TABLE_NAME"));
        }

        //Statement stmt = connection.createStatement();
        //stmt.executeQuery(resourceStr);

        // Load data in database
        importSQL(connection, resourceStream);

        // mapping
        if (expectedException == null) {
            doMapping(tempMappingPath, outputPath);
        } else {
            doMappingExpectError(tempMappingPath);
        }

        deleteTempMappingFile(tempMappingPath);
    }

    private void importSQL(Connection conn, InputStream in) throws SQLException {
        Scanner s = new Scanner(in);
        s.useDelimiter("(;\n?)");

        try (Statement st = conn.createStatement()) {
            while (s.hasNext()) {
                String line = s.next();
                if (line.startsWith("/*!") && line.endsWith("*/")) {
                    int i = line.indexOf(' ');
                    line = line.substring(i + 1, line.length() - " */".length());
                }

                if (line.trim().length() > 0) {
                    System.out.println(line);
                    st.execute(line);
                }
            }
        }
    }
}
