package be.ugent.rml.term;

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

    @Override
    public String toString() { return this.value; }
}
