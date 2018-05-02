package be.ugent.rml.functions;

import be.ugent.rml.Utils;
import be.ugent.rml.store.QuadStore;
import be.ugent.rml.store.RDF4JStore;
import com.google.common.io.Resources;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.ModelBuilder;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FunctionLoader {

    private Map<String, FunctionModel> loadedMethods;
    private Map<String, Class> classMap;
    private QuadStore functionDescriptionTriples;
    private String libraryNamespace = "http://example.com/library#";

    public FunctionLoader() {
        this.loadedMethods = new HashMap<>();
        this.classMap = new HashMap<>();
        URL url = Resources.getResource("functions.ttl");
        QuadStore store;
        try {
            File myFile = new File(url.toURI());
            store = Utils.readTurtle(myFile);
        } catch (Exception e) {
            ModelBuilder builder = new ModelBuilder();
            Model model = builder.build();
            store = new RDF4JStore(model);
        }
        this.functionDescriptionTriples = store;
    }

    public FunctionLoader(Map<String, Class> libraryMap) {
        this();
        this.classMap = libraryMap;
    }

    public FunctionLoader(RDF4JStore functionDescriptionTriples) {
        this.loadedMethods = new HashMap<>();
        this.classMap = new HashMap<>();
        this.functionDescriptionTriples = functionDescriptionTriples;
    }

    public FunctionLoader(RDF4JStore functionDescriptionTriples, Map<String, Class> libraryMap) {
        this.loadedMethods = new HashMap<>();
        this.classMap = libraryMap;
        this.functionDescriptionTriples = functionDescriptionTriples;
    }

    public FunctionModel getFunction(String iri) throws IOException {
        if (!this.loadedMethods.containsKey(iri)) {
            List<String> libraries = Utils.getObjectsFromQuads(this.functionDescriptionTriples.getQuads(iri, libraryNamespace + "providedBy", null));

            if (libraries.size() > 0) {
                List<String> pathNames = Utils.getObjectsFromQuads(this.functionDescriptionTriples.getQuads(libraries.get(0), libraryNamespace + "localLibrary", null));
                List<String> classes = Utils.getObjectsFromQuads(this.functionDescriptionTriples.getQuads(libraries.get(0), libraryNamespace + "class", null));

                if (pathNames.size() > 0 && classes.size() > 0) {
                    String pathName = Utils.getLiteral(pathNames.get(0));
                    Class cls;
                    if (this.classMap.containsKey(pathName)) {
                        cls = this.classMap.get(pathName);
                    } else {
                        cls = FunctionUtils.functionRequire(pathName, Utils.getLiteral(classes.get(0)));
                        this.classMap.put(pathName, cls);
                    }

                    List<String> parameters = Utils.getList(this.functionDescriptionTriples, Utils.getObjectsFromQuads(this.functionDescriptionTriples.getQuads(iri, "http://semweb.datasciencelab.be/ns/function#expects", null)).get(0));
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
}
