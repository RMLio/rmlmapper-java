package be.ugent.rml.conformer;

import be.ugent.idlab.knows.dataio.access.DatabaseType;
import be.ugent.rml.records.ReferenceFormulation;
import be.ugent.rml.store.Quad;
import be.ugent.rml.store.QuadStore;
import be.ugent.rml.term.BlankNode;
import be.ugent.rml.term.Literal;
import be.ugent.rml.term.NamedNode;
import be.ugent.rml.term.Term;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static be.ugent.rml.NAMESPACES.*;

public class RMLConverterNew implements Converter {

    private static Logger logger = LoggerFactory.getLogger(RMLConverterNew.class);

    private final Map<String, ReplacementEntry> replacementsObjects = new HashMap<>() {{
        /* Old RML */
        put(RML + "BaseSource", new ReplacementEntry(RML2 + "LogicalSource", null));
        /* R2RML */
        put(RR + "BaseTableOrView", new ReplacementEntry(RML2 + "LogicalSource", null));
        put(RR + "Literal", new ReplacementEntry(RML2 + "Literal", null));
        put(RR + "R2RMLView", new ReplacementEntry(RML2 + "LogicalSource", null));
        put(RR + "SQL2008", new ReplacementEntry(RML2 + "SQL2008", null));

    }};

    private final Map<String, ReplacementEntry> replacementsPredicates = new HashMap<>() {{
        put(RML + "source", new ReplacementEntry(RML2 + "source", (quad, quadStore) -> processSources(quad, quadStore)));
        put(RML + "query", new ReplacementEntry(RML2 + "query", (quad, quadStore) -> processQueries(quad, quadStore)));
        put(RR + "column", new ReplacementEntry(RML2 + "reference", null));
        put(RR + "SQL2008", new ReplacementEntry(RML2 + "SQL2008", null));
        put(RR + "logicalTable", new ReplacementEntry(RML2 + "logicalSource", (quad, quadStore) -> processLogicalTable(quad, quadStore)));
        put(RR + "sqlVersion", new ReplacementEntry(RML2 + "referenceFormulation", null));
        put(RR + "tableName", new ReplacementEntry(RML2 + "source", RMLConverterNew::processTableName));
        put(RR + "Literal", new ReplacementEntry(RML2 + "Literal", RMLConverterNew::processTableName));
    }};

    private final Map<String, String> objectRenames = new HashMap<>() {{
        put(QL + "CSV", ReferenceFormulation.CSV);
        put(QL + "JSONPath", ReferenceFormulation.JSONPath);
        put(QL + "XPath", ReferenceFormulation.XPath);
        put(RML + "LogicalSource", RML2 + "LogicalSource");
        put(RML + "TriplesMap", RML2 + "TriplesMap");
        put(RR + "BlankNode", RML2 + "BlankNode");
        put(RR + "IRI", RML2 + "IRI");
        put(RR + "Join", RML2 + "Join");
        put(RR + "PredicateMap", RML2 + "PredicateMap");
        put(RR + "PredicateObjectMap", RML2 + "PredicateObjectMap");
        put(RR + "RefObjectMap", RML2 + "RefObjectMap");
        put(RR + "SubjectMap", RML2 + "SubjectMap");
        put(RR + "ObjectMap", RML2 + "ObjectMap");
        put(RR + "TermMap", RML2 + "TermMap");
        put(RR + "TriplesMap", RML2 + "TriplesMap");
        put(RR + "GraphMap", RML2 + "GraphMap");
        put(RR + "defaultGraph", RML2 + "defaultGraph");
        put(RML + "LanguageMap", RML2 + "LanguageMap");
        put(RMLT + "LogicalTarget", RML2 + "LogicalTarget");
        put(RR + "GraphMap", RML2 + "GraphMap");
        put(RMLT + "Target", RML2 + "Target");
    }};

    /*
    TODO: figure out not included entries:
    - rr:inverseExpression: what to do with the deprecated tag?
     */
    private final Map<String, String> predicateRenames = new HashMap<>() {{
        /* Old RML */
        put(RML + "iterator", RML2 + "iterator");
        put(RML + "logicalSource", RML2 + "logicalSource");
        put(RML + "logicalTarget", RML2 + "logicalTarget");
        put(RML + "reference", RML2 + "reference");
        put(RML + "referenceFormulation", RML2 + "referenceFormulation");
        put(RML + "languageMap", RML2 + "languageMap");
        put(RML + "parentTermMap", RML2 + "parentTermMap");

        /* Old RMLT */
        put(RMLT + "target", RML2 + "target");
        put(RMLT + "serialization", RML2 + "serialization");
        put(RMLT + "compression", RML2 + "compression");

        /* R2RML */
        put(RR + "joinCondition", RML2 + "joinCondition");
        put(RR + "parent", RML2 + "parent");
        put(RR + "child", RML2 + "child");
        put(RR + "parentTriplesMap", RML2 + "parentTriplesMap");

        put(RR + "column", RML2 + "reference");
        put(RR + "class", RML2 + "class");
        put(RR + "constant", RML2 + "constant");
        put(RR + "datatype", RML2 + "datatype");
        put(RR + "graph", RML2 + "graph");
        put(RR + "graphMap", RML2 + "graphMap");
        put(RR + "language", RML2 + "language");
        put(RR + "object", RML2 + "object");
        put(RR + "objectMap", RML2 + "objectMap");
        put(RR + "predicate", RML2 + "predicate");
        put(RR + "predicateMap", RML2 + "predicateMap");
        put(RR + "predicateObjectMap", RML2 + "predicateObjectMap");
        put(RR + "subject", RML2 + "subject");
        put(RR + "subjectMap", RML2 + "subjectMap");
        put(RR + "termType", RML2 + "termType");
        put(RR + "template", RML2 + "template");
        put(RR + "logicalTable", RML2 + "logicalSource");
    }};
    private final Set<String> obsoletes = new HashSet<>() {{
        add(RR + "sqlVersion");
    }};
    private final QuadStore store;

    public RMLConverterNew(QuadStore store) {
        this.store = store;
    }

    private static void processTableName(Quad tableName, QuadStore store) {
        store.addQuad(tableName.getSubject(), new NamedNode(RML2 + "referenceFormulation"), new NamedNode(RML2 + "SQL2008Table"));
        store.removeQuads(tableName.getSubject(), new NamedNode(RML2 + "referenceFormulation"), null);
    }

    /**
     * Replace a logical table quad with a proper logical source
     * @param quad
     * @param quadStore
     */
    private void processLogicalTable(Quad logicalTableQuad, QuadStore quadStore) {
        BlankNode blank = new BlankNode();
        quadStore.addQuad(logicalTableQuad.getSubject(), new NamedNode(RML2 + "logicalSource"), blank);

        // add the reference formulation of the logical source
        quadStore.removeQuads(logicalTableQuad.getSubject(), new NamedNode(RML2 + "referenceFormulation"), new NamedNode(ReferenceFormulation.RDBTable));

        // now have blank contain all required fields
        // translate the table name as rml:source
        Term logicalTable = logicalTableQuad.getObject();
        Term tableName = quadStore.getQuads(logicalTable, new NamedNode(RR + "tableName"), null)
                .get(0)
                .getObject();

        quadStore.addQuad(blank, new NamedNode(RML2 + "source"), tableName);
        quadStore.addQuad(blank, new NamedNode(RML2 + "referenceFormulation"), new NamedNode(ReferenceFormulation.RDBTable));
        quadStore.removeQuads(logicalTableQuad);
    }

    @Override
    public void convert(Map<String, String> mappingOptions) throws Exception {
        // inject DB sources for every triple map that has a logicalTable as source
        if (mappingOptions != null) {
            // R2RML conversion
            // convert all logical tables into proper logical sources
            List<Quad> logicalTableMaps = this.store.getQuads(null, new NamedNode(RR + "logicalTable"), null);
            for (Quad map : logicalTableMaps) {
                // insert the database for the logical source
                Term database = new NamedNode(map.getSubject().getValue() + "_database");
                this.store.addQuad(database, new NamedNode(RDF + "type"), new NamedNode(D2RQ + "Database"));
                for (Map.Entry<String, String> entry : mappingOptions.entrySet()) {
                    this.store.addQuad(database, new NamedNode(D2RQ + entry.getKey()), new Literal(entry.getValue()));

                    if (entry.getKey().equals("jdbcDSN")) {
                        DatabaseType type = DatabaseType.getDBtype(entry.getValue());
                        this.store.addQuad(database, new NamedNode(D2RQ + "jdbcDriver"), new Literal(type.getDriver()));
                    }
                }

                Term logicalSource = new NamedNode(map.getSubject().getValue() + "_logicalSource");
                this.store.addQuad(logicalSource, new NamedNode(RML2+"source"), database);

                // translate rr:logicalTable to rml:source
                // grab the logical table
                Term logicalTable = map.getObject();
                List<Quad> tableNames = this.store.getQuads(logicalTable, new NamedNode(RR + "tableName"), null);
                if (tableNames.isEmpty()) {
                    // no tableNames present, SQL query must be present
                    List<Quad> queries = this.store.getQuads(logicalTable, new NamedNode(RR + "sqlQuery"), null);
                    if (queries.isEmpty()) {
                        throw new IllegalArgumentException("Logical table contains neither a tableName, nor a SQL query");
                    }
                    this.store.addQuad(logicalSource, new NamedNode(RML2 + "referenceFormulation"), new NamedNode(ReferenceFormulation.RDBQuery));
                    this.store.addQuad(logicalSource, new NamedNode(RML2 + "iterator"), new Literal(queries.get(0).getObject().getValue()));

                    this.store.removeQuads(logicalTable, new NamedNode(RR + "sqlQuery"), null);
                } else {
                    this.store.addQuad(logicalSource, new NamedNode(RML2 + "referenceFormulation"), new NamedNode(ReferenceFormulation.RDBTable));
                    this.store.addQuad(logicalSource, new NamedNode(RML2 + "iterator"), new Literal(tableNames.get(0).getObject().getValue()));

                    this.store.removeQuads(logicalTable, new NamedNode(RR + "tableName"), null);
                }


                // connect the logical source to the map
                this.store.addQuad(map.getSubject(), new NamedNode(RML2 + "logicalSource"), logicalSource);

                // clean up the store: remove logicalTable and tableName
                this.store.removeQuads(map.getSubject(), new NamedNode(RR + "logicalTable"), null);

            }

            // convert all logical sources that have a rr:tableName into proper sources
            List<Quad> tableNameLogicalSources = this.store.getQuads(null, new NamedNode(RR + "tableName"), null);
            for (Quad ls : tableNameLogicalSources) {
                // drop the sql version
                // set the correct reference formulation
                this.store.addQuad(ls.getSubject(), new NamedNode(RML2 + "referenceFormulation"), new NamedNode(ReferenceFormulation.RDBTable));
                // put table name in rml:iterator
                String tableName = ls.getObject().getValue();
                this.store.addQuad(ls.getSubject(), new NamedNode(RML2 + "iterator"), new Literal(tableName));
                // drop obsolete fields
                this.store.removeQuads(ls.getSubject(), new NamedNode(RR + "sqlVersion"), null);
                this.store.removeQuads(ls);
            }

            // convert all logical sources that have a rml:query to proper sources
            List<Quad> queryLogicalSources = this.store.getQuads(null, new NamedNode(RML + "query"), null);
            for (Quad ls : queryLogicalSources) {
                Term source = null;
                if (this.store.contains(ls.getSubject(), new NamedNode(RML + "source"), null)) {
                    source = this.store.getQuad(ls.getSubject(), new NamedNode(RML + "source"), null).getObject();
                }
                // drop any obsolete reference formulations
                this.store.removeQuads(ls.getSubject(), new NamedNode(RML + "referenceFormulation"), null);
                // set the proper reference formulation
                if (source != null && this.store.contains(source, new NamedNode(SD + "resultFormat"), null)) {
                    Term resultsFormat = this.store.getQuad(source, new NamedNode(SD + "resultFormat"), null).getObject();
                    this.store.addQuad(ls.getSubject(), new NamedNode(RML2 + "referenceFormulation"), resultsFormat);
                } else {
                    this.store.addQuad(ls.getSubject(), new NamedNode(RML2 + "referenceFormulation"), new NamedNode(ReferenceFormulation.RDBQuery));
                }
                // set the query into the iterator
                this.store.addQuad(ls.getSubject(), new NamedNode(RML2 + "iterator"), ls.getObject());

                // drop the obsolete quads
                this.store.removeQuads(ls.getSubject(), new NamedNode(RR + "sqlVersion"), null);
                this.store.removeQuads(ls);
            }
        }


        for (Map.Entry<String, ReplacementEntry> e : this.replacementsPredicates.entrySet()) {
            List<Quad> quads = this.store.getQuads(null, new NamedNode(e.getKey()), null);
            ReplacementEntry entry = e.getValue();
            for (Quad q : quads) {
                if (entry.function != null) {
                    entry.function.call(q, this.store);
                } else {
                    // apply the replace and warn
                    this.store.renameAllPredicates(new NamedNode(e.getKey()), new NamedNode(entry.replacementTerm));
                    logger.warn("Predicate replacement function for term {} is not yet defined!", e.getKey());
                }
            }
        }

        for (Map.Entry<String, ReplacementEntry> e : this.replacementsObjects.entrySet()) {
            // find all quads that carry this object
            List<Quad> quads = this.store.getQuads(null, null, new NamedNode(e.getKey()));
            ReplacementEntry entry = e.getValue();
            for (Quad q : quads) {
                this.store.addQuad(q.getSubject(), q.getPredicate(), new NamedNode(entry.replacementTerm));
                if (entry.function != null) {
                    entry.function.call(q, store);
                } else {
                    logger.warn("Object replacement function for term {} is not yet defined!", e.getKey());
                }
            }
            this.store.removeQuads(quads);
        }

        // apply simple renames first
        for (Map.Entry<String, String> e : this.predicateRenames.entrySet()) {
            String old = e.getKey();
            String _new = e.getValue();

            this.store.renameAllPredicates(new NamedNode(old), new NamedNode(_new));
        }

        for (Map.Entry<String, String> e : this.objectRenames.entrySet()) {
            String old = e.getKey();
            String _new = e.getValue();

            this.store.renameAllObjects(new NamedNode(old), new NamedNode(_new));
        }

        dropObsolete();
    }

    private void processSources(Quad source, QuadStore store) {
        if (source.getObject().isLiteral()) {
            String path = source.getObject().getValue();
            BlankNode node = new BlankNode();
            store.addQuad(node, new NamedNode(RDF + "type"), new NamedNode(DCAT + "Distribution"));
            store.addQuad(node, new NamedNode(RDF + "type"), new NamedNode(RML2 + "Source"));
            store.addQuad(node, new NamedNode(DCAT + "downloadURL"), new Literal(path)); // TODO: file:// prefix
            store.addQuad(source.getSubject(), new NamedNode(RML2 + "source"), node);
            store.removeQuads(source.getSubject(), source.getPredicate(), source.getObject());
        }
        store.renameAllPredicates(new NamedNode(RML + "source"), new NamedNode(RML2 + "source"));
    }

    private void processQueries(Quad query, QuadStore store) throws Exception {
        Term source = store.getQuad(query.getSubject(), new NamedNode(RML2 + "source"), null).getObject();
        System.out.println("processQueries: " + source.getValue());
        if (store.contains(source, new NamedNode(SD + "resultFormat"), null)) {
            Term supportedLanguage = store.getQuad(source, new NamedNode(SD + "resultFormat"), null).getObject();
            store.addQuad(query.getSubject(), new NamedNode(RML2 + "referenceFormulation"), supportedLanguage);
        }
        else {
            store.addQuad(query.getSubject(), new NamedNode(RML2 + "referenceFormulation"), new NamedNode(RML2 + "SQL2008Query"));
        }

        store.removeQuads(query.getSubject(), new NamedNode(RML + "referenceFormulation"), null);
        store.removeQuads(query.getSubject(), new NamedNode(RML + "iterator"), null);
        store.renameAllPredicates(new NamedNode(RML + "query"), new NamedNode(RML2 + "iterator"));
    }

    private void dropObsolete() {
        for (String obsolete : obsoletes) {
            this.store.removeQuads(null, new NamedNode(obsolete), null);
        }
    }

    /**
     * Serves as a function to run when converting terms that are replaced by another term
     */
    @FunctionalInterface
    private interface ReplaceFunction {
        void call(Quad quad, QuadStore store) throws Exception;
    }

    /**
     * Private record to contain the term to replace and a function to further execute on the QuadStore
     * @param replacementTerm term to be put instead of the previous one
     * @param function function to run at replacement time
     */
    private record ReplacementEntry(String replacementTerm, ReplaceFunction function) {
    }
}
