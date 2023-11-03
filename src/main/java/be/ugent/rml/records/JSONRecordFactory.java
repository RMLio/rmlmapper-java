package be.ugent.rml.records;

import be.ugent.idlab.knows.dataio.access.Access;
import be.ugent.idlab.knows.dataio.iterators.JSONLinesSourceIterator;
import be.ugent.idlab.knows.dataio.iterators.JSONSourceIterator;
import be.ugent.idlab.knows.dataio.iterators.SourceIterator;

public class JSONRecordFactory extends IteratorFormat {

    @Override
    protected SourceIterator getIterator(Access access, String iterator) throws Exception {
        String contentType = access.getContentType();
        if (contentType.equals("jsonl") || contentType.equals("application/jsonl")) {
            return new JSONLinesSourceIterator(access, iterator);
        }
        return new JSONSourceIterator(access, iterator);
    }
}
