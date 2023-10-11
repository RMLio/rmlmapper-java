package be.ugent.rml.functions;


import be.ugent.idlab.knows.dataio.record.Record;

public interface SingleRecordFunctionExecutor {

    Object execute(Record record) throws Exception;

    default boolean needsMagicEndValue() {
        return false;
    }
}
