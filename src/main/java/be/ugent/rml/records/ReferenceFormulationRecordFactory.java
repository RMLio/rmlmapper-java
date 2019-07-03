package be.ugent.rml.records;

import be.ugent.rml.access.Access;
import be.ugent.rml.store.QuadStore;
import be.ugent.rml.term.Term;

import java.io.IOException;
import java.util.List;

public interface ReferenceFormulationRecordFactory {

    List<Record> getRecords(Access access, Term logicalSource, QuadStore rmlStore) throws IOException;
}
