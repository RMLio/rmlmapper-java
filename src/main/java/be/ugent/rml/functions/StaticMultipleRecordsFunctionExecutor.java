package be.ugent.rml.functions;

import be.ugent.rml.Template;
import be.ugent.rml.Utils;
import be.ugent.rml.records.Record;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StaticMultipleRecordsFunctionExecutor implements MultipleRecordsFunctionExecutor {

    private final FunctionModel functionModel;
    private final Map<String, Object[]> parameters;

    public StaticMultipleRecordsFunctionExecutor(FunctionModel model, Map<String, Object[]> parameters) {
        this.functionModel = model;
        this.parameters = parameters;
    }

    @Override
    public Object execute(Map<String, Record> records) throws IOException {
        Map <String, Object> filledInParameters = new HashMap<>();

        for (Map.Entry<String, Object[]> entry : this.parameters.entrySet()) {
            List<Template> templates = (List<Template>) entry.getValue()[1];
            String recordType = (String) entry.getValue()[0];

            List<String> objects = Utils.applyTemplate(templates.get(0), records.get(recordType));

            if (objects.size() > 0) {
                filledInParameters.put(entry.getKey(), objects.get(0));
            } else {
                // TODO check whether key is actually optional!
                filledInParameters.put(entry.getKey(), null);
            }
        }

        return this.functionModel.execute(filledInParameters);
    }
}
