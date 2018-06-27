package be.ugent.rml;

import java.util.ArrayList;
import java.util.List;

public class Template {
    private List<Element> elements;

    public Template() {
        this.elements = new ArrayList<>();
    }

    public Template(List<Element> elements) {
        this.elements = elements;
    }

    public List<Element> getElements() {
        return elements;
    }

    public void addElement(Element element) {
        this.elements.add(element);
    }

    public int countVariables() {
        int counter = 0;

        for (Element aTemplate : this.elements) {
            if (aTemplate.getType() == TEMPLATETYPE.VARIABLE) {
                counter++;
            }
        }

        return counter;
    }
}
