package be.ugent.rml;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;


public abstract class DBTestCore extends TestCore {

    protected static HashSet<String> tempFiles = new HashSet<>();

    /*protected static String replaceDSNInMappingFile(String path, String connectionString) {
        try {
            // Read mapping file
            String mapping = new String(Files.readAllBytes(Paths.get(Utils.getFile(path, null).getAbsolutePath())), StandardCharsets.UTF_8);

            // Replace "PORT" in mapping file by new port
            mapping = mapping.replace("CONNECTIONDSN", connectionString);

            // Write to temp mapping file

            String fileName = Integer.toString(Math.abs(path.hashCode())) + "tempMapping.ttl";
            Path file = Paths.get(fileName);
            Files.write(file, Arrays.asList(mapping.split("\n")));

            String absolutePath = Paths.get(Utils.getFile(fileName, null).getAbsolutePath()).toString();
            tempFiles.add(absolutePath);

            return absolutePath;

        } catch (IOException ex) {
            throw new Error(ex.getMessage());
        }
    }*/

    protected static String createTempMappingFile(String path) {
        try {
            // Read mapping file
            String mapping = new String(Files.readAllBytes(Paths.get(Utils.getFile(path, null).getAbsolutePath())), StandardCharsets.UTF_8);

            // Write to temp mapping file
            return writeMappingFile(mapping, path);

        } catch (IOException ex) {
            throw new Error(ex.getMessage());
        }
    }

    protected static String CreateTempMappingFileAndReplaceDSN(String path, String connectionString) {
        try {
            // Read mapping file
            String mapping = new String(Files.readAllBytes(Paths.get(Utils.getFile(path, null).getAbsolutePath())), StandardCharsets.UTF_8);

            // Replace "CONNECTIONDSN" in mapping file by new port
            mapping = mapping.replace("CONNECTIONDSN", connectionString);

            // Write to temp mapping file
            return writeMappingFile(mapping, path);

        } catch (IOException ex) {
            throw new Error(ex.getMessage());
        }
    }

    protected static void deleteTempMappingFile(String absolutePath) {
        File file = new File(absolutePath);

        if (file.delete()) {
            tempFiles.remove(absolutePath);
        }
    }

    private static String readMappingFile(String path) {
        try {
            return new String(Files.readAllBytes(Paths.get(Utils.getFile(path, null).getAbsolutePath())), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new Error(ex.getMessage());
        }
    }

    private static String writeMappingFile(String mapping, String path) {
        try {
            String fileName = Integer.toString(Math.abs(mapping.hashCode())) + "tempMapping.ttl";
            Path file = Paths.get(fileName);
            Files.write(file, Arrays.asList(mapping.split("\n")));

            String absolutePath = Paths.get(Utils.getFile(fileName, null).getAbsolutePath()).toString();
            tempFiles.add(absolutePath);

            return absolutePath;
        } catch (IOException ex) {
            throw new Error(ex.getMessage());
        }
    }

}
