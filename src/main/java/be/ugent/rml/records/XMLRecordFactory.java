package be.ugent.rml.records;

import be.ugent.idlab.knows.dataio.access.Access;
import be.ugent.idlab.knows.dataio.iterators.SourceIterator;
import be.ugent.idlab.knows.dataio.iterators.XMLSourceIterator;
import net.sf.saxon.s9api.SaxonApiException;

import java.io.IOException;
import java.sql.SQLException;

public class XMLRecordFactory extends IteratorFormat {
    @Override
    protected SourceIterator getIterator(Access access, String iterator) throws SQLException, IOException, SaxonApiException {
        return new XMLSourceIterator(access, iterator);
    }
}
