package be.ugent.rml.term;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleLiteral;

import java.util.Optional;

public class Literal extends SimpleLiteral implements Term {

    private Term datatype;

    public Literal(String value) {
        super(value);
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
    public String toString() {
        String temp = "\"" + escapeValue(this.getValue()) + "\"";

        if (getLanguage().isPresent() && !getLanguage().get().isEmpty()) {
            temp += "@" + getLanguage().get();
        } else if (this.datatype != null && ! this.datatype.getValue().equals("http://www.w3.org/2001/XMLSchema#string")) {
            // https://www.w3.org/TR/turtle/#h4_turtle-literals
            // > If there is no datatype IRI and no language tag, the datatype is xsd:string.
            temp += "^^" + this.datatype;
        }

        return temp;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Literal) {
            return o.toString().equals(toString());
        } else {
            return false;
        }
    }

    /**
     * Escapes a Unicode string to an N-Triples compatible character sequence. Any special characters are
     * escaped using backslashes (<tt>"</tt> becomes <tt>\"</tt>, etc.), and non-ascii/non-printable
     * characters are escaped using Unicode escapes (<tt>&#x5C;uxxxx</tt> and <tt>&#x5C;Uxxxxxxxx</tt>) if the
     * option is selected.
     */
    private String escapeValue(String label) {
        String result = "";

        for (int i = 0; i < label.length(); i++) {
            char c = label.charAt(i);

            if (c == '\\') {
                result += "\\\\";
            } else if (c == '"') {
                result += "\\\"";
            } else if (c == '\n') {
                result += "\\n";
            } else if (c == '\r') {
                result += "\\r";
            } else if (c == '\t') {
                result += "\\t";
            } else {
                result += c;
            }
        }

        return result;
    }

    @Override
    public String getValue() {
        return this.stringValue();
    }
}
