package be.ugent.rml.functions;

import be.ugent.rml.Element;
import be.ugent.rml.Utils;
import be.ugent.rml.records.Record;

import java.util.List;
import java.util.Map;

public class ApplyTemplateFunction extends Function{

    private boolean encodeURI;

    public ApplyTemplateFunction(boolean encodeURI) {
        super(null);

        this.encodeURI = encodeURI;
    }

    public ApplyTemplateFunction() {
        this(false);
    }

    @Override
    public List<?> execute(Record record, Map<String, List<List<Element>>> parameters) {
        return Utils.applyTemplate(parameters.get("_TEMPLATE").get(0), record, encodeURI);
    }
}
