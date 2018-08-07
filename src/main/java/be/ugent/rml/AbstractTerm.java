package be.ugent.rml;

public class AbstractTerm implements Term {

    private String value;

    public AbstractTerm(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
