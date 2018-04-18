package be.ugent.rml;

public class Element {

    private String value;
    private String type;

    public Element(String value, String type) {
        this.value = value;
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public String getType() {
        return type;
    }
}
