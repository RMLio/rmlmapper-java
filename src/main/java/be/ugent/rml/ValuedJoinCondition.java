package be.ugent.rml;

import java.util.List;

public class ValuedJoinCondition {

    private List<Element> path;
    private List<String> values;

    public ValuedJoinCondition(List<Element> path, List<String> values) {
        this.path = path;
        this.values = values;
    }

    public List<Element> getPath() {
        return path;
    }

    public List<String> getValues() {
        return values;
    }
}
