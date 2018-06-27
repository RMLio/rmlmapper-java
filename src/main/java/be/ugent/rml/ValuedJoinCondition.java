package be.ugent.rml;

import java.util.List;

public class ValuedJoinCondition {

    private Template path;
    private List<String> values;

    public ValuedJoinCondition(Template path, List<String> values) {
        this.path = path;
        this.values = values;
    }

    public Template getPath() {
        return path;
    }

    public List<String> getValues() {
        return values;
    }
}
