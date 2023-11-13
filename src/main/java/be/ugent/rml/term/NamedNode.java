package be.ugent.rml.term;

import org.eclipse.rdf4j.model.impl.SimpleIRI;

public class NamedNode extends SimpleIRI implements Term{

    public NamedNode(final String iri) {
        super(iri);
    }


    @Override
    public boolean equals(Object o) {
        if (o instanceof NamedNode) {
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
