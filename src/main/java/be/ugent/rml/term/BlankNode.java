package be.ugent.rml.term;

import be.ugent.rml.Utils;
import org.eclipse.rdf4j.model.impl.SimpleBNode;

public class BlankNode extends SimpleBNode implements Term {

    public BlankNode(String suffix) {
        super(suffix);
    }

    public BlankNode() { this(Utils.randomString(10)); }


    @Override
    public String getValue() {
        return this.stringValue();
    }
}
