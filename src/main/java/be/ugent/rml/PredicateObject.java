package be.ugent.rml;

import be.ugent.rml.functions.Function;
import be.ugent.rml.functions.JoinConditionFunction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PredicateObject extends TripleElement {


    private final List<Template> predicates;
    private final List<JoinConditionFunction> joinConditions;
    private final String language;
    private final String datatype;
    private String parentTriplesMap;

    public PredicateObject(List<Template> predicates, List<Template> graphs, String termType, Function function, String language, String datatype) {
        super(graphs, termType, function);
        this.language = language;
        this.datatype = datatype;
        this.predicates = predicates;
        this.joinConditions = new ArrayList<JoinConditionFunction>();
    }

    public PredicateObject(List<Template> predicates, List<Template> graphs, String termType, Function function) {
        this(predicates, graphs, termType, function, null, null);
    }

    public String getLanguage() {
        return this.language;
    }

    public String getDataType() {
        if (this.language == null) {
            return this.datatype;
        }
        return null;
    }

    public List<Template> getPredicates() {
        return predicates;
    }

    public String getParentTriplesMap() {
        return parentTriplesMap;
    }

    public List<JoinConditionFunction> getJoinConditions() {
        return joinConditions;
    }

    public void setParentTriplesMap(String parentTriplesMap) {
        this.parentTriplesMap = parentTriplesMap;
    }

    public void addJoinCondition(JoinConditionFunction condition) {
        joinConditions.add(condition);
    }
}
