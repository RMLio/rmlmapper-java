package be.ugent.rml.extractor;


import be.ugent.idlab.knows.dataio.record.Record;

import java.util.List;

public interface Extractor {

    List<Object> extract(Record record);
}
