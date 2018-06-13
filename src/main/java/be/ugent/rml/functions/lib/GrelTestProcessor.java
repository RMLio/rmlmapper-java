package be.ugent.rml.functions.lib;

import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;

public class GrelTestProcessor {

    public static String toUppercase(String test) {
        return test.toUpperCase();
    }

    public static String escape(String value, String mode) {
        switch (mode) {
            case "html":
                value = escapeHtml(value);
                break;
        }

        return value;
    }

    public static String random() {
        return "random string here";
    }

    public static String toUpperCaseURL(String test) {
        String upperTest = test.toUpperCase();
        if (!upperTest.startsWith("http://")) {
            upperTest = "http://" + upperTest;
        }
        return upperTest;
    }

    public static String getNull() {
        return null;
    }

    public static String generateA() {
        return "A_by_function";
    }
}
