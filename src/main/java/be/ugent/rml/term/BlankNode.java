package be.ugent.rml.term;

public class BlankNode extends AbstractTerm {

    public BlankNode(String suffix) {
        super(suffix);
    }

    @Override
    public String toString() {
        return "_:" + this.getValue();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof BlankNode) {
            return o.toString().equals(toString());
        } else {
            return false;
        }
    }
}
