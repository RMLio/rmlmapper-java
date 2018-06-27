package be.ugent.rml.functions;

import be.ugent.rml.Template;
import be.ugent.rml.Utils;
import be.ugent.rml.records.Record;

import java.util.List;
import java.util.Map;

public class ApplyTemplateFunction extends Function{

    private boolean encodeURI;

    public ApplyTemplateFunction(Map<String, List<Template>> parameters, boolean encodeURI) {
        super(null, parameters);

        this.encodeURI = encodeURI;
    }

    public ApplyTemplateFunction(Map<String, List<Template>> parameters) {
        this(parameters, false);
    }

    @Override
    public List<?> execute(Record record) {
        return Utils.applyTemplate(this.parameters.get("_TEMPLATE").get(0), record, encodeURI);
    }
}
