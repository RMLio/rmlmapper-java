package be.ugent.rml;

import be.ugent.rml.functions.JoinConditionFunction;
import be.ugent.rml.term.Term;
import be.ugent.rml.termgenerator.TermGenerator;

import java.util.ArrayList;
import java.util.List;

public class PredicateObjectGraphGenerator {


    private final TermGenerator predicateGenerator;
    private final TermGenerator objectGenerator;
    private final TermGenerator graphGenerator;
    private final List<JoinConditionFunction> joinConditions;
    private Term parentTriplesMap;

    public PredicateObjectGraphGenerator(TermGenerator predicateGenerator, TermGenerator objectGenerator, TermGenerator graphGenerator) {
        this.graphGenerator = graphGenerator;
        this.predicateGenerator = predicateGenerator;
        this.joinConditions = new ArrayList<JoinConditionFunction>();
        this.objectGenerator = objectGenerator;
    }

    public TermGenerator getPredicateGenerator() {
        return predicateGenerator;
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

    public TermGenerator getObjectGenerator() {
        return objectGenerator;
    }

    public TermGenerator getGraphGenerator() {
        return graphGenerator;
    }
}
