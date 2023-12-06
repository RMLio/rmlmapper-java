package be.ugent.rml.term;

import be.ugent.rml.NAMESPACES;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleLiteral;

import java.util.Optional;

public class Literal extends SimpleLiteral implements Term {

    private Term datatype;

    public Literal(String value) {
        super(value);
        this.datatype = new NamedNode(NAMESPACES.XSD + "string");
    }

    public Literal(String value, String language) {
        super(value, language);
    }

    public Literal(String value, Term datatype) {
        this(value);

        this.datatype = datatype;
    }

    @Override
    public IRI getDatatype(){
        return (NamedNode) datatype;
    }


    @Override
    public String getValue() {
        return this.stringValue();
    }
}
