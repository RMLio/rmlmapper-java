package be.ugent.rml;

import be.ugent.rml.functions.MultipleRecordsFunctionExecutor;
import be.ugent.rml.term.Term;

import java.util.ArrayList;
import java.util.List;

public class PredicateObjectGraphMapping {


    private final MappingInfo predicateMappingInfo;
    private final MappingInfo objectMappingInfo;
    private final MappingInfo graphMappingInfo;
    private final List<MultipleRecordsFunctionExecutor> joinConditions;
    private Term parentTriplesMap;
    private Term predicateObjectMap;

    public PredicateObjectGraphMapping(MappingInfo predicateMappingInfo, MappingInfo objectMappingInfo, MappingInfo graphMappingInfo, Term predicateObjectMap) {
        this.predicateMappingInfo = predicateMappingInfo;
        this.graphMappingInfo = graphMappingInfo;
        this.joinConditions = new ArrayList<MultipleRecordsFunctionExecutor>();
        this.objectMappingInfo = objectMappingInfo;
        this.predicateObjectMap = predicateObjectMap;
    }

    public Term getParentTriplesMap() {
        return parentTriplesMap;
    }

    public Term getPredicateObjectMap() { return predicateObjectMap; }

    public List<MultipleRecordsFunctionExecutor> getJoinConditions() {
        return joinConditions;
    }

    public void setParentTriplesMap(Term parentTriplesMap) {
        this.parentTriplesMap = parentTriplesMap;
    }

    public void addJoinCondition(MultipleRecordsFunctionExecutor condition) {
        joinConditions.add(condition);
    }

    public MappingInfo getPredicateMappingInfo() {
        return predicateMappingInfo;
    }

    public MappingInfo getObjectMappingInfo() {
        return objectMappingInfo;
    }

    public MappingInfo getGraphMappingInfo() {
        return graphMappingInfo;
    }
}
