package be.ugent.rml.records;

import be.ugent.idlab.knows.dataio.access.Access;
import be.ugent.idlab.knows.dataio.iterators.JSONLinesSourceIterator;
import be.ugent.idlab.knows.dataio.iterators.JSONSourceIterator;
import be.ugent.idlab.knows.dataio.iterators.SourceIterator;

import java.io.IOException;
import java.sql.SQLException;

public class JSONRecordFactory extends IteratorFormat {

    @Override
    protected SourceIterator getIterator(Access access, String iterator) throws SQLException, IOException {
        if (access.getContentType().equals("jsonl")) {
            return new JSONLinesSourceIterator(access, iterator);
        }
        return new JSONSourceIterator(access, iterator);
    }
}
