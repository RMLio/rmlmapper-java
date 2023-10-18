package be.ugent.rml.records;

import be.ugent.idlab.knows.dataio.access.Access;
import be.ugent.idlab.knows.dataio.iterators.SourceIterator;
import be.ugent.idlab.knows.dataio.iterators.XMLSourceIterator;

public class XMLRecordFactory extends IteratorFormat {
    @Override
    protected SourceIterator getIterator(Access access, String iterator) throws Exception {
        return new XMLSourceIterator(access, iterator);
    }
}
