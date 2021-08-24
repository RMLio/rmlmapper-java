package be.ugent.rml.functions;

import be.ugent.rml.term.Term;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.*;
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
            case "java.lang.Object":
            case "java.lang.String":
                return parameter.toString();
            case "int":
            case "java.lang.Integer":
                return Integer.parseInt(parameter.toString());
            case "double":
            case "java.lang.Double":
                return Double.parseDouble(parameter.toString());
            case "long":
            case "java.lang.Long":
                return Long.parseLong(parameter.toString());
            case "java.lang.Boolean":
                return Boolean.parseBoolean(parameter.toString());
            case "java.time.LocalDate":
                return LocalDate.parse(parameter.toString());
            case "java.time.LocalDateTime":
                return LocalDateTime.parse(parameter.toString());
            case "java.time.ZonedDateTime":
                return ZonedDateTime.parse(parameter.toString());
            case "java.time.Duration":
                return Duration.parse(parameter.toString());
            case "java.time.Month":
                return Month.valueOf(parameter.toString());
            case "java.time.MonthDay":
                return MonthDay.parse(parameter.toString());
            case "java.time.Year":
                return Year.parse(parameter.toString());
            case "java.time.YearMonth":
                return YearMonth.parse(parameter.toString());
            // TODO my IDE says this case is unreachable (redundant with the check at the start of this function)
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
