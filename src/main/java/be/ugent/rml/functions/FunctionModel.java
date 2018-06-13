package be.ugent.rml.functions;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Function Model
 *
 * @author bjdmeest
 */
public class FunctionModel {

    private final List<String> parameters; // parameters urls
    private final List<String> outputs; // output types
    private String URI;
    private Method method;

    public FunctionModel(String URI, Method m, List<String> parameters, List<String> outputs) {
        this.URI = URI;
        this.method = m;
        this.parameters = parameters;
        this.outputs = outputs;
    }

    public List<String> execute(Map<String, Object> args) {
        Object[] parameters = this.getParameters(args);
        try {
            Object object = this.method.invoke(null, parameters);
            //            ArrayList<Value> result = this.toValue(object, this.getDataType(args));
            List<String> result = new ArrayList<>();
            if (object != null) {
                result.add(object.toString());
            }
            return result;

        } catch (IllegalAccessException | InvocationTargetException e) {
            // Nothing to do?
            e.printStackTrace(); // maybe this? :p
        }
        return new ArrayList<>();
    }

    public String getURI() {
        return URI;
    }

    private ArrayList<Value> toValue(Object result, IRI type) {
        SimpleValueFactory vf = SimpleValueFactory.getInstance();
        ArrayList<Value> values = new ArrayList<>();
        if (!(result instanceof Collection<?>)) {
            ArrayList<Object> arr = new ArrayList<>();
            arr.add(result);
            result = arr;
        }
        ArrayList<Object> arr = new ArrayList<>();
        for (Object res : (List) result) {
            if (res != null) {
                arr.add(res);
            }
        }
        result = arr;
        switch (type.toString()) {
            case "http://www.w3.org/2001/XMLSchema#string":
                for (Object res : (List) result) {
                    values.add(vf.createLiteral((String) res));
                }
                break;
            case "http://www.w3.org/2001/XMLSchema#boolean":
                for (Object res : (List) result) {
                    values.add(vf.createLiteral((Boolean) res));
                }
                break;
            case "http://www.w3.org/2001/XMLSchema#anyURI":
                for (Object res : (List) result) {
                    values.add(vf.createIRI((String) res));
                }
                break;
            default:
                for (Object res : (List) result) {
                    values.add(vf.createLiteral(res.toString(), type));
                }
        }
        return values;
    }

    private IRI getDataType(Map<String, Object> args) {
        SimpleValueFactory vf = SimpleValueFactory.getInstance();
        String type = null;
        if (this.outputs.size() > 0) {
            if (this.outputs.get(0).startsWith("xsd:")) {
                type = this.outputs.get(0).replace("xsd:", "http://www.w3.org/2001/XMLSchema#");
            }
            if (this.outputs.get(0).startsWith("owl:")) {
                type = this.outputs.get(0).replace("owl:", "http://www.w3.org/2002/07/owl#");
            }
        }
        if ((type == null) && args.containsKey("http://dbpedia.org/function/unitParameter")) {
            type = "http://dbpedia.org/datatype/" + args.get("http://dbpedia.org/function/unitParameter");
        }
        if ((type == null) && args.containsKey("http://dbpedia.org/function/dataTypeParameter")) {
            if (args.get("http://dbpedia.org/function/dataTypeParameter").toString().equals("owl:Thing")) {
                type = "http://www.w3.org/2001/XMLSchema#anyURI";
            }
        }
        if ((type == null) && args.containsKey("http://dbpedia.org/function/equals/valueParameter")) {
            type = "http://www.w3.org/2001/XMLSchema#boolean";
        }
        if (type == null) {
            type = "http://www.w3.org/2001/XMLSchema#string";
        }

        return vf.createIRI(type);
    }

    private Object[] getParameters(Map<String, Object> parameters) {
        Object[] args = new Object[this.parameters.size()];
        Class[] paramTypes = this.method.getParameterTypes();
        for (int i = 0; i < this.parameters.size(); i++) {
            if (parameters.get(this.parameters.get(i)) != null) {
                args[i] = parseParameter(parameters.get(this.parameters.get(i)), paramTypes[i]);
            } else {
                args[i] = null;
            }
        }
        return args;
    }

    private Object parseParameter(Object parameter, Class type) {
        switch (type.getName()) {
            case "java.lang.String":
                return parameter.toString();
            case "int":
                return Integer.parseInt(parameter.toString());
            case "double":
                return Double.parseDouble(parameter.toString());
            default:
                throw new Error("Couldn't derive " + type.getName() + " from " + parameter);
        }
    }
}
