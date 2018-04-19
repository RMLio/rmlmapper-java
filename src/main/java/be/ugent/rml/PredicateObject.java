package be.ugent.rml;

import be.ugent.rml.functions.ApplyTemplateFunction;
import be.ugent.rml.functions.Function;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PredicateObject extends TripleElement {


    private final List<List<Element>> predicates;

    public PredicateObject(List<List<Element>> predicates, List<List<Element>> graphs, String termType, Function function, Map<String, List<List<Element>>> parameters) {
        super(graphs, termType, function, parameters);
        this.predicates = predicates;
    }

    public String getLanguage() {
        return null;
    }

    public String getDataType() {
        return null;
    }

    public List<List<Element>> getPredicates() {
        return predicates;
    }

    public String getParentTriplesMap() {
        return null;
    }

    public JoinCondition[] getJoinConditions() {
        return null;
    }
}
