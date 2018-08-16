package be.ugent.rml;

import be.ugent.rml.cli.Main;
import be.ugent.rml.store.QuadStore;
import be.ugent.rml.store.SimpleQuadStore;
import be.ugent.rml.store.TriplesQuads;
import be.ugent.rml.term.*;

import java.util.ArrayList;
import java.util.List;
import java.time.Instant;

/**
 * Unique class -- reusable outside of the mapper
 */
public class DatasetLevelMetadataGenerator {

    public static void createMetadata(String outputFile, QuadStore rmlStore, List<Term> triplesMaps, String startTimeStamp,
                                String stopTimeStamp, String mappingFile) {
        QuadStore result = new SimpleQuadStore();
        List<Term> logicalSources = new ArrayList();

        for(Term triplesMap: triplesMaps) {
            List<Term> logicalSourcesObjects = Utils.getObjectsFromQuads(rmlStore.getQuads(triplesMap,
                    new NamedNode(NAMESPACES.RML + "logicalSource"), null));

            if (logicalSourcesObjects.isEmpty()) {
                throw new Error("No Logical Source is found for " + triplesMap + ". Exactly one Logical Source is required per Triples Map.");
            }

            Term logicalSource = logicalSourcesObjects.get(0);

            if (Utils.isBlankNode(logicalSource.toString())) {
                List<Term> sourceObjects = Utils.getObjectsFromQuads(rmlStore.getQuads(logicalSource,
                        new NamedNode(NAMESPACES.RML + "source"), null));

                if (sourceObjects.isEmpty()) {
                    throw new Error("No Source is found for " + triplesMap + ". Exactly one Source is required per Logical Source.");
                }

                Term source = sourceObjects.get(0);
                Term sourceNode;

                // Literal -- encapsulate source in blank node
                if (Utils.isLiteral(source.toString())) {
                    sourceNode = new NamedNode(String.format("file:%s",sourceObjects.get(0).getValue()));
                } else {    // todo: what with blank nodes?
                    sourceNode = source;
                }
                logicalSources.add(sourceNode);
            } else {
                logicalSources.add(logicalSource);
            }
        }

        // Create the metadata and add to QuadStore

        Term rdfDataset = new NamedNode("#RDF_Dataset");
        Term rdfDatasetGeneration = new NamedNode("#RDFdataset_Generation");
        Term rmlProcessor = new NamedNode("#RMLProcessor");

        // <#RDF_Dataset>
        result.addTriple(rdfDataset, new NamedNode(NAMESPACES.RDF + "type"),
                new NamedNode(NAMESPACES.PROV + "Entity"));
        result.addTriple(rdfDataset, new NamedNode(NAMESPACES.RDF + "type"),
                new NamedNode(NAMESPACES.VOID + "Dataset"));
        result.addTriple(rdfDataset, new NamedNode(NAMESPACES.PROV + "generatedAtTime"),
                new Literal(Instant.now().toString(), new AbstractTerm(NAMESPACES.XSD + "dateTime")));
        result.addTriple(rdfDataset, new NamedNode(NAMESPACES.PROV + "wasGeneratedBy"),
                rdfDatasetGeneration);
        result.addTriple(rdfDataset, new NamedNode(NAMESPACES.PROV + "wasAssociatedWith"),
                rmlProcessor);

        // <#RMLProcessor>
        result.addTriple(rmlProcessor, new NamedNode(NAMESPACES.RDF + "type"),
                new NamedNode(NAMESPACES.PROV + "Agent"));
        result.addTriple(rmlProcessor, new NamedNode(NAMESPACES.PROV + "type"),
                new NamedNode(NAMESPACES.PROV + "SoftwareAgent"));


        // <#RDFdataset_Generation>
        result.addTriple(rdfDatasetGeneration, new NamedNode(NAMESPACES.RDF + "type"),
                new NamedNode(NAMESPACES.PROV + "Activity"));
        result.addTriple(rdfDatasetGeneration, new NamedNode(NAMESPACES.PROV + "generated"),
                rdfDataset);
        result.addTriple(rdfDatasetGeneration, new NamedNode(NAMESPACES.PROV + "startedAtTime"),
                new Literal(startTimeStamp, new AbstractTerm(NAMESPACES.XSD + "dateTime")));
        result.addTriple(rdfDatasetGeneration, new NamedNode(NAMESPACES.PROV + "endedAtTime"),
                new Literal(stopTimeStamp, new AbstractTerm(NAMESPACES.XSD + "dateTime")));
        result.addTriple(rdfDatasetGeneration, new NamedNode(NAMESPACES.PROV + "used"),
                new NamedNode(String.format("file:%s", mappingFile)));

        for (Term logicalSource: logicalSources) {
            result.addTriple(rdfDataset, new NamedNode(NAMESPACES.PROV + "wasDerivedFrom"),
                    logicalSource);
            result.addTriple(rdfDatasetGeneration, new NamedNode(NAMESPACES.PROV + "used"),
                    logicalSource);
        }


        TriplesQuads tq = Utils.getTriplesAndQuads(result.toSimpleSortedQuadStore().getQuads(null, null, null, null));

        Utils.writeOutput("triple", tq.getTriples(), "nt", outputFile);
    }
}
