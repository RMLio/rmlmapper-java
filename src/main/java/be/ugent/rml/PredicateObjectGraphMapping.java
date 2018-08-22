package be.ugent.rml;

import be.ugent.rml.functions.JoinConditionFunction;
import be.ugent.rml.term.Term;
import be.ugent.rml.termgenerator.TermGenerator;

import java.util.ArrayList;
import java.util.List;

public class PredicateObjectGraphMapping {


    private final Mapping.MappingInfo predicateMappingInfo;
    private final Mapping.MappingInfo objectMappingInfo;
    private final Mapping.MappingInfo graphMappingInfo;
    private final List<JoinConditionFunction> joinConditions;
    private Term parentTriplesMap;

    public PredicateObjectGraphMapping(Mapping.MappingInfo predicateMappingInfo, Mapping.MappingInfo objectMappingInfo, Mapping.MappingInfo graphMappingInfo) {
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

    public Mapping.MappingInfo getPredicateMappingInfo() {
        return predicateMappingInfo;
    }

    public Mapping.MappingInfo getObjectMappingInfo() {
        return objectMappingInfo;
    }

    public Mapping.MappingInfo getGraphMappingInfo() {
        return graphMappingInfo;
    }
}
