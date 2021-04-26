package be.ugent.rml.functions;

import be.ugent.rml.NAMESPACES;
import be.ugent.rml.functions.lib.IDLabFunctions;
import be.ugent.rml.store.RDF4JStore;
import be.ugent.rml.term.NamedNode;
import be.ugent.rml.term.Term;
import be.ugent.rml.Utils;
import be.ugent.rml.functions.lib.UtilFunctions;
import be.ugent.rml.store.QuadStore;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FunctionLoader {

    private static final Logger logger = LoggerFactory.getLogger(FunctionLoader.class);

    private final QuadStore functionDescriptionTriples;

    // updated dynamically
    /**
     * Cache for loaded classes
     */
    private Map<String, Class> classMap;
    /**
     * Cache for library paths
     */
    private Map<String, String> libraryMap;
    /**
     * Cache for loaded functions
     */
    private Map<Term, FunctionModel> loadedMethods;

    public FunctionLoader() throws Exception {
        this(null, null);
    }

    public FunctionLoader(QuadStore functionDescriptionTriples) throws Exception {
        this(functionDescriptionTriples, null);
    }

    public FunctionLoader(QuadStore functionDescriptionTriples, Map<String, Class> libraryMap) throws Exception {
        if (functionDescriptionTriples == null) {
            functionDescriptionTriples = new RDF4JStore();
            functionDescriptionTriples.read(Utils.getInputStreamFromFile(Utils.getFile("functions_idlab.ttl")), null, RDFFormat.TURTLE);
            functionDescriptionTriples.read(Utils.getInputStreamFromFile(Utils.getFile("functions_grel.ttl")), null, RDFFormat.TURTLE);
            functionDescriptionTriples.read(Utils.getInputStreamFromFile(Utils.getFile("grel_java_mapping.ttl")), null, RDFFormat.TURTLE);
        }

        this.functionDescriptionTriples = functionDescriptionTriples;

        this.libraryMap = new HashMap<>();

        if (libraryMap == null) {
            this.classMap = new HashMap<>();
            this.classMap.put("IDLabFunctions", IDLabFunctions.class);
            this.classMap.put("io.fno.grel.ArrayFunctions", io.fno.grel.ArrayFunctions.class);
            this.classMap.put("io.fno.grel.BooleanFunctions", io.fno.grel.BooleanFunctions.class);
            this.classMap.put("io.fno.grel.ControlsFunctions", io.fno.grel.ControlsFunctions.class);
            this.classMap.put("io.fno.grel.StringFunctions", io.fno.grel.StringFunctions.class);
            this.libraryMap.put("IDLabFunctions", "__local");
            this.libraryMap.put("io.fno.grel.ArrayFunctions", "__local");
            this.libraryMap.put("io.fno.grel.BooleanFunctions", "__local");
            this.libraryMap.put("io.fno.grel.ControlsFunctions", "__local");
            this.libraryMap.put("io.fno.grel.StringFunctions", "__local");
        } else {
            this.classMap = libraryMap;
            for (String key : libraryMap.keySet()) {
                this.libraryMap.put(key, "__local");
            }
        }

        this.classMap.put("UtilFunctions", UtilFunctions.class);
        this.libraryMap.put("UtilFunctions", "__local");

        this.loadedMethods = new HashMap<>();
    }

    public FunctionModel getFunction(Term iri) throws IOException {
        if (!this.loadedMethods.containsKey(iri)) {
            try {
                findMethodOldWay(iri);
                logger.warn("Found a function using the old `lib:` way, this is deprecated");
            } catch (IOException e) {
                findMethodNewWay(iri);
            }
        }

        return this.loadedMethods.get(iri);
    }

    public String getLibraryPath(String className) {
        return this.libraryMap.get(className);
    }

    private void findMethodOldWay(Term iri) throws IOException {
        List<Term> libraries = Utils.getObjectsFromQuads(this.functionDescriptionTriples.getQuads(iri, new NamedNode(NAMESPACES.LIB + "providedBy"), null));

        if (libraries.size() > 0) {
            List<Term> pathNames = Utils.getObjectsFromQuads(this.functionDescriptionTriples.getQuads(libraries.get(0), new NamedNode(NAMESPACES.LIB + "localLibrary"), null));
            List<Term> classes = Utils.getObjectsFromQuads(this.functionDescriptionTriples.getQuads(libraries.get(0), new NamedNode(NAMESPACES.LIB + "class"), null));

            if (pathNames.size() > 0 && classes.size() > 0) {
                String pathName = pathNames.get(0).getValue();
                String className = classes.get(0).getValue();
                Class cls;
                if (this.classMap.containsKey(className)) {
                    cls = this.classMap.get(className);
                } else {
                    File functionFile = Utils.getFile(pathName);
                    cls = FunctionUtils.functionRequire(functionFile, className);
                    this.classMap.put(className, cls);
                    this.libraryMap.put(className, functionFile.getCanonicalPath());
                }

                List<Term> parameters = new ArrayList<>();
                List<Term> expectList = Utils.getObjectsFromQuads(FunctionUtils.getQuadsByFunctionPrefix(this.functionDescriptionTriples, iri, "expects", null));

                if (expectList.size() > 0) {
                    parameters = Utils.getList(this.functionDescriptionTriples, expectList.get(0));
                }

                Class<?>[] orderedParameters = FunctionUtils.parseFunctionParameters(this.functionDescriptionTriples, parameters);
                List<Term> methods = Utils.getObjectsFromQuads(this.functionDescriptionTriples.getQuads(libraries.get(0), new NamedNode(NAMESPACES.LIB + "method"), null));

                List<Term> outputs = Utils.getList(this.functionDescriptionTriples, Utils.getObjectsFromQuads(FunctionUtils.getQuadsByFunctionPrefix(this.functionDescriptionTriples, iri, "returns", null)).get(0));
                List<Term> fnParameterUris = FunctionUtils.getFunctionParameterUris(this.functionDescriptionTriples, parameters);
                List<Term> fnOutputUris = FunctionUtils.getFunctionParameterUris(this.functionDescriptionTriples, outputs);

                if (methods.size() > 0) {
                    Method fn = null;
                    try {
                        fn = cls.getMethod(methods.get(0).getValue(), orderedParameters);
                    } catch (NoSuchMethodException e) {
                        throw new IOException("Declared method " + methods.get(0) + " does not exist for class " + classes.get(0) + ".");
                    }

                    FunctionModel fnm = new FunctionModel(iri, fn, fnParameterUris, fnOutputUris);

                    this.loadedMethods.put(iri, fnm);
                }
            } else {
                throw new IOException("No library or class was found for the function with IRI " + iri + " in the function descriptions.");
            }
        } else {
            throw new IOException("No library or class was found for the function with IRI " + iri + " in the function descriptions.");
        }
    }

    private void findMethodNewWay(Term iri) throws IOException {
        List<Term> mappings = Utils.getSubjectsFromQuads(FunctionUtils.getQuadsByFunctionPrefix(this.functionDescriptionTriples, null, "function", iri));

        if (mappings.size() == 0) {
            throw new IOException("No mapping was found for the function with IRI " + iri + " in the function descriptions.");
        }

        List<Term> libraries = Utils.getObjectsFromQuads(FunctionUtils.getQuadsByFunctionPrefix(this.functionDescriptionTriples, mappings.get(0), "implementation", null));

        if (libraries.size() == 0) {
            throw new IOException("No library was found for the mapping with IRI " + mappings.get(0) + " in the function descriptions.");
        }
        List<Term> pathNames = Utils.getObjectsFromQuads(this.functionDescriptionTriples.getQuads(libraries.get(0), new NamedNode(NAMESPACES.DOAP + "download-page"), null));
        List<Term> classes = Utils.getObjectsFromQuads(this.functionDescriptionTriples.getQuads(libraries.get(0), new NamedNode(NAMESPACES.FNOI + "class-name"), null));

        if (pathNames.size() == 0 || classes.size() == 0) {
            throw new IOException("No path or class found for the library with IRI " + libraries.get(0) + " in the function descriptions.");
        }

        String pathName = pathNames.get(0).getValue();
        String className = classes.get(0).getValue();
        Class cls;

        if (this.classMap.containsKey(className)) {
            cls = this.classMap.get(className);
        } else {
            File functionFile = Utils.getFile(pathName);
            cls = FunctionUtils.functionRequire(functionFile, className);
            this.classMap.put(className, cls);
            this.libraryMap.put(className, functionFile.getCanonicalPath());
        }

        List<Term> parameters = new ArrayList<>();
        List<Term> expectList = Utils.getObjectsFromQuads(FunctionUtils.getQuadsByFunctionPrefix(this.functionDescriptionTriples, iri, "expects", null));

        if (expectList.size() > 0) {
            parameters = Utils.getList(this.functionDescriptionTriples, expectList.get(0));
        }


        List<Term> outputs = Utils.getList(this.functionDescriptionTriples, Utils.getObjectsFromQuads(FunctionUtils.getQuadsByFunctionPrefix(this.functionDescriptionTriples, iri, "returns", null)).get(0));

        List<Term> methodMappings = Utils.getObjectsFromQuads(FunctionUtils.getQuadsByFunctionPrefix(this.functionDescriptionTriples, mappings.get(0), "methodMapping", null));
        if (methodMappings.size() == 0) {
            throw new IOException("No methodmapping found for the mapping with IRI " + mappings.get(0) + " in the function descriptions.");
        }

        List<Term> methods = Utils.getObjectsFromQuads(this.functionDescriptionTriples.getQuads(methodMappings.get(0), new NamedNode(NAMESPACES.FNOM + "method-name"), null));
        if (methods.size() == 0) {
            throw new IOException("No method found for the mapping with IRI " + mappings.get(0) + " in the function descriptions.");
        }

        Class<?>[] orderedParameters = FunctionUtils.parseFunctionParameters(this.functionDescriptionTriples, parameters);
        Method fn = null;
        try {
            fn = cls.getDeclaredMethod(methods.get(0).getValue(), orderedParameters);
        } catch (NoSuchMethodException e) {
            throw new IOException("Declared method " + methods.get(0) + " does not exist for class " + classes.get(0) + ".");
        }

        List<Term> fnParameterUris = FunctionUtils.getFunctionParameterUris(this.functionDescriptionTriples, parameters);
        List<Term> fnOutputUris = FunctionUtils.getFunctionParameterUris(this.functionDescriptionTriples, outputs);

        FunctionModel fnm = new FunctionModel(iri, fn, fnParameterUris, fnOutputUris);

        this.loadedMethods.put(iri, fnm);
    }
}
