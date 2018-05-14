package be.ugent.rml.functions;

import be.ugent.rml.Element;
import be.ugent.rml.Utils;
import be.ugent.rml.records.Record;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Function {

    private final FunctionModel functionModel;

    public Function(FunctionModel model) {
        this.functionModel = model;
    }

    public List<?> execute(Record record, Map<String, List<List<Element>>> parameters) {
        Map <String, Object> filledInParameters = new HashMap<>();
        for (Map.Entry<String, List<List<Element>>> entry : parameters.entrySet()) {
            filledInParameters.put(entry.getKey(), Utils.applyTemplate(entry.getValue().get(0), record).get(0));
        }

        return this.functionModel.execute(filledInParameters);
    }
}
