package be.ugent.rml.functions.lib;

import java.util.Arrays;
import java.util.List;

public class IDLabFunctions {

    public static boolean stringContainsOtherString(String str, String otherStr, String delimiter) {
        String[] split = str.split(delimiter);
        List<String> list = Arrays.asList(split);

        return list.contains(otherStr);
    }
}
