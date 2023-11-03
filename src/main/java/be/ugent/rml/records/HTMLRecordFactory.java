package be.ugent.rml.records;

import be.ugent.idlab.knows.dataio.access.Access;
import be.ugent.idlab.knows.dataio.iterators.HTMLSourceIterator;
import be.ugent.idlab.knows.dataio.iterators.SourceIterator;

public class HTMLRecordFactory extends IteratorFormat {
    @Override
    protected SourceIterator getIterator(Access access, String iterator) throws Exception {
        return new HTMLSourceIterator(access, iterator);
    }
}
