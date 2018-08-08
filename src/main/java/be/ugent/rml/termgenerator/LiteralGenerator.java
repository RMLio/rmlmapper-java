package be.ugent.rml.termgenerator;

import be.ugent.rml.functions.Function;
import be.ugent.rml.records.Record;
import be.ugent.rml.term.Literal;
import be.ugent.rml.term.Term;

import java.util.ArrayList;
import java.util.List;

public class LiteralGenerator extends TermGenerator {

    private String language;
    private Term datatype;

    private LiteralGenerator(Function fn, String language, Term datatype) {
        super(fn);
        this.language = language;
        this.datatype = datatype;
    }

    public LiteralGenerator(Function fn, String language) {
        this(fn, language, null);
    }

    public LiteralGenerator(Function fn, Term datatype) {
        this(fn, null, datatype);
    }

    public LiteralGenerator(Function fn) {
        this(fn, null, null);
    }

    @Override
    public List<Term> generate(Record record) {
        ArrayList<Term> objects = new ArrayList<>();
        List<String> objectStrings = (List<String>) this.fn.execute(record);

        if (objectStrings.size() > 0) {
            //add language tag if present
            objectStrings.forEach(objectString -> {
                if (language != null) {
                    objects.add(new Literal(objectString, language));
                } else if (datatype != null) {
                    //add datatype if present; language and datatype can't be combined because the language tag implies langString as datatype
                    objects.add(new Literal(objectString, datatype));
                } else {
                    objects.add(new Literal(objectString));
                }
            });
        }

        return objects;
    }
}
