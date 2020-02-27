package be.ugent.rml.functions;

import be.ugent.rml.NAMESPACES;
import be.ugent.rml.store.Quad;
import be.ugent.rml.term.NamedNode;
import be.ugent.rml.term.Term;
import be.ugent.rml.Utils;
import be.ugent.rml.store.QuadStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FunctionUtils {

    private static final Logger logger = LoggerFactory.getLogger(FunctionUtils.class);

    public static Class functionRequire(File file, String className) throws IOException {
        String path = file.getPath();
        if (path.endsWith(".jar")) {
            return FunctionUtils.getClass(file, className, "application/java-archive");
        } else if (path.endsWith(".java")) {
            return FunctionUtils.getClass(file, className, "text/x-java-source");
        }

        throw new IOException("Not a valid path for a JAVA implementation: " + path);
    }

    public static List<Term> getFunctionParameterUris(QuadStore store, List<Term> parameterResources) {
        List<Term> parameterPredicates = new ArrayList<>();

        for (Term subject : parameterResources) {
            parameterPredicates.add(Utils.getObjectsFromQuads(getQuadsByFunctionPrefix(store, subject, "predicate", null)).get(0));
        }

        return parameterPredicates;
    }

    public static Class<?>[] parseFunctionParameters(QuadStore store, List<Term> parameterResources) {
        Class<?>[] args = new Class<?>[parameterResources.size()];

        for (int i = 0; i < parameterResources.size(); i++) {
            Term subject = parameterResources.get(i);
            Term type = Utils.getObjectsFromQuads(getQuadsByFunctionPrefix(store, subject, "type", null)).get(0);

            try {
                args[i] = FunctionUtils.getParamType(type);
            } catch (Exception e) {
                args[i] = String.class;
            }
        }
        return args;
    }

    public static void functionObjectToList(Object o, List<String> result) {
        if (o != null) {
            if (o instanceof String) {
                result.add((String) o);
            } else if (o instanceof List) {
                ((List) o).forEach(item -> {
                    functionObjectToList(item, result);
                });
            } else if (o instanceof Boolean) {
                result.add(o.toString());
            }
        }
    }

    private static Class getParamType(Term type) {
        String typeStr = type.getValue();

        switch (typeStr) {
            case "http://www.w3.org/2001/XMLSchema#string":
                return String.class;
            case "http://www.w3.org/2001/XMLSchema#integer":
                return Integer.class;
            case "http://www.w3.org/2001/XMLSchema#decimal":
            case "http://www.w3.org/2001/XMLSchema#double":
                return Double.class;
            case "http://www.w3.org/1999/02/22-rdf-syntax-ns#List":
                return List.class;
            default:
                throw new Error("Couldn't derive type from " + type);
        }
    }

    private static Class getClass(File sourceFile, String className, String mime) throws IOException {
        logger.info("Found class on path " + sourceFile.getCanonicalPath());

        switch (mime) {
            case "text/x-java-source":
                return FunctionUtils.getClassFromJAVA(sourceFile, className);
            case "application/java-archive":
                return FunctionUtils.getClassFromJAR(sourceFile, className);
        }

        return null;
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

    /**
     * Retrieve triples of a store based on a predicate, taking into account deprecated FnO prefixed predicates
     * @param store The triple store to retrieve the triples from
     * @param s the subject
     * @param functionTerm the unprefixed function term
     * @param o the object
     * @return the quads that are conform to the triple pattern fragment
     */
    static List<Quad> getQuadsByFunctionPrefix(QuadStore store, Term s, String functionTerm, Term o) {
        List<String> prefices = Arrays.asList(NAMESPACES.FNO_S, NAMESPACES.FNO, NAMESPACES.FNO_OLD);
        return getQuadsByPrefix(store, s, functionTerm, o, prefices);
    }

    /**
     * Retrieve triples of a store based on a predicate, taking into account multiple prefixes
     * @param store The triple store to retrieve the triples from
     * @param s the subject
     * @param pString the unprefixed predicate term
     * @param o the object
     * @param prefices the list of prefices on which to look for, in order of 'correctness' (all prefixes except for the first one are assumed deprecated)
     * @return the quads that are conform to the triple pattern fragment
     */
    private static List<Quad> getQuadsByPrefix(QuadStore store, Term s, String pString, Term o, List<String> prefices) {
        String preferredPrefix = prefices.get(0);
        Term realTerm;
        List<Quad> quads = new ArrayList<>();
        for (int i = 0; i < prefices.size(); i++) {
            realTerm = new NamedNode(prefices.get(i) + pString);
            quads = store.getQuads(s, realTerm, o);
            if (quads.size() > 0) {
                if (i != 0) {
                    logger.warn(prefices.get(i) + "is a deprecated prefix, please use " + preferredPrefix);
                }
                return quads;
            }
        }
        return quads;
    }
}
