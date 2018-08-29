package be.ugent.rml.functions;

import be.ugent.rml.records.Record;

import java.io.IOException;
import java.util.List;

public interface SingleRecordFunctionExecutor {

    List<?> execute(Record record) throws IOException;
}
