package be.ugent.rml;

import be.ugent.rml.functions.Function;
import be.ugent.rml.functions.JoinConditionFunction;

import java.util.ArrayList;
import java.util.List;

public class PredicateObjectGenerator extends TripleElement {


    private final List<Template> predicates;
    private final List<JoinConditionFunction> joinConditions;
    private final String language;
    private final Term datatype;
    private Term parentTriplesMap;

    public PredicateObjectGenerator(List<Template> predicates, List<Template> graphs, Term termType, Function function, String language, Term datatype) {
        super(graphs, termType, function);
        this.language = language;
        this.datatype = datatype;
        this.predicates = predicates;
        this.joinConditions = new ArrayList<JoinConditionFunction>();
    }

    public PredicateObjectGenerator(List<Template> predicates, List<Template> graphs, Term termType, Function function) {
        this(predicates, graphs, termType, function, null, null);
    }

    public String getLanguage() {
        return this.language;
    }

    public Term getDataType() {
        if (this.language == null) {
            return this.datatype;
        }
        return null;
    }

    public List<Template> getPredicates() {
        return predicates;
    }

    public Term getParentTriplesMap() {
        return parentTriplesMap;
    }

    public List<JoinConditionFunction> getJoinConditions() {
        return joinConditions;
    }

    public void setParentTriplesMap(Term parentTriplesMap) {
        this.parentTriplesMap = parentTriplesMap;
    }

    public void addJoinCondition(JoinConditionFunction condition) {
        joinConditions.add(condition);
    }
}
