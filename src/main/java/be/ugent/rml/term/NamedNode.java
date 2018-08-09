package be.ugent.rml.term;

public class NamedNode extends AbstractTerm {

    public NamedNode(String iri) {
        super(iri);
    }

    @Override
    public String toString() {
        return "<" + this.getValue() + ">";
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof NamedNode) {
            return o.toString().equals(toString());
        } else {
            return false;
        }
    }
}
