package be.ugent.rml.termgenerator;

import be.ugent.rml.functions.FunctionExecutor;
import be.ugent.rml.records.Record;
import be.ugent.rml.term.Literal;
import be.ugent.rml.term.Term;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LiteralGenerator extends TermGenerator {

    private String language;
    private Term datatype;

    private LiteralGenerator(FunctionExecutor functionExecutor, String language, Term datatype) {
        super(functionExecutor);
        this.language = language;
        this.datatype = datatype;
    }

    public LiteralGenerator(FunctionExecutor functionExecutor, String language) {
        this(functionExecutor, language, null);
    }

    public LiteralGenerator(FunctionExecutor functionExecutor, Term datatype) {
        this(functionExecutor, null, datatype);
    }

    public LiteralGenerator(FunctionExecutor functionExecutor) {
        this(functionExecutor, null, null);
    }

    @Override
    public List<Term> generate(Record record) throws IOException {
        ArrayList<Term> objects = new ArrayList<>();
        List<String> objectStrings = (List<String>) this.functionExecutor.execute(record);

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
