package be.ugent.rml;

public class TemplateElement {

    private String value;
    private TEMPLATETYPE type;

    public TemplateElement(String value, TEMPLATETYPE type) {
        this.value = value;
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public TEMPLATETYPE getType() {
        return type;
    }
}
