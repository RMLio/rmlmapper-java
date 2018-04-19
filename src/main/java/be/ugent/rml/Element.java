package be.ugent.rml;

public class Element {

    private String value;
    private TEMPLATETYPE type;

    public Element(String value, TEMPLATETYPE type) {
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
