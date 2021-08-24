package be.ugent.rml.termgenerator;

import be.ugent.rml.Utils;
import be.ugent.rml.extractor.ConstantExtractor;
import be.ugent.rml.extractor.ReferenceExtractor;
import be.ugent.rml.functions.FunctionUtils;
import be.ugent.rml.functions.SingleRecordFunctionExecutor;
import be.ugent.rml.records.Record;
import be.ugent.rml.term.Literal;
import be.ugent.rml.term.NamedNode;
import be.ugent.rml.term.Term;

import java.util.ArrayList;
import java.util.List;

import static be.ugent.rml.Utils.isValidrrLanguage;

public class LiteralGenerator extends TermGenerator {

    // The executor used to get the language for the literal.
    private SingleRecordFunctionExecutor languageExecutor;
    // The URL of the datatype used for the literal.
    private Term datatype;
    private int maxNumberOfTerms;

    private LiteralGenerator(SingleRecordFunctionExecutor functionExecutor, SingleRecordFunctionExecutor languageExecutor, Term datatype, int maxNumberOfTerms) {
        super(functionExecutor);
        this.languageExecutor = languageExecutor;
        this.datatype = datatype;
        this.maxNumberOfTerms = maxNumberOfTerms;
    }

    public LiteralGenerator(SingleRecordFunctionExecutor functionExecutor, String language) {
        this(functionExecutor, new ConstantExtractor(language));
    }

    public LiteralGenerator(SingleRecordFunctionExecutor functionExecutor, SingleRecordFunctionExecutor languageExecutor) {
        this(functionExecutor, languageExecutor, null, 0);
    }

    public LiteralGenerator(SingleRecordFunctionExecutor functionExecutor, Term datatype) {
        this(functionExecutor, null, datatype, 0);
    }

    public LiteralGenerator(SingleRecordFunctionExecutor functionExecutor) {
        this(functionExecutor, null, null, 0);
    }

    @Override
    public List<Term> generate(Record record) throws Exception {
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
                if (languageExecutor != null) {
                    try {
                        ArrayList<String> languages = new ArrayList<>();
                        FunctionUtils.functionObjectToList(this.languageExecutor.execute(record), languages);

                        if (!languages.isEmpty()) {
                            String language = languages.get(0);

                            if (! isValidrrLanguage(language)) {
                                throw new RuntimeException(String.format("Language tag \"%s\" does not conform to BCP 47 standards", language));
                            }

                            objects.add(new Literal(objectString, language));
                        }
                    } catch (Exception e) {
                        // TODO print error message
                        e.printStackTrace();
                    }
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
