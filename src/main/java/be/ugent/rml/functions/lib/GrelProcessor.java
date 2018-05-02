package be.ugent.rml.functions.lib;

import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;

public class GrelProcessor {

    public static String toUpperCase(String test) {
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
}
