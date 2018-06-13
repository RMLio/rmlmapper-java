package be.ugent.rml.functions.lib;

import java.util.UUID;

import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;

public class GrelProcessor {

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
        return UUID.randomUUID().toString();
    }

    public static String toUpperCaseURL(String test) {
        String upperTest = test.toUpperCase();
        if (!upperTest.startsWith("http://")) {
            upperTest = "http://" + upperTest;
        }
        return upperTest;
    }
}
