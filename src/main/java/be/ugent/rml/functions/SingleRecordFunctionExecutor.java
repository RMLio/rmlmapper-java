package be.ugent.rml.functions;

import be.ugent.rml.records.Record;

import java.io.IOException;
import java.util.List;

public interface SingleRecordFunctionExecutor {

    Object execute(Record record) throws IOException;
}
