package be.ugent.rml.records;

import be.ugent.idlab.knows.dataio.access.VirtualAccess;
import be.ugent.idlab.knows.dataio.iterators.JSONSourceIterator;
import be.ugent.idlab.knows.dataio.source.Source;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JSONRecordFactory2 extends IteratorFormat2 {
    @Override
    protected List<Source> getSourcesFromAccess(VirtualAccess access, String iterator) {
        List<Source> sources = new ArrayList<>();

        try (JSONSourceIterator it = new JSONSourceIterator(access, iterator)) {
            while(it.hasNext()) {
                sources.add(it.next());
            }
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);
        }

        return sources;
    }
}
