package be.ugent.rml.functions;

import be.ugent.rml.records.Record;

import java.io.IOException;

public interface JoinConditionFunctionExecutor {

    boolean execute(Record child, Record parent) throws IOException;
}
