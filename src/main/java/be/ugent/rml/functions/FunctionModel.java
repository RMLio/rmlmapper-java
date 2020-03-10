package be.ugent.rml.functions;

import be.ugent.rml.term.Term;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private final List<Term> parameters; // parameters urls
    private final List<Term> outputs; // output types
    private Term URI;
    private Method method;

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    public FunctionModel(Term URI, Method m, List<Term> parameters, List<Term> outputs) {
        this.URI = URI;
        this.method = m;
        this.parameters = parameters;
        this.outputs = outputs;
    }

    public Object execute(Map<String, Object> args) {
        Object[] parameters = this.getParameters(args);
        try {
            return this.method.invoke(null, parameters);
//            ArrayList<Value> result = this.toValue(object, this.getDataType(args));
        } catch (IllegalAccessException | InvocationTargetException e) {
            // Nothing to do?
            e.printStackTrace(); // maybe this? :p
        }

        return null;
    }

    public Term getURI() {
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
            if (this.outputs.get(0).getValue().startsWith("xsd:")) {
                type = this.outputs.get(0).getValue().replace("xsd:", "http://www.w3.org/2001/XMLSchema#");
            }
            if (this.outputs.get(0).getValue().startsWith("owl:")) {
                type = this.outputs.get(0).getValue().replace("owl:", "http://www.w3.org/2002/07/owl#");
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
            if (parameters.get(this.parameters.get(i).getValue()) != null) {
                args[i] = parseParameter(parameters.get(this.parameters.get(i).getValue()), paramTypes[i]);
            } else {
                logger.debug("No argument was found for following parameter: " + this.parameters.get(i).getValue());
                args[i] = null;
            }
        }

        return args;
    }

    private Object parseParameter(Object parameter, Class type) {
        if (type.getName().equals("java.util.List")) {
            if (parameter instanceof String) {
                JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
                try {
                    //this should return a JSONArray, which implements java.util.List
                    return parser.parse((String) parameter);
                } catch (ParseException e) {
                    e.printStackTrace();
                    throw new Error("Could not get a List from " + parameter);
                }
            } else {
                return parameter;
            }
        }
        if (parameter instanceof List) {
            List l = (List) parameter;

            if (l.isEmpty()) {
                return null;
            } else {
                parameter = l.get(0);
            }
        }
        switch (type.getName()) {
            case "java.lang.String":
                return parameter.toString();
            case "int":
            case "java.lang.Integer":
                return Integer.parseInt(parameter.toString());
            case "double":
            case "java.lang.Double":
                return Double.parseDouble(parameter.toString());
            case "java.util.List":
                if (parameter instanceof String) {
                    JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
                    try {
                        //this should return a JSONArray, which implements java.util.List
                        return parser.parse((String) parameter);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                } else {
                    return parameter;
                }

            default:
                throw new Error("Couldn't derive " + type.getName() + " from " + parameter);
        }
    }
}
