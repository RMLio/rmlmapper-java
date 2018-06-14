package be.ugent.rml;

import be.ugent.rml.functions.Function;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PredicateObject extends TripleElement {


    private final List<List<Element>> predicates;
    private final List<JoinCondition> joinConditions;
    private final String language;
    private final String datatype;
    private String parentTriplesMap;

    public PredicateObject(List<List<Element>> predicates, List<List<Element>> graphs, String termType, Function function, Map<String, List<List<Element>>> parameters, String language, String datatype) {
        super(graphs, termType, function, parameters);
        this.language = language;
        this.datatype = datatype;
        this.predicates = predicates;
        this.joinConditions = new ArrayList<JoinCondition>();
    }

    public PredicateObject(List<List<Element>> predicates, List<List<Element>> graphs, String termType, Function function, Map<String, List<List<Element>>> parameters) {
        this(predicates, graphs, termType, function, parameters, null, null);
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

    public List<List<Element>> getPredicates() {
        return predicates;
    }

    public String getParentTriplesMap() {
        return parentTriplesMap;
    }

    public JoinCondition[] getJoinConditions() {
        return null;
    }

    public void setParentTriplesMap(String parentTriplesMap) {
        this.parentTriplesMap = parentTriplesMap;
    }

    public void addJoinCondition(JoinCondition condition) {
        joinConditions.add(condition);
    }
}
