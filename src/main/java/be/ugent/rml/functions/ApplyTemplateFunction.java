package be.ugent.rml.functions;

import be.ugent.rml.Template;
import be.ugent.rml.Utils;
import be.ugent.rml.records.Record;

import java.util.List;
import java.util.Map;

public class ApplyTemplateFunction implements SingleRecordFunctionExecutor {

    private boolean encodeURI;
    private boolean unnestCollections;
    private Map<String, List<Template>> parameters;

    public ApplyTemplateFunction(Map<String, List<Template>> parameters, boolean encodeURI, boolean unnestCollections) {
        this.parameters = parameters;
        this.encodeURI = encodeURI;
        this.unnestCollections = unnestCollections;
    }

    public ApplyTemplateFunction(Map<String, List<Template>> parameters) {
        this(parameters, false, false);
    }

    @Override
    public List<?> execute(Record record) {
        return Utils.applyTemplate(this.parameters.get("_TEMPLATE").get(0), record, encodeURI, unnestCollections);
    }
}
