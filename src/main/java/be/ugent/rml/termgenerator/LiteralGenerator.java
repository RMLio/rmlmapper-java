package be.ugent.rml.termgenerator;

import be.ugent.rml.Utils;
import be.ugent.rml.extractor.ReferenceExtractor;
import be.ugent.rml.functions.FunctionUtils;
import be.ugent.rml.functions.SingleRecordFunctionExecutor;
import be.ugent.rml.records.Record;
import be.ugent.rml.term.Literal;
import be.ugent.rml.term.NamedNode;
import be.ugent.rml.term.Term;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LiteralGenerator extends TermGenerator {

    private String language;
    private Term datatype;
    private int maxNumberOfTerms;

    private LiteralGenerator(SingleRecordFunctionExecutor functionExecutor, String language, Term datatype, int maxNumberOfTerms) {
        super(functionExecutor);
        this.language = language;
        this.datatype = datatype;
        this.maxNumberOfTerms = maxNumberOfTerms;
    }

    public LiteralGenerator(SingleRecordFunctionExecutor functionExecutor, String language) {
        this(functionExecutor, language, null, 0);
    }

    public LiteralGenerator(SingleRecordFunctionExecutor functionExecutor, Term datatype) {
        this(functionExecutor, null, datatype, 0);
    }

    public LiteralGenerator(SingleRecordFunctionExecutor functionExecutor) {
        this(functionExecutor, null, null, 0);
    }

    @Override
    public List<Term> generate(Record record) throws IOException {
        ArrayList<Term> objects = new ArrayList<>();
        ArrayList<String> objectStrings = new ArrayList<>();
        FunctionUtils.functionObjectToList(this.functionExecutor.execute(record), objectStrings);

        String dataTypeSource = null;
        if (this.functionExecutor instanceof ReferenceExtractor) {
            dataTypeSource = record.getDataType(((ReferenceExtractor) this.functionExecutor).reference);
        }

        if (objectStrings.size() > 0) {
            //add language tag if present
            String finalDataTypeSource = dataTypeSource;
            objectStrings.forEach(objectString -> {
                if (language != null) {
                    objects.add(new Literal(objectString, language));
                } else if (datatype != null) {
                    //add datatype if present; language and datatype can't be combined because the language tag implies langString as datatype
                    objects.add(new Literal(objectString, datatype));
                } else if (finalDataTypeSource != null) {
                    if (this.functionExecutor instanceof ReferenceExtractor) {
                        objectString = Utils.transformDatatypeString(objectString, finalDataTypeSource);
                    }
                    objects.add(new Literal(objectString, new NamedNode(finalDataTypeSource)));
                } else {
                    objects.add(new Literal(objectString));
                }
            });
        }

        if (maxNumberOfTerms != 0) {
            return objects.subList(0, maxNumberOfTerms);

        } else {
            return objects;
        }
    }
}
