package be.ugent.rml;

import be.ugent.rml.functions.Function;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PredicateObject extends TripleElement {


    private final List<List<Element>> predicates;
    private final List<JoinCondition> joinConditions;

    public PredicateObject(List<List<Element>> predicates, List<List<Element>> graphs, String termType, Function function, Map<String, List<List<Element>>> parameters, String language, String datatype) {
        super(graphs, termType, function, parameters);
        this.predicates = predicates;
        this.joinConditions = new ArrayList<>();
    }

    public PredicateObject(List<List<Element>> predicates, List<List<Element>> graphs, String termType, Function function, Map<String, List<List<Element>>> parameters) {
        this(predicates, graphs, termType, function, parameters, null, null);
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

    public void setParentTriplesMap(String parentTriplesMap) {

    }

    public void addJoinCondition(JoinCondition condition) {
        joinConditions.add(condition);
    }
}
