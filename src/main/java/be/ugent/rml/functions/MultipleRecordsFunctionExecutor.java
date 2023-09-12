package be.ugent.rml.functions;

import be.ugent.idlab.knows.dataio.source.Source;

import java.util.Map;

public interface MultipleRecordsFunctionExecutor {

    Object execute(Map<String, Source> sources) throws Exception;
}
