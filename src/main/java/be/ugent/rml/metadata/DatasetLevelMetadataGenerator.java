package be.ugent.rml.metadata;

import be.ugent.rml.NAMESPACES;
import be.ugent.rml.store.QuadStore;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

import java.util.List;
import java.time.Instant;

/**
 * Unique class -- reusable outside of the mapper
 */
public class DatasetLevelMetadataGenerator {

    private static final ValueFactory valueFactory = SimpleValueFactory.getInstance();


    public static void createMetadata(Value rdfDataset, Value rdfDatasetGeneration, Value rmlMapper,
                                      QuadStore result, List<Value> logicalSources,
                                      String startTimestamp, String stopTimestamp, String[] mappingFiles) {
        // <#RDF_Dataset>
        result.addQuad(rdfDataset, valueFactory.createIRI(NAMESPACES.RDF + "type"),
                valueFactory.createIRI(NAMESPACES.PROV + "Entity"));
        result.addQuad(rdfDataset, valueFactory.createIRI(NAMESPACES.RDF + "type"),
                valueFactory.createIRI(NAMESPACES.VOID + "Dataset"));
        result.addQuad(rdfDataset, valueFactory.createIRI(NAMESPACES.PROV + "generatedAtTime"),
                valueFactory.createLiteral(Instant.now().toString(), valueFactory.createIRI(NAMESPACES.XSD + "dateTime")));
        result.addQuad(rdfDataset, valueFactory.createIRI(NAMESPACES.PROV + "wasGeneratedBy"),
                rdfDatasetGeneration);
        result.addQuad(rdfDataset, valueFactory.createIRI(NAMESPACES.PROV + "wasAssociatedWith"),
                rmlMapper);

        // <#rmlMapper>
        result.addQuad(rmlMapper, valueFactory.createIRI(NAMESPACES.RDF + "type"),
                valueFactory.createIRI(NAMESPACES.PROV + "Agent"));
        result.addQuad(rmlMapper, valueFactory.createIRI(NAMESPACES.PROV + "type"),
                valueFactory.createIRI(NAMESPACES.PROV + "SoftwareAgent"));

        // <#RDFdataset_Generation>
        result.addQuad(rdfDatasetGeneration, valueFactory.createIRI(NAMESPACES.RDF + "type"),
                valueFactory.createIRI(NAMESPACES.PROV + "Activity"));
        result.addQuad(rdfDatasetGeneration, valueFactory.createIRI(NAMESPACES.PROV + "generated"),
                rdfDataset);
        result.addQuad(rdfDatasetGeneration, valueFactory.createIRI(NAMESPACES.PROV + "startedAtTime"),
                valueFactory.createLiteral(startTimestamp, valueFactory.createIRI(NAMESPACES.XSD + "dateTime")));
        result.addQuad(rdfDatasetGeneration, valueFactory.createIRI(NAMESPACES.PROV + "endedAtTime"),
                valueFactory.createLiteral(stopTimestamp, valueFactory.createIRI(NAMESPACES.XSD + "dateTime")));
        for (String mappingFile : mappingFiles) {
            result.addQuad(rdfDatasetGeneration, valueFactory.createIRI(NAMESPACES.PROV + "used"),
                valueFactory.createIRI(String.format("file://%s", mappingFile)));
        }
        for (Value logicalSource : logicalSources) {
            result.addQuad(rdfDataset, valueFactory.createIRI(NAMESPACES.PROV + "wasDerivedFrom"),
                    logicalSource);
            result.addQuad(rdfDatasetGeneration, valueFactory.createIRI(NAMESPACES.PROV + "used"),
                    logicalSource);
        }
    }
}
