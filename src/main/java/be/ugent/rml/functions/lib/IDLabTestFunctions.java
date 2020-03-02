package be.ugent.rml.functions.lib;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IDLabTestFunctions extends IDLabFunctions {

    private static final Logger logger = LoggerFactory.getLogger(IDLabTestFunctions.class);

    public static String random() {
        return "random_string_here";
    }

    public static String getNull() {
        return null;
    }

    public static String generateA() {
        return "A_by_function";
    }
}
