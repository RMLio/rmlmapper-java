package be.ugent.rml;

import be.ugent.rml.extractor.Extractor;
import be.ugent.rml.extractor.ReferenceExtractor;
import be.ugent.rml.store.Quad;
import be.ugent.rml.store.QuadStore;
import be.ugent.rml.term.NamedNode;
import be.ugent.rml.term.Term;

import java.util.*;
import static be.ugent.rml.Utils.getObjectsFromQuads;

public class MappingOptimizer {

    private final QuadStore rmlStore;

    public MappingOptimizer(QuadStore rmlStore) {
        this.rmlStore = rmlStore;
    }

    public QuadStore optimizeMapping() throws Exception {
        renameSameLogicalSource();
        eliminateSelfJoins();
        return rmlStore;
    }

    private void renameSameLogicalSource() {
        List<Term> logicalSources = Utils.getObjectsFromQuads(rmlStore.getQuads(null,new NamedNode(NAMESPACES.RML2 + "logicalSource"),null));
        Map<Set<Term>, Term> logicalSourcesDict = new HashMap<>();
        for (Term logicalSource : logicalSources){
            // two logical Sources are considered to be identical when they have the same objects at the leaves of their subgraph
            Set<Term> allObjects = new HashSet<>();
            List<Term> objects = Utils.getObjectsFromQuads(rmlStore.getQuads(logicalSource, null, null));
            while (!objects.isEmpty()) {
                Term object = objects.remove(objects.size()-1);
                if (object.isBNode() || object.isIRI()) {
                    List<Term> newObjects = Utils.getObjectsFromQuads(rmlStore.getQuads(object, null, null));
                    if (!newObjects.isEmpty()) {
                        objects.addAll(newObjects);
                    } else {
                        //object is final, not subject of new quads
                        allObjects.add(object);
                    }
                } else {
                    //object is final, not subject of new quads
                    allObjects.add(object);
                }
            }
            Set<Term> finalObjectSet = Collections.unmodifiableSet(allObjects);
            if (!logicalSourcesDict.keySet().contains(finalObjectSet)) {
                logicalSourcesDict.put(finalObjectSet, logicalSource);
            } else {
                List<Term> triplesMaps = Utils.getSubjectsFromQuads(this.rmlStore.getQuads(null, new NamedNode(NAMESPACES.RML2 + "logicalSource"), logicalSource));
                for (Term triplesMap : triplesMaps) {
                    rmlStore.removeQuads(triplesMap, new NamedNode(NAMESPACES.RML2 + "logicalSource"), logicalSource);
                    rmlStore.addQuad(triplesMap, new NamedNode(NAMESPACES.RML2 + "logicalSource"), logicalSourcesDict.get(finalObjectSet));
                }
            }
        }
    }

    private void eliminateSelfJoins() {
        List<Quad> refObjectMapsQuads = rmlStore.getQuads(null, new NamedNode(NAMESPACES.RML2 + "parentTriplesMap"), null);
        for (Quad refObjectMapQuad : refObjectMapsQuads) {
            Term parentTriplesMap = refObjectMapQuad.getObject();
            Term childObjectMap = refObjectMapQuad.getSubject();
            Term parentLogicalSource = Utils.getObjectsFromQuads(rmlStore.getQuads(parentTriplesMap, new NamedNode(NAMESPACES.RML2 + "logicalSource"), null)).get(0);
            Term childPredicateObjectMap = Utils.getSubjectsFromQuads(rmlStore.getQuads(null, new NamedNode(NAMESPACES.RML2 + "objectMap"), childObjectMap)).get(0);
            Term childTriplesMap = Utils.getSubjectsFromQuads(rmlStore.getQuads(null, new NamedNode(NAMESPACES.RML2 + "predicateObjectMap"), childPredicateObjectMap)).get(0);
            Term childLogicalSource = Utils.getObjectsFromQuads(rmlStore.getQuads(childTriplesMap, new NamedNode(NAMESPACES.RML2 + "logicalSource"), null)).get(0);

            // check if the logical sources are the same
            if (childLogicalSource.equals(parentLogicalSource)) {

                List<Term> joinConditions = Utils.getObjectsFromQuads(rmlStore.getQuads(childObjectMap, new NamedNode(NAMESPACES.RML2 + "joinCondition"), null));

                List<Term> parentSubjectMaps = Utils.getObjectsFromQuads(rmlStore.getQuads(parentTriplesMap, new NamedNode(NAMESPACES.RML2 + "subjectMap"), null));
                 Term parentSubjectMap = null;
                if (!parentSubjectMaps.isEmpty()) {
                    parentSubjectMap = parentSubjectMaps.get(0);
                }

                boolean safeSelfJoinElimination = true;

                // if no join condition, we can safely eliminate the self-join
                // else we need more checks
                if (parentSubjectMap != null && !joinConditions.isEmpty()) {
                    // we can eliminate a self-join when all join conditions have equal references and all references for the parent subject or all reference for the related child triple come back in the join conditions
                    // 1. check if all join references are equal
                    List<String> joinReferences = new ArrayList<>();
                    for (Term joinCondition : joinConditions) {
                        String parent = getObjectsFromQuads(rmlStore.getQuads(joinCondition, new NamedNode(NAMESPACES.RML2 + "parent"), null)).get(0).getValue();
                        String child = getObjectsFromQuads(rmlStore.getQuads(joinCondition, new NamedNode(NAMESPACES.RML2 + "child"), null)).get(0).getValue();
                        if (child.equals(parent)) {
                            joinReferences.add(child);
                        } else {
                            safeSelfJoinElimination = false;
                        }
                    }
                    if (safeSelfJoinElimination) {
                        // 2. check if all references for the parent subject come back in the join conditions
                        boolean safeTerms = hasSafeReferences(parentSubjectMap, joinReferences);
                        if(!safeTerms) {
                            // if not all references for the parent subject come back in the join conditions,
                            // 3. check if all references for the related child terms come back in the join conditions
                            // 3.1 check child subject
                            List<Term> childSubjectMaps = Utils.getObjectsFromQuads(rmlStore.getQuads(parentTriplesMap, new NamedNode(NAMESPACES.RML2 + "subjectMap"), null));
                            if(!childSubjectMaps.isEmpty()) {
                                safeTerms = hasSafeReferences(childSubjectMaps.get(0), joinReferences);
                            } else {
                                safeTerms = true;
                            }
                            //3.2 check child predicate (only make sense if the child subject was safe, otherwise we cannot eliminate the-self join)
                            if (safeTerms) {
                                List<Term> childPredicateMaps = Utils.getObjectsFromQuads(rmlStore.getQuads(childPredicateObjectMap, new NamedNode(NAMESPACES.RML2 + "predicateMap"), null));
                                if(!childPredicateMaps.isEmpty()) {
                                    safeTerms = hasSafeReferences(childPredicateMaps.get(0), joinReferences);
                                }
                            }
                        }
                        // 4. if parent subject or all child terms are safe, the self join can be eliminated, else not
                        if (!safeTerms) {
                            safeSelfJoinElimination = false;
                        }
                    }
                }
                if (safeSelfJoinElimination) {
                    // now we rewrite the mapping file to eliminate the self-join
                    boolean termTypeAdded = false;
                    List<Quad> parentSubjectMapQuads = rmlStore.getQuads(parentSubjectMap, null, null);
                    for (Quad parentSubjectMapQuad : parentSubjectMapQuads) {
                        Term predicate = parentSubjectMapQuad.getPredicate();
                        if (predicate.equals(new NamedNode(NAMESPACES.FNML + "functionValue"))
                                || predicate.equals(new NamedNode(NAMESPACES.RML2 + "termType"))
                                || predicate.equals(new NamedNode(NAMESPACES.RML2 + "reference"))
                                || predicate.equals(new NamedNode(NAMESPACES.RML2 + "template"))
                                || predicate.equals(new NamedNode(NAMESPACES.RML2 + "constant"))) {
                            rmlStore.addQuad(childObjectMap, predicate, parentSubjectMapQuad.getObject());
                        }
                        if (predicate.equals(new NamedNode(NAMESPACES.RML2 + "termType"))) {
                            termTypeAdded = true;
                        }
                    }
                    rmlStore.removeQuads(childObjectMap, new NamedNode(NAMESPACES.RML2 + "parentTriplesMap"), parentTriplesMap);
                    if (!termTypeAdded) {
                        rmlStore.addQuad(childObjectMap, new NamedNode(NAMESPACES.RML2 + "termType"), new NamedNode(NAMESPACES.RML2 + "IRI"));
                    }
                }
            }
        }
    }

    private Set<String> getAllLinkedReferences(Term term){
        Set<String> references = new HashSet<>();
        List<Term> linkedSubjects = new ArrayList<>();
        linkedSubjects.add(term);
        while(!linkedSubjects.isEmpty()) {
            Term subject = linkedSubjects.get(0);
            List<Quad> linkedQuads = rmlStore.getQuads(subject, null, null);
            for (Quad linkedQuad : linkedQuads) {
                Term predicate = linkedQuad.getPredicate();
                if (predicate.equals(new NamedNode(NAMESPACES.RML2 + "reference"))) {
                    references.add(linkedQuad.getObject().getValue());
                } else if (predicate.equals(new NamedNode(NAMESPACES.RML2 + "template"))) {
                    String template = linkedQuad.getObject().getValue();
                    List<Extractor> extractors = Utils.parseTemplate(template, false);
                    for (Extractor extractor : extractors) {
                        if (extractor instanceof ReferenceExtractor) {
                            references.add(((ReferenceExtractor) extractor).getReference());
                        }
                    }
                } else {
                    Term object = linkedQuad.getObject();
                    if (object.isBNode() || object.isIRI()) {
                        linkedSubjects.add(object);
                    }
                }
            }
            linkedSubjects.remove(0);
        }
        return references;
    }

    private boolean hasSafeReferences(Term term, List<String> joinReferences){
        boolean isSafe = true;
        Set<String> termReferences = getAllLinkedReferences(term);
        for (String parentReference : termReferences){
            if (!joinReferences.contains(parentReference)){
                isSafe = false;
            }
        }
        return isSafe;
    }
}
