package be.ugent.rml.records;

import be.ugent.rml.access.Access;

import java.io.IOException;
import java.util.List;

public interface ReferenceFormulationRecordFactory {

    List<Record> getRecords(Access access, String iterator) throws IOException;
}
