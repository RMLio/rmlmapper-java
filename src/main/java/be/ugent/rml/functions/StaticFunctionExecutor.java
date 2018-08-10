package be.ugent.rml.functions;

import be.ugent.rml.Template;
import be.ugent.rml.Utils;
import be.ugent.rml.records.Record;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StaticFunctionExecutor implements FunctionExecutor{

    private final FunctionModel functionModel;
    protected final Map<String, List<Template>> parameters;

    public StaticFunctionExecutor(FunctionModel model, Map<String, List<Template>> parameters) {
        this.functionModel = model;
        this.parameters = parameters;
    }

    public List<?> execute(Record record) {
        Map <String, Object> filledInParameters = new HashMap<>();
        for (Map.Entry<String, List<Template>> entry : this.parameters.entrySet()) {
            List<String> objects = Utils.applyTemplate(entry.getValue().get(0), record);
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
