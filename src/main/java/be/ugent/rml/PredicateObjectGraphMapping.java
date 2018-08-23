package be.ugent.rml;

import be.ugent.rml.functions.JoinConditionFunction;
import be.ugent.rml.term.Term;
import be.ugent.rml.termgenerator.TermGenerator;

import java.util.ArrayList;
import java.util.List;

public class PredicateObjectGraphMapping {


    private final MappingInfo predicateMappingInfo;
    private final MappingInfo objectMappingInfo;
    private final MappingInfo graphMappingInfo;
    private final List<JoinConditionFunction> joinConditions;
    private Term parentTriplesMap;

    public PredicateObjectGraphMapping(MappingInfo predicateMappingInfo, MappingInfo objectMappingInfo, MappingInfo graphMappingInfo) {
        this.predicateMappingInfo = predicateMappingInfo;
        this.graphMappingInfo = graphMappingInfo;
        this.joinConditions = new ArrayList<JoinConditionFunction>();
        this.objectMappingInfo = objectMappingInfo;
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
