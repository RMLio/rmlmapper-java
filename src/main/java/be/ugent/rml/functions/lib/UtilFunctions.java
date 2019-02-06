package be.ugent.rml.functions.lib;

public class UtilFunctions {

    public static boolean equal(String str1, String str2) {
        if (str1 == null) {
            return false;
        } else {
            return str1.equals(str2);
        }
    }

    public static boolean notEqual(String str1, String str2) {
        return !equal(str1, str2);
    }
}
