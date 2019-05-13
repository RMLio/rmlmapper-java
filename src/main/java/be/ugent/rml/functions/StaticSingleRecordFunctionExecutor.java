package be.ugent.rml.functions;

import be.ugent.rml.Template;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StaticSingleRecordFunctionExecutor extends AbstractSingleRecordFunctionExecutor {

    public StaticSingleRecordFunctionExecutor(FunctionModel model, Map<String, List<Template>> parameters) {
        HashMap<String, Object[]> parametersForOtherExecutor = new HashMap<>();

        parameters.keySet().forEach(parameter -> {
           List<Template> object = parameters.get(parameter);
           Object[] objects = new Object[2];
           objects[0] = "_default";
           objects[1] = object;

           parametersForOtherExecutor.put(parameter, objects);
        });

        functionExecutor = new StaticMultipleRecordsFunctionExecutor(model, parametersForOtherExecutor);
    }
}
