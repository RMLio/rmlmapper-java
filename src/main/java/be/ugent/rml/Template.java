package be.ugent.rml;

import java.util.ArrayList;
import java.util.List;

public class Template {
    private List<TemplateElement> templateElements;

    public Template() {
        this.templateElements = new ArrayList<>();
    }

    public Template(List<TemplateElement> templateElements) {
        this.templateElements = templateElements;
    }

    public List<TemplateElement> getTemplateElements() {
        return templateElements;
    }

    public void addElement(TemplateElement templateElement) {
        this.templateElements.add(templateElement);
    }

    public int countVariables() {
        int counter = 0;

        for (TemplateElement aTemplate : this.templateElements) {
            if (aTemplate.getType() == TEMPLATETYPE.VARIABLE) {
                counter++;
            }
        }

        return counter;
    }
}
