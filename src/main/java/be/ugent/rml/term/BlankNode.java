package be.ugent.rml.term;

import org.eclipse.rdf4j.model.impl.SimpleBNode;

public class BlankNode extends SimpleBNode implements Term {

    public BlankNode(String suffix) {
        super(suffix);
    }

    public BlankNode() { super(); }

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

    @Override
    public String getValue() {
        return this.stringValue();
    }
}
