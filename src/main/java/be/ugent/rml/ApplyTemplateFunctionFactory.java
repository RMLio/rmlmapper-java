package be.ugent.rml;

import be.ugent.rml.functions.ApplyTemplateFunction;
import be.ugent.rml.functions.SingleRecordFunctionExecutor;
import be.ugent.rml.functions.StaticSingleRecordFunctionExecutor;
import be.ugent.rml.term.NamedNode;
import be.ugent.rml.term.Term;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ApplyTemplateFunctionFactory {

    static SingleRecordFunctionExecutor generate(String genericTemplate, Term termType) {
        return ApplyTemplateFunctionFactory.generate(genericTemplate, termType, false);
    }

    static SingleRecordFunctionExecutor generate(String genericTemplate, Term termType, boolean unnestCollections) {
        return ApplyTemplateFunctionFactory.generate(genericTemplate, termType.equals(new NamedNode(NAMESPACES.RR + "IRI")), unnestCollections);
    }

    static SingleRecordFunctionExecutor generate(String genericTemplate, boolean encodeURI) {
        return ApplyTemplateFunctionFactory.generate(genericTemplate, encodeURI, false);
    }

    static SingleRecordFunctionExecutor generate(String genericTemplate, boolean encodeURI, boolean unnestCollections) {
        HashMap<String, List<Template>> parameters = new HashMap<>();
        ArrayList<Template> temp = new ArrayList<>();
        temp.add(Utils.parseTemplate(genericTemplate));
        parameters.put("_TEMPLATE", temp);
        return new ApplyTemplateFunction(parameters, encodeURI, unnestCollections);
    }

    static SingleRecordFunctionExecutor generateWithConstantValue(String value) {
        HashMap<String, List<Template>> parameters = new HashMap<>();
        List<Template> temp = new ArrayList<>();
        Template temp2 = new Template();
        temp2.addElement(new TemplateElement(value, TEMPLATETYPE.CONSTANT));
        temp.add(temp2);

        parameters.put("_TEMPLATE", temp);

        return new ApplyTemplateFunction(parameters);
    }
}
