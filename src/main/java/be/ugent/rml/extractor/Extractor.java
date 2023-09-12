package be.ugent.rml.extractor;

import be.ugent.idlab.knows.dataio.source.Source;
import be.ugent.rml.records.Record;

import java.util.List;

public interface Extractor {

    List<Object> extract(Source source);
}
