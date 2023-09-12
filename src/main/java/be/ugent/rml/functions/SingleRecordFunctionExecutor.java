package be.ugent.rml.functions;

import be.ugent.idlab.knows.dataio.source.Source;

public interface SingleRecordFunctionExecutor {

    Object execute(Source source) throws Exception;
}
