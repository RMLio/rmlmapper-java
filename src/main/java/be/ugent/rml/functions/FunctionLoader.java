package be.ugent.rml.functions;

import be.ugent.rml.Utils;
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
    private Map<String, FunctionModel> loadedMethods;

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
        this.loadedMethods = new HashMap<>();
    }

    public FunctionModel getFunction(String iri) throws IOException {
        if (!this.loadedMethods.containsKey(iri)) {
            List<String> libraries = Utils.getObjectsFromQuads(this.functionDescriptionTriples.getQuads(iri, libraryNamespace + "providedBy", null));

            if (libraries.size() > 0) {
                List<String> pathNames = Utils.getObjectsFromQuads(this.functionDescriptionTriples.getQuads(libraries.get(0), libraryNamespace + "localLibrary", null));
                List<String> classes = Utils.getObjectsFromQuads(this.functionDescriptionTriples.getQuads(libraries.get(0), libraryNamespace + "class", null));

                if (pathNames.size() > 0 && classes.size() > 0) {
                    String pathName = Utils.getLiteral(pathNames.get(0));
                    String className = Utils.getLiteral(classes.get(0));
                    Class cls;
                    if (this.classMap.containsKey(className)) {
                        cls = this.classMap.get(className);
                    } else {
                        File functionFile = Utils.getFile(pathName, this.basePath);
                        cls = FunctionUtils.functionRequire(functionFile, className);
                        this.classMap.put(className, cls);
                        this.libraryMap.put(className, functionFile.getCanonicalPath());
                    }

                    List<String> parameters = new ArrayList<>();
                    List<String> expectList = Utils.getObjectsFromQuads(this.functionDescriptionTriples.getQuads(iri, "http://semweb.datasciencelab.be/ns/function#expects", null));
                    if (expectList.size() > 0) {
                        parameters = Utils.getList(this.functionDescriptionTriples, expectList.get(0));
                    }
                    Class<?>[] orderedParameters = FunctionUtils.parseFunctionParameters(this.functionDescriptionTriples, parameters);
                    List<String> methods = Utils.getObjectsFromQuads(this.functionDescriptionTriples.getQuads(libraries.get(0), libraryNamespace + "method", null));

                    List<String> outputs = Utils.getList(this.functionDescriptionTriples, Utils.getObjectsFromQuads(this.functionDescriptionTriples.getQuads(iri, "http://semweb.datasciencelab.be/ns/function#returns", null)).get(0));
                    List<String> fnParameterUris = FunctionUtils.getFunctionParameterUris(this.functionDescriptionTriples, parameters);
                    List<String> fnOutputUris = FunctionUtils.getFunctionParameterUris(this.functionDescriptionTriples, outputs);

                    if (methods.size() > 0) {
                        Method fn = null;
                        try {
                            fn = cls.getDeclaredMethod(Utils.getLiteral(methods.get(0)), orderedParameters);
                        } catch (NoSuchMethodException e) {
                            throw new IOException("Declared method " + methods.get(0) + "does not exist for class " + classes.get(0) + ".");
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
