package be.ugent.rml.records;

import be.ugent.idlab.knows.dataio.access.Access;
import be.ugent.idlab.knows.dataio.access.VirtualAccess;
import be.ugent.idlab.knows.dataio.iterators.JSONSourceIterator;
import be.ugent.idlab.knows.dataio.iterators.SourceIterator;
import be.ugent.idlab.knows.dataio.source.Source;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JSONRecordFactory2 extends IteratorFormat2 {

    @Override
    protected SourceIterator getIterator(Access access, String iterator) throws SQLException, IOException {
        return new JSONSourceIterator(access, iterator);
    }
}
