package be.ugent.rml;

import be.ugent.rml.records.Record;

import java.util.HashMap;
import java.util.List;

public interface Function {

    List<String> execute(Record record, HashMap<String, Value> parameters);
}
