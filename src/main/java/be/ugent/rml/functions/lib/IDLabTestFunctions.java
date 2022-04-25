package be.ugent.rml.functions.lib;

public class IDLabTestFunctions extends IDLabFunctions {

    public static String random() {
        return "random_string_here";
    }

    public static String getNull() {
        return null;
    }

    public static String generateA() {
        return "A_by_function";
    }

    public static String throwErrorIfMars(String string) throws Exception {
        if(string.equals("Mars\"")){
            throw new Exception();
        }
        return string;
    }
}
