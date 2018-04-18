package be.ugent.rml;

import java.util.List;

public class ValuedJoinCondition {

    private String path;
    private List<String> values;

    public ValuedJoinCondition(String path, List<String> values) {
        this.path = path;
        this.values = values;
    }

    public String getPath() {
        return path;
    }

    public List<String> getValues() {
        return values;
    }
}
