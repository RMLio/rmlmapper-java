package be.ugent.rml.functions;

import be.ugent.rml.Utils;
import be.ugent.rml.store.QuadStore;
import com.google.common.io.Resources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

public class FunctionUtils {

    private static final Logger logger = LoggerFactory.getLogger(FunctionUtils.class);

    public static Class functionRequire(String path, String className) throws IOException {
        if (path.endsWith(".jar")) {
            return FunctionUtils.getClass(path, className, "application/java-archive");
        } else if (path.endsWith(".java")) {
            return FunctionUtils.getClass(path, className, "text/x-java-source");
        }

        throw new IOException("Not a valid path for a JAVA implementation: " + path);
    }

    public static List<String> getFunctionParameterUris(QuadStore store, List<String> parameterResources) {
        List<String> parameterPredicates = new ArrayList<>();
        for (String subject : parameterResources) {
            parameterPredicates.add(Utils.getObjectsFromQuads(store.getQuads(subject, "http://semweb.datasciencelab.be/ns/function#predicate", null)).get(0));
        }
        return parameterPredicates;
    }

    public static Class<?>[] parseFunctionParameters(QuadStore store, List<String> parameterResources) {
        Class<?>[] args = new Class<?>[parameterResources.size()];
        for (int i = 0; i < parameterResources.size(); i++) {
            String subject = parameterResources.get(i);
            String type = Utils.getObjectsFromQuads(store.getQuads(subject, "http://semweb.datasciencelab.be/ns/function#type", null)).get(0);
            try {
                args[i] = FunctionUtils.getParamType(type);
            } catch (Exception e) {
                args[i] = String.class;
            }
        }
        return args;
    }

    private static Class getParamType(String type) {
        switch (type) {
            case "http://www.w3.org/2001/XMLSchema#string":
                return String.class;
            case "http://www.w3.org/2001/XMLSchema#integer":
                return int.class;
            case "http://www.w3.org/2001/XMLSchema#decimal":
                return double.class;
            default:
                throw new Error("Couldn't derive type from " + type);
        }
    }

    private static Class getClass(String path, String className, String mime) throws IOException {
        File sourceFile = FunctionUtils.getFile(path);
        logger.info("Found class on path " + sourceFile.getPath());

        switch (mime) {
            case "text/x-java-source":
                return FunctionUtils.getClassFromJAVA(sourceFile, className);
            case "application/java-archive":
                return FunctionUtils.getClassFromJAR(sourceFile, className);
        }

        return null;
    }

    private static File getFile(String path) throws IOException {
        // Absolute path?
        File f = new File(path);
        if (f.isAbsolute()) {
            if (f.exists()) {
                return f;
            } else {
                throw new FileNotFoundException();
            }
        }

        // Resource path?
        try {
            URL url = Resources.getResource(path);
            f = new File(url.getFile());
            if (f.exists()) {
                return f;
            }
        } catch (IllegalArgumentException e) {
            // Too bad
        }

        String basePath;
        try {
            basePath = System.getProperty("user.dir");
        } catch (Exception e) {
            throw new FileNotFoundException();
        }

        // Relative from user dir?
        f = new File(basePath + "/" + path);
        if (f.exists()) {
            return f;
        }

        // Relative from parent of user dir?
        f = new File(basePath + "/../" + path);
        if (f.exists()) {
            return f;
        }


        throw new FileNotFoundException();
    }

    private static Class getClassFromJAVA(File sourceFile, String className) {
        Class<?> cls = null;

        // TODO let's not recompile every time
        // Compile source file.
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        int res = compiler.run(null, null, null, sourceFile.getPath());

        if (res != 0) {
            return null;
        }

        // Load and instantiate compiled class.
        URLClassLoader classLoader = null;
        try {
            classLoader = URLClassLoader.newInstance(new URL[]{(new File(sourceFile.getParent())).toURI().toURL()});
            cls = Class.forName(className, true, classLoader);
        } catch (MalformedURLException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return cls;
    }

    private static Class getClassFromJAR(File sourceFile, String className) {
        Class<?> cls = null;

        URLClassLoader child = null;
        try {
            child = URLClassLoader.newInstance(new URL[]{sourceFile.toURI().toURL()});
            cls = Class.forName(className, true, child);
        } catch (MalformedURLException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return cls;
    }
}
