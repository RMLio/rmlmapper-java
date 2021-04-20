package be.ugent.rml.conformer;

import be.ugent.rml.NAMESPACES;
import be.ugent.rml.access.DatabaseType;
import be.ugent.rml.store.QuadStore;

import be.ugent.rml.term.Literal;
import be.ugent.rml.term.NamedNode;
import be.ugent.rml.term.Term;

import java.util.Map;

import static be.ugent.rml.NAMESPACES.*;

/**
 * Converts InputStream of R2RML or RML mapping files to RML mapping files.
 * convert() can fail with an Exception
 * No support for quoted database identifiers
 * Uses the first "a d2rq:Database" it finds as source
 */
public class R2RMLConverter implements Converter {

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
    public boolean detect(Term triplesMap) {
        return store.contains(triplesMap, new NamedNode(RR + "logicalTable"), null);
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
    public void convert(Term triplesMap, Map<String, String> mappingOptions) throws Exception {
        // UNSAFE store changes not yet allowed; check if all required properties are present
        Term logicalTable;

        // Get logical table
        try {
            logicalTable = store
                    .getQuad(triplesMap, new NamedNode(RR + "logicalTable"), null)
                    .getObject();
        } catch (Exception e) {
            // Also not R2RML
            throw new UnsupportedOperationException("Mapping is either RML without logicalSource or R2RML without logicalTable");
        }

        Term database;

        // SAFE store changes allowed

//        if (! store.contains(null, null, new NamedNode(D2RQ + "Database"))) {
        database = new NamedNode(triplesMap.getValue() + "_database");

        if (mappingOptions != null) {
            store.addQuad(database, new NamedNode(RDF + "type"), new NamedNode(D2RQ + "Database"));

            for (Map.Entry<String, String> entry : mappingOptions.entrySet()) {
                String removePrefix = entry.getKey();
                store.addQuad(database, new NamedNode(D2RQ + removePrefix), new Literal(entry.getValue()));

                if (removePrefix.equals("jdbcDSN")) {
                    DatabaseType type = DatabaseType.getDBtype(entry.getValue());
                    String driver = type.getDriver();

                    store.addQuad(database, new NamedNode(D2RQ + "jdbcDriver"), new Literal(driver));
                }
            }
        }
//        }
//        else {
//            database = store.getQuad(null, null, new NamedNode(D2RQ + "Database")).getSubject();
//        }

        // Add logical source
        String logicalSourceIRI = triplesMap.getValue() + "_logicalSource";
        Term logicalSource = new NamedNode(logicalSourceIRI);

        store.addQuad(triplesMap, new NamedNode(RML + "logicalSource"), logicalSource, null);
        store.addQuad(logicalSource, new NamedNode(RML + "referenceFormulation"),
                new NamedNode(NAMESPACES.QL + "CSV")
        );

        // Also add old R2RML for AccessFactory property
        // TODO issue #130
        store.addQuad(logicalSource, new NamedNode(RR + "sqlVersion"),
                new NamedNode(RR + "SQL2008")
        );
        store.addQuad(logicalSource, new NamedNode(RML + "source"),
                database
        );
        store.tryPropertyTranslation(logicalTable, new NamedNode(RR + "sqlQuery"), logicalSource, new NamedNode(RML + "query"));
        store.tryPropertyTranslation(logicalTable, new NamedNode(RR + "tableName"), logicalSource, new NamedNode(RR + "tableName"));
        store.tryPropertyTranslation(logicalTable, new NamedNode(RR + "sqlVersion"), logicalSource, new NamedNode(RR + "sqlVersion"));

        // Rename on whole store instead of deep search in TriplesMap Resource
        store.renameAll(new NamedNode(RR + "column"), new NamedNode(RML + "reference"));
        store.removeQuads(triplesMap, new NamedNode(RR + "logicalTable"), null);
    }
}
