package be.ugent.rml.functions;

import be.ugent.idlab.knows.dataio.source.Source;
import be.ugent.rml.records.Record;

import java.util.HashMap;

public abstract class AbstractSingleRecordFunctionExecutor implements SingleRecordFunctionExecutor {

    protected MultipleRecordsFunctionExecutor functionExecutor;

    public Object execute(Source source) throws Exception {
        HashMap<String, Source> recordsMap = new HashMap<>();
        recordsMap.put("_default", source);

        return this.functionExecutor.execute(recordsMap);
    }
}
