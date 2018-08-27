package be.ugent.rml;

import be.ugent.rml.functions.JoinConditionFunctionExecutor;
import be.ugent.rml.functions.MultipleRecordsFunctionExecutor;
import be.ugent.rml.term.Term;
import be.ugent.rml.termgenerator.TermGenerator;

import java.util.ArrayList;
import java.util.List;

public class PredicateObjectGraphGenerator {


    private final TermGenerator predicateGenerator;
    private final TermGenerator objectGenerator;
    private final TermGenerator graphGenerator;
    private final List<MultipleRecordsFunctionExecutor> joinConditions;
    private Term parentTriplesMap;

    public PredicateObjectGraphGenerator(TermGenerator predicateGenerator, TermGenerator objectGenerator, TermGenerator graphGenerator) {
        this.graphGenerator = graphGenerator;
        this.predicateGenerator = predicateGenerator;
        this.joinConditions = new ArrayList<MultipleRecordsFunctionExecutor>();
        this.objectGenerator = objectGenerator;
    }

    public TermGenerator getPredicateGenerator() {
        return predicateGenerator;
    }

    public Term getParentTriplesMap() {
        return parentTriplesMap;
    }

    public List<MultipleRecordsFunctionExecutor> getJoinConditions() {
        return joinConditions;
    }

    public void setParentTriplesMap(Term parentTriplesMap) {
        this.parentTriplesMap = parentTriplesMap;
    }

    public void addJoinCondition(MultipleRecordsFunctionExecutor condition) {
        joinConditions.add(condition);
    }

    public TermGenerator getObjectGenerator() {
        return objectGenerator;
    }

    public TermGenerator getGraphGenerator() {
        return graphGenerator;
    }
}
