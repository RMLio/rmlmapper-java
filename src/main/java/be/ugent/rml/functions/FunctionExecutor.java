package be.ugent.rml.functions;

import be.ugent.rml.records.Record;

import java.io.IOException;
import java.util.List;

public interface FunctionExecutor {

    List<?> execute(Record record) throws IOException;
}
