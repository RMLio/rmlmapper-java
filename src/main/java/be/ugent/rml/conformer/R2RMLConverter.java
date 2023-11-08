package be.ugent.rml.conformer;

import be.ugent.idlab.knows.dataio.access.DatabaseType;
import be.ugent.rml.NAMESPACES;
import be.ugent.rml.store.QuadStore;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;


import java.util.Map;

import static be.ugent.rml.NAMESPACES.*;

/**
 * Converts InputStream of R2RML or RML mapping files to RML mapping files.
 * convert() can fail with an Exception
 * No support for quoted database identifiers
 * Uses the first "a d2rq:Database" it finds as source
 */
public class R2RMLConverter implements Converter {
    private static final ValueFactory valueFactory = SimpleValueFactory.getInstance();

    private QuadStore store;

    R2RMLConverter(QuadStore store) {
        this.store = store;
    }

    /**
     * TriplesMap is R2RML if RR:logicalTable property is found
     *
     * @param triplesMap
     * @return true if triplesMap is R2RML (tripleMap contains a rr:logicalTable)
     */
    public boolean detect(Value triplesMap) {
        return store.contains(triplesMap, valueFactory.createIRI(RR + "logicalTable"), null);
    }

    /**
     * Tries to convert R2RML TriplesMap to rml by:
     * - renaming logicalTable to logicalSource
     * - adding referenceFormulation: CSV
     * - adding sqlVersion: SQL2008
     * - renaming rr:sqlQuery to rml:query
     * - renaming all rr:column properties to rml:reference
     * - removing all rr:logicalTable nodes, leaving rml:logicalSource to take their place
     * - moving rest over from logicalTable to logicalSource
     *
     * @param triplesMap rr:TriplesMap
     */
    public void convert(Value triplesMap, Map<String, String> mappingOptions) throws Exception {
        // UNSAFE store changes not yet allowed; check if all required properties are present
        Value logicalTable;

        // Get logical table
        try {
            logicalTable = store
                    .getQuad(triplesMap, valueFactory.createIRI(RR + "logicalTable"), null)
                    .getObject();
        } catch (Exception e) {
            // Also not R2RML
            throw new UnsupportedOperationException("Mapping is either RML without logicalSource or R2RML without logicalTable");
        }

        Value database;

        // SAFE store changes allowed

//        if (! store.contains(null, null, valueFactory.createIRI(D2RQ + "Database"))) {
        database = valueFactory.createIRI(triplesMap.stringValue() + "_database");

        if (mappingOptions != null) {
            store.addQuad(database, valueFactory.createIRI(RDF + "type"), valueFactory.createIRI(D2RQ + "Database"));

            for (Map.Entry<String, String> entry : mappingOptions.entrySet()) {
                String removePrefix = entry.getKey();
                store.addQuad(database, valueFactory.createIRI(D2RQ + removePrefix), valueFactory.createLiteral(entry.getValue()));

                if (removePrefix.equals("jdbcDSN")) {
                    DatabaseType type = DatabaseType.getDBtype(entry.getValue());
                    String driver = type.getDriver();

                    store.addQuad(database, valueFactory.createIRI(D2RQ + "jdbcDriver"), valueFactory.createLiteral(driver));
                }
            }
        }
//        }
//        else {
//            database = store.getQuad(null, null, new NamedNode(D2RQ + "Database")).getSubject();
//        }

        // Add logical source
        String logicalSourceIRI = triplesMap.stringValue() + "_logicalSource";
        Value logicalSource = valueFactory.createIRI(logicalSourceIRI);

        store.addQuad(triplesMap, valueFactory.createIRI(RML + "logicalSource"), logicalSource, null);
        store.addQuad(logicalSource, valueFactory.createIRI(RML + "referenceFormulation"),
                valueFactory.createIRI(NAMESPACES.QL + "CSV")
        );

        // Also add old R2RML for AccessFactory property
        store.addQuad(logicalSource, valueFactory.createIRI(RML + "source"),
                database
        );
        store.tryPropertyTranslation(logicalTable, valueFactory.createIRI(RR + "sqlQuery"), logicalSource, valueFactory.createIRI(RML + "query"));
        store.tryPropertyTranslation(logicalTable, valueFactory.createIRI(RR + "tableName"), logicalSource, valueFactory.createIRI(RR + "tableName"));
        store.tryPropertyTranslation(logicalTable, valueFactory.createIRI(RR + "sqlVersion"), logicalSource, valueFactory.createIRI(RR + "sqlVersion"));

        // Rename on whole store instead of deep search in TriplesMap Resource
        store.renameAll(valueFactory.createIRI(RR + "column"), valueFactory.createIRI(RML + "reference"));
        store.removeQuads(triplesMap, valueFactory.createIRI(RR + "logicalTable"), null);
    }
}
