package be.ugent.rml.metadata;

import be.ugent.rml.NAMESPACES;
import be.ugent.rml.store.QuadStore;
import be.ugent.rml.term.*;

import java.util.List;
import java.time.Instant;

/**
 * Unique class -- reusable outside of the mapper
 */
public class DatasetLevelMetadataGenerator {

    public static void createMetadata(Term rdfDataset, Term rdfDatasetGeneration, Term rmlMapper,
                                      QuadStore result, List<Term> logicalSources,
                                      String startTimestamp, String stopTimestamp, String[] mappingFiles) {
        // <#RDF_Dataset>
        result.addQuad(rdfDataset, new NamedNode(NAMESPACES.RDF + "type"),
                new NamedNode(NAMESPACES.PROV + "Entity"));
        result.addQuad(rdfDataset, new NamedNode(NAMESPACES.RDF + "type"),
                new NamedNode(NAMESPACES.VOID + "Dataset"));
        result.addQuad(rdfDataset, new NamedNode(NAMESPACES.PROV + "generatedAtTime"),
                new Literal(Instant.now().toString(), new NamedNode(NAMESPACES.XSD + "dateTime")));
        result.addQuad(rdfDataset, new NamedNode(NAMESPACES.PROV + "wasGeneratedBy"),
                rdfDatasetGeneration);
        result.addQuad(rdfDataset, new NamedNode(NAMESPACES.PROV + "wasAssociatedWith"),
                rmlMapper);

        // <#rmlMapper>
        result.addQuad(rmlMapper, new NamedNode(NAMESPACES.RDF + "type"),
                new NamedNode(NAMESPACES.PROV + "Agent"));
        result.addQuad(rmlMapper, new NamedNode(NAMESPACES.PROV + "type"),
                new NamedNode(NAMESPACES.PROV + "SoftwareAgent"));

        // <#RDFdataset_Generation>
        result.addQuad(rdfDatasetGeneration, new NamedNode(NAMESPACES.RDF + "type"),
                new NamedNode(NAMESPACES.PROV + "Activity"));
        result.addQuad(rdfDatasetGeneration, new NamedNode(NAMESPACES.PROV + "generated"),
                rdfDataset);
        result.addQuad(rdfDatasetGeneration, new NamedNode(NAMESPACES.PROV + "startedAtTime"),
                new Literal(startTimestamp, new NamedNode(NAMESPACES.XSD + "dateTime")));
        result.addQuad(rdfDatasetGeneration, new NamedNode(NAMESPACES.PROV + "endedAtTime"),
                new Literal(stopTimestamp, new NamedNode(NAMESPACES.XSD + "dateTime")));
        for (String mappingFile : mappingFiles) {
            result.addQuad(rdfDatasetGeneration, new NamedNode(NAMESPACES.PROV + "used"),
                new NamedNode(String.format("file://%s", mappingFile)));
        }
        for (Term logicalSource : logicalSources) {
            result.addQuad(rdfDataset, new NamedNode(NAMESPACES.PROV + "wasDerivedFrom"),
                    logicalSource);
            result.addQuad(rdfDatasetGeneration, new NamedNode(NAMESPACES.PROV + "used"),
                    logicalSource);
        }
    }
}
