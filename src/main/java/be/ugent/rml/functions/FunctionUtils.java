package be.ugent.rml.functions;

import be.ugent.rml.NAMESPACES;
import be.ugent.rml.Utils;
import be.ugent.rml.store.Quad;
import be.ugent.rml.store.QuadStore;
import be.ugent.rml.term.NamedNode;
import be.ugent.rml.term.Term;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.time.*;
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

    /**
     * Returns and validates parameters
     * @param store
     * @param parameterResources
     * @return
     */
    public static List<Term> getFunctionParameterUris(QuadStore store, List<Term> parameterResources) {
        List<Term> parameterPredicates = new ArrayList<>();

        try {
            for (Term subject : parameterResources) {
                parameterPredicates.add(Utils.getObjectsFromQuads(getQuadsByFunctionPrefix(store, subject, "predicate", null)).get(0));
            }
        } catch (Exception e) {
            logger.error("Missing function parameters in {}", parameterResources);
        }

        return parameterPredicates;
    }

    public static Class<?>[] parseFunctionParameters(QuadStore store, List<Term> parameterResources)
            throws IOException {
        Class<?>[] args = new Class<?>[parameterResources.size()];

        for (int i = 0; i < parameterResources.size(); i++) {
            Term subject = parameterResources.get(i);
            List<Term> types = Utils.getObjectsFromQuads(getQuadsByFunctionPrefix(store, subject, "type", null));
            if (types.isEmpty()) {
                throw new IOException("Missing " + NAMESPACES.FNO_S + "type for " + subject + " in function descriptions.");
            }
            Term type = types.get(0);

            try {
                args[i] = FunctionUtils.getParamType(type);
            } catch (Exception e) {
                args[i] = String.class;
            }
        }
        return args;
    }

    /**
     * Generates strings from a function object. Possible lists/sets/bags/... in the object are unrolled recursively
     * and a string value is generated from each "simple" (i.e., not a list/set/bag/...) child object.
     *
     * @param o      Function object, can be iterable.
     * @param result A string list to which string values of objects are added
     */
    public static void functionObjectToList(Object o, List<String> result) {
        if (o != null) {
            // if o has child objects, recursively call this function on each child
            if (o instanceof Iterable<?>) {
                ((Iterable<?>) o).forEach(item -> {
                    functionObjectToList(item, result);
                });
            }
            // if o has no children, call toString() to serialize it into a string
            else {
                // numeric and boolean types are trivially serialized correctly
                // times/dates objects in the java.time package use the relevant ISO-8601 standard,
                // which is also used by xsd types and thus RDF types
                result.add(o.toString());
            }
        }
    }

    private static Class getParamType(Term type) {
        String typeStr = type.getValue();

        switch (typeStr) {
            // This is quite crude, based on https://www.w3.org/TR/xmlschema11-2/#built-in-datatypes
            case "http://www.w3.org/2001/XMLSchema#any":
                return Object.class;
            case "http://www.w3.org/2001/XMLSchema#string":
                return String.class;
            case "http://www.w3.org/2001/XMLSchema#unsignedLong":
            case "http://www.w3.org/2001/XMLSchema#long":
                return Long.class;
            case "http://www.w3.org/2001/XMLSchema#integer":
            case "http://www.w3.org/2001/XMLSchema#int":
            case "http://www.w3.org/2001/XMLSchema#short":
            case "http://www.w3.org/2001/XMLSchema#byte":
            case "http://www.w3.org/2001/XMLSchema#nonNegativeInteger":
            case "http://www.w3.org/2001/XMLSchema#positiveInteger":
            case "http://www.w3.org/2001/XMLSchema#unsignedInt":
            case "http://www.w3.org/2001/XMLSchema#unsignedShort":
            case "http://www.w3.org/2001/XMLSchema#unsignedByte":
            case "http://www.w3.org/2001/XMLSchema#nonPositiveInteger":
            case "http://www.w3.org/2001/XMLSchema#negativeInteger":
                return Integer.class;
            case "http://www.w3.org/2001/XMLSchema#boolean":
                return Boolean.class;
            case "http://www.w3.org/2001/XMLSchema#date":
                // "Local" just means "without a time zone"
                return LocalDate.class;
            case "http://www.w3.org/2001/XMLSchema#dateTime":
                // again "Local" means "without a time zone"
                // (An xsd:dateTime actually has an OPTIONAL time zone, so there is a small semantic difference
                // with java.time.LocalDateTime, this is a best effort.)
                return LocalDateTime.class;
            case "http://www.w3.org/2001/XMLSchema#dateTimeStamp":
                return ZonedDateTime.class;
            case "http://www.w3.org/2001/XMLSchema#dayTimeDuration":
            case "http://www.w3.org/2001/XMLSchema#yearMonthDuration":
                return Duration.class;
            case "http://www.w3.org/2001/XMLSchema#gDay":
                // TODO there is no java.time equivalent of xsd:day
                // (There is java.time.DayOfWeek, but xsd:day would corresponds to java.time.DayOfMonth .)
                throw new DateTimeException("There is no java.time equivalent of xsd:day. Crashing.");
            case "http://www.w3.org/2001/XMLSchema#gMonth":
                return Month.class;
            case "http://www.w3.org/2001/XMLSchema#gMonthDay":
                return MonthDay.class;
            case "http://www.w3.org/2001/XMLSchema#gYear":
                return Year.class;
            case "http://www.w3.org/2001/XMLSchema#gYearMonth":
                return YearMonth.class;
            case "http://www.w3.org/2001/XMLSchema#decimal":
            case "http://www.w3.org/2001/XMLSchema#double":
            case "http://www.w3.org/2001/XMLSchema#float":
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
