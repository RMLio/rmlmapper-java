package be.ugent.rml.records;

import be.ugent.idlab.knows.dataio.access.Access;
import be.ugent.idlab.knows.dataio.access.VirtualAccess;
import be.ugent.idlab.knows.dataio.iterators.SourceIterator;
import be.ugent.idlab.knows.dataio.record.Record;
import be.ugent.rml.NAMESPACES;
import be.ugent.rml.Utils;
import be.ugent.rml.store.QuadStore;
import be.ugent.rml.term.NamedNode;
import be.ugent.rml.term.Term;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class IteratorFormat implements ReferenceFormulationRecordFactory {
    protected Logger logger = LoggerFactory.getLogger(this.getClass());
    protected Map<Access, VirtualAccess> cache;

    public IteratorFormat() {
        this.cache = new HashMap<>();
    }
    @Override
    public List<Record> getRecords(Access access, Term logicalSource, QuadStore rmlStore) throws Exception {
        // if the access object is not yet cached, cache it
        if (!this.cache.containsKey(access)) {
            logger.debug("No document found for {}. Creating new one", access);
            // create a new virtual access to cache the data
            VirtualAccess virtualAccess = new VirtualAccess(access);
            this.cache.put(access, virtualAccess);
        }

        // document is definitely in cache, fetch records out of it
        String iterator;
        List<Term> iterators = Utils.getObjectsFromQuads(rmlStore.getQuads(logicalSource, new NamedNode(NAMESPACES.RML2 + "iterator"), null));

        // SPARQL results have fixed iterators, same as SQL
        Term referenceFormulation = rmlStore.getQuad(logicalSource, new NamedNode(NAMESPACES.RML2 + "referenceFormulation"), null).getObject();
        if (referenceFormulation.equals(new NamedNode(NAMESPACES.FORMATS + "SPARQL_Results_JSON"))) {
            // SPARQL JSON
            iterator = "$.results.bindings[*]";
        } else if (referenceFormulation.equals(new NamedNode(NAMESPACES.FORMATS + "SPARQL_Results_XML"))) {
            // SPARQL XML
            iterator = "//result/binding";
        } else if (referenceFormulation.equals(new NamedNode(NAMESPACES.FORMATS + "SPARQL_Results_CSV"))) {
            // SPARQL CSV
            iterator = "";
        } else if (iterators.isEmpty()) {
            // if there's no iterator present, we're dealing with XML access to PostgreSQL.
            // PostgreSQL XML is built on the fly and accessible on this XPath iterator
            iterator = "/Results/row";
        } else {
            iterator = iterators.get(0).getValue();
        }

        List<Record> sources = new ArrayList<>();
        try (SourceIterator it = getIterator(cache.get(access), iterator)) {
            it.forEachRemaining(sources::add);
        }

        return sources;
    }

    protected abstract SourceIterator getIterator(Access access, String iterator) throws Exception;
}