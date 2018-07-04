package be.ugent.rml.functions;

import be.ugent.rml.Template;
import be.ugent.rml.Utils;
import be.ugent.rml.records.Record;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JoinConditionFunction {

    private FunctionModel functionModel;
    private Map<String, Object[]> parameters;

    public JoinConditionFunction(FunctionModel functionModel, Map<String, Object[]> parameters) {
        this.functionModel = functionModel;
        this.parameters = parameters;
    }

    public boolean execute(Record child, Record parent) {
        Map <String, Object> filledInParameters = new HashMap<>();

        for (Map.Entry<String, Object[]> entry : this.parameters.entrySet()) {
            List<Template> templates = (List<Template>) entry.getValue()[1];
            String recordType = (String) entry.getValue()[0];

            Record record;

            if (recordType.equals("child")) {
                record = child;
            } else {
                record = parent;
            }

            List<String> objects = Utils.applyTemplate(templates.get(0), record);
            if (objects.size() > 0) {
                filledInParameters.put(entry.getKey(), objects.get(0));
            } else {
                // TODO check whether key is actually optional!
                filledInParameters.put(entry.getKey(), null);
            }
        }

        List<String> results = this.functionModel.execute(filledInParameters);

        return !results.isEmpty() && results.get(0).equals("true");
    }
}
