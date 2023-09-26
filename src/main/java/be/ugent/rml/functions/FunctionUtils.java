package be.ugent.rml.functions;

import java.util.ArrayList;
import java.util.List;

public class FunctionUtils {

    /**
     * Generates strings from a function object. Possible lists/sets/bags/... in the object are unrolled recursively
     * and a string value is generated from each "simple" (i.e., not a list/set/bag/...) child object.
     *
     * @param o      Function object, can be iterable.
     * @param result A string list to which string values of objects are added
     */
    public static List<String> functionObjectToList(Object o) {
        final List<String> result = new ArrayList<>();
        if (o != null) {
            // if o has child objects, recursively call this function on each child
            if (o instanceof Iterable<?>) {
                ((Iterable<?>) o).forEach(item -> {
                    result.addAll(functionObjectToList(item));
                });
            // Some functions return a regular Array, not an Iterable, handle those as well.
            } else if (o instanceof Object[]) {
                for (Object item: (Object[])o)
                    result.addAll(functionObjectToList(item));
            }
            // if o has no children, call toString() to serialize it into a string
            else {
                // numeric and boolean types are trivially serialized correctly
                // times/dates objects in the java.time package use the relevant ISO-8601 standard,
                // which is also used by xsd types and thus RDF types
                result.add(o.toString());
            }
        }
        return result;
    }
}
