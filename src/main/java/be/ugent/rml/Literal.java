package be.ugent.rml;

public class Literal extends AbstractTerm {

    private String language;
    private Term datatype;

    public Literal(String value) {
        super(value);
    }

    public Literal(String value, String language) {
        this(value);

        this.language = language;
    }

    public Literal(String value, Term datatype) {
        this(value);

        this.datatype = datatype;
    }

    @Override
    public String toString() {
        String temp = "\"" + this.getValue() + "\"";

        if (this.language != null && !this.language.equals("")) {
            temp += "@" + this.language;
        } else if (this.datatype != null) {
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
}
