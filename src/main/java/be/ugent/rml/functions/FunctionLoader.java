package be.ugent.rml.functions;

import be.ugent.rml.term.NamedNode;
import be.ugent.rml.term.Term;
import be.ugent.rml.Utils;
import be.ugent.rml.functions.lib.UtilFunctions;
import be.ugent.rml.store.QuadStore;
import be.ugent.rml.store.RDF4JStore;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.ModelBuilder;
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
    private static String libraryNamespace = "http://example.com/library#";
    private static String defaultFunctionsPath = "functions.ttl";

    private final File functionsFile;
    private final File basePath;
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

    public FunctionLoader() {
        this(null, null, null);
    }

    public FunctionLoader(File functionsFile) {
        this(functionsFile, null, null);
    }

    public FunctionLoader(File functionsFile, QuadStore functionDescriptionTriples, Map<String, Class> libraryMap) {
        if (functionsFile == null) {
            try {
                functionsFile = Utils.getFile(defaultFunctionsPath);
            } catch (IOException e) {
                logger.warn(e.getMessage(), e);
            }
            this.functionsFile = functionsFile;
            if (functionsFile == null) {
                this.basePath = null;
            } else {
                this.basePath = this.functionsFile.getParentFile();
            }
        } else {
            this.functionsFile = functionsFile;
            this.basePath = this.functionsFile.getParentFile();
        }

        if (functionDescriptionTriples == null) {
            if (functionsFile == null) {
                ModelBuilder builder = new ModelBuilder();
                Model model = builder.build();
                this.functionDescriptionTriples = new RDF4JStore(model);
            } else {
                this.functionDescriptionTriples = Utils.readTurtle(functionsFile);
            }
        } else {
            this.functionDescriptionTriples = functionDescriptionTriples;
        }

        this.libraryMap = new HashMap<>();

        if (libraryMap == null) {
            this.classMap = new HashMap<>();
        } else {
            this.classMap = libraryMap;
            for (String key : libraryMap.keySet()) {
                this.libraryMap.put(key, "__local");
            }
        }

        this.classMap.put("UtilFunctions", UtilFunctions.class);
        this.libraryMap.put("UtilFunctions.jar", "__local");

        this.loadedMethods = new HashMap<>();
    }

    public FunctionModel getFunction(Term iri) throws IOException {
        if (!this.loadedMethods.containsKey(iri.getValue())) {
            List<Term> libraries = Utils.getObjectsFromQuads(this.functionDescriptionTriples.getQuads(iri, new NamedNode(libraryNamespace + "providedBy"), null));

            if (libraries.size() > 0) {
                List<Term> pathNames = Utils.getObjectsFromQuads(this.functionDescriptionTriples.getQuads(libraries.get(0), new NamedNode(libraryNamespace + "localLibrary"), null));
                List<Term> classes = Utils.getObjectsFromQuads(this.functionDescriptionTriples.getQuads(libraries.get(0), new NamedNode(libraryNamespace + "class"), null));

                if (pathNames.size() > 0 && classes.size() > 0) {
                    String pathName = pathNames.get(0).getValue();
                    String className = classes.get(0).getValue();
                    Class cls;
                    if (this.classMap.containsKey(className)) {
                        cls = this.classMap.get(className);
                    } else {
                        File functionFile = Utils.getFile(pathName, this.basePath);
                        cls = FunctionUtils.functionRequire(functionFile, className);
                        this.classMap.put(className, cls);
                        this.libraryMap.put(className, functionFile.getCanonicalPath());
                    }

                    List<Term> parameters = new ArrayList<>();
                    List<Term> expectList = Utils.getObjectsFromQuads(this.functionDescriptionTriples.getQuads(iri, new NamedNode("http://semweb.datasciencelab.be/ns/function#expects"), null));

                    if (expectList.size() > 0) {
                        parameters = Utils.getList(this.functionDescriptionTriples, expectList.get(0));
                    }

                    Class<?>[] orderedParameters = FunctionUtils.parseFunctionParameters(this.functionDescriptionTriples, parameters);
                    List<Term> methods = Utils.getObjectsFromQuads(this.functionDescriptionTriples.getQuads(libraries.get(0), new NamedNode(libraryNamespace + "method"), null));

                    List<Term> outputs = Utils.getList(this.functionDescriptionTriples, Utils.getObjectsFromQuads(this.functionDescriptionTriples.getQuads(iri, new NamedNode("http://semweb.datasciencelab.be/ns/function#returns"), null)).get(0));
                    List<Term> fnParameterUris = FunctionUtils.getFunctionParameterUris(this.functionDescriptionTriples, parameters);
                    List<Term> fnOutputUris = FunctionUtils.getFunctionParameterUris(this.functionDescriptionTriples, outputs);

                    if (methods.size() > 0) {
                        Method fn = null;
                        try {
                            fn = cls.getDeclaredMethod(methods.get(0).getValue(), orderedParameters);
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

        return this.loadedMethods.get(iri);
    }

    public String getLibraryPath(String className) {
        return this.libraryMap.get(className);
    }
}
