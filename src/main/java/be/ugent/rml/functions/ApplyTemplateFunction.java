package be.ugent.rml.functions;

import be.ugent.rml.Element;
import be.ugent.rml.Utils;
import be.ugent.rml.records.Record;

import java.util.List;
import java.util.Map;

public class ApplyTemplateFunction implements Function{

    @Override
    public List<String> execute(Record record, Map<String, List<List<Element>>> parameters) {
        return Utils.applyTemplate(parameters.get("_TEMPLATE").get(0), record);
    }
}
