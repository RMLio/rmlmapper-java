package be.ugent.rml;

import be.ugent.rml.term.Term;
import be.ugent.rml.termgenerator.TermGenerator;

import java.util.List;

public class Mapping {

    private MappingInfo subjectMappingInfo;
    private List<PredicateObjectGraphMapping> predicateObjectGraphMappings;
    private List<MappingInfo> graphMappingInfos;

    public Mapping(MappingInfo subjectMappingInfo, List<PredicateObjectGraphMapping> predicateObjectGraphMappings, List<MappingInfo> graphMappingInfos) {
        this.subjectMappingInfo = subjectMappingInfo;
        this.predicateObjectGraphMappings = predicateObjectGraphMappings;
        this.graphMappingInfos = graphMappingInfos;
    }

    public MappingInfo getSubjectMappingInfo() {
        return subjectMappingInfo;
    }

    public List<PredicateObjectGraphMapping> getPredicateObjectGraphMappings() {
        return predicateObjectGraphMappings;
    }

    public List<MappingInfo> getGraphMappingInfos() {
        return graphMappingInfos;
    }
}
