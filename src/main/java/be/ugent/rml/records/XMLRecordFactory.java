package be.ugent.rml.records;

import be.ugent.idlab.knows.dataio.access.VirtualAccess;
import be.ugent.idlab.knows.dataio.iterators.XMLSourceIterator;
import be.ugent.idlab.knows.dataio.source.Source;
import net.sf.saxon.s9api.SaxonApiException;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class XMLRecordFactory extends IteratorFormat2 {
    @Override
    protected List<Source> getSourcesFromAccess(VirtualAccess access, String iterator) {
        List<Source> sources = new ArrayList<>();
        try (XMLSourceIterator it = new XMLSourceIterator(access, iterator)) {
            while (it.hasNext()) {
                sources.add(it.next());
            }
        } catch (SQLException | IOException | SaxonApiException e) {
            throw new RuntimeException(e);
        }
        return sources;
    }
}
