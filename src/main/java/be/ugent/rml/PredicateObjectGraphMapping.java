package be.ugent.rml;

import be.ugent.rml.functions.MultipleRecordsFunctionExecutor;
import be.ugent.rml.term.Term;

import java.util.ArrayList;
import java.util.List;

public class PredicateObjectGraphMapping {


    private final MappingInfo predicateMappingInfo;
    private final MappingInfo objectMappingInfo;
    private final MappingInfo graphMappingInfo;
    private final MappingInfo languageMappingInfo;
    private final List<MultipleRecordsFunctionExecutor> joinConditions;
    private Term parentTriplesMap;

    public PredicateObjectGraphMapping(MappingInfo predicateMappingInfo, MappingInfo objectMappingInfo, MappingInfo graphMappingInfo, MappingInfo languageMappingInfo) {
        this.predicateMappingInfo = predicateMappingInfo;
        this.graphMappingInfo = graphMappingInfo;
        this.joinConditions = new ArrayList<MultipleRecordsFunctionExecutor>();
        this.objectMappingInfo = objectMappingInfo;
        this.languageMappingInfo = languageMappingInfo;

        // Language Maps are tightly connected to the object, merge the target lists
        if(this.languageMappingInfo != null) {
            this.objectMappingInfo.addTargets(this.languageMappingInfo.getTargets());
        }
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

    public MappingInfo getPredicateMappingInfo() {
        return predicateMappingInfo;
    }

    public MappingInfo getObjectMappingInfo() {
        return objectMappingInfo;
    }

    public MappingInfo getGraphMappingInfo() {
        return graphMappingInfo;
    }

    public MappingInfo getLanguageMappingInfo() {
        return this.languageMappingInfo;
    }
}