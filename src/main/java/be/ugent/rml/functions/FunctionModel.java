package be.ugent.rml.functions;

import be.ugent.rml.term.Term;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.time.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        Type[] paramTypes = this.method.getGenericParameterTypes();

        for (int i = 0; i < this.parameters.size(); i++) {
            if (parameters.get(this.parameters.get(i).getValue()) != null) {
                args[i] = parseParameter(parameters.get(this.parameters.get(i).getValue()), paramTypes[i].getTypeName());
            } else {
                logger.debug("No argument was found for following parameter: " + this.parameters.get(i).getValue());
                args[i] = null;
            }
        }

        return args;
    }

    private Object parseParameter(Object parameter, String typeName) {
        String javaList = "java.util.List";
        if (typeName.contains(javaList)) {
            if (parameter instanceof String) {
                JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
                try {
                    //this should return a JSONArray, which implements java.util.List
                    return parser.parse((String) parameter);
                } catch (ParseException e) {
                    e.printStackTrace();
                    throw new Error("Could not get a List from " + parameter);
                }
            } else if(parameter instanceof List<?> && typeName.contains("<") && typeName.contains(">")) {
                // Must have <T> contents to be able to recursively parse
                String listElementType = typeName.substring(javaList.length() + 1, typeName.length() - 1);
                return ((List<?>) parameter).stream()
                        .map(o -> parseParameter(o, listElementType)) // recursively convert List elements
                        .collect(Collectors.toList());
            } else {
                return parameter;
            }
        }
        if (parameter instanceof List) {
            List<?> l = (List<?>) parameter;

            if (l.isEmpty()) {
                return null;
            } else {
                parameter = l.get(0);
            }
        }
        switch (typeName) {
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
            default:
                throw new Error("Couldn't derive " + typeName + " from " + parameter);
        }
    }
}
