package be.ugent.rml.records;

import be.ugent.idlab.knows.dataio.access.Access;
import be.ugent.idlab.knows.dataio.access.VirtualAccess;
import be.ugent.idlab.knows.dataio.iterators.SourceIterator;
import be.ugent.idlab.knows.dataio.source.Source;
import be.ugent.rml.NAMESPACES;
import be.ugent.rml.Utils;
import be.ugent.rml.store.QuadStore;
import be.ugent.rml.term.NamedNode;
import be.ugent.rml.term.Term;
import net.sf.saxon.s9api.SaxonApiException;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class IteratorFormat2 implements ReferenceFormulationRecordFactory {
    protected Map<Access, VirtualAccess> cache;

    public IteratorFormat2() {
        this.cache = new HashMap<>();
    }
    @Override
    public List<Source> getRecords(Access access, Term logicalSource, QuadStore rmlStore) throws Exception {
        // if the access object is not yet cached, cache it
        if (!this.cache.containsKey(access)) {
            // create a new virtual access to cache the data
            VirtualAccess virtualAccess = new VirtualAccess(access);
            this.cache.put(access, virtualAccess);
        }

        // document is definitely in cache, fetch records out of it
        String iterator;
        List<Term> iterators = Utils.getObjectsFromQuads(rmlStore.getQuads(logicalSource, new NamedNode(NAMESPACES.RML + "iterator"), null));

        if (iterators.isEmpty()) {
            // if there's no iterator present, we're dealing with XML access to PostgreSQL.
            // XML is built on the fly and accessible on this XPath iterator
            iterator = "/Results/row";
        } else {
            iterator = iterators.get(0).getValue();
        }

        List<Source> sources = new ArrayList<>();
        try (SourceIterator it = getIterator(cache.get(access), iterator)) {
            it.forEachRemaining(sources::add);
        }

        return sources;
    }

    protected abstract SourceIterator getIterator(Access access, String iterator) throws SQLException, IOException, SaxonApiException;
}