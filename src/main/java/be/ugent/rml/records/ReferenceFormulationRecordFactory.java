package be.ugent.rml.records;

import be.ugent.rml.access.Access;
import be.ugent.rml.store.QuadStore;
import be.ugent.rml.term.Term;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * This is the interface for reference formulation-specific record factories.
 */
public interface ReferenceFormulationRecordFactory {

    /**
     * This method returns a list of records for a data source.
     * @param access the access from which records need to be fetched.
     * @param logicalSource the used Logical Source.
     * @param rmlStore the QuadStore with the RML rules.
     * @return a list of records.
     * @throws IOException
     */
    List<Record> getRecords(Access access, Term logicalSource, QuadStore rmlStore) throws IOException, SQLException, ClassNotFoundException;
}
