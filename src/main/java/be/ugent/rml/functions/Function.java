package be.ugent.rml.functions;

import be.ugent.rml.Element;
import be.ugent.rml.records.Record;

import java.util.List;
import java.util.Map;

public interface Function {

    List<String> execute(Record record, Map<String, List<List<Element>>> parameters);
}
