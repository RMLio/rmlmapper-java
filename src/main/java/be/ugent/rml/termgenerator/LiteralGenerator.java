package be.ugent.rml.termgenerator;

import be.ugent.idlab.knows.dataio.record.Record;
import be.ugent.rml.Utils;
import be.ugent.rml.extractor.ReferenceExtractor;
import be.ugent.rml.functions.FunctionUtils;
import be.ugent.rml.functions.SingleRecordFunctionExecutor;
import be.ugent.rml.term.Literal;
import be.ugent.rml.term.NamedNode;
import be.ugent.rml.term.Term;

import be.ugent.rml.NAMESPACES;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static be.ugent.rml.Utils.isValidrrLanguage;

public class LiteralGenerator extends TermGenerator {

    // The executor used to get the language for the literal.
    private final SingleRecordFunctionExecutor languageExecutor;
    // The URL of the datatype used for the literal.
    private final Term datatype;
    private final int maxNumberOfTerms;
    // Whether a reference gives its natural RDF literal (RML-Core): a JSON number
    // or boolean is typed xsd:integer, xsd:double or xsd:boolean. Not in the
    // legacy RML vocabulary, whose references give simple literals.
    private final boolean naturalDatatypes;

    private LiteralGenerator(SingleRecordFunctionExecutor functionExecutor, SingleRecordFunctionExecutor languageExecutor, Term datatype, int maxNumberOfTerms, boolean naturalDatatypes) {
        super(functionExecutor);
        this.languageExecutor = languageExecutor;
        this.datatype = datatype;
        this.maxNumberOfTerms = maxNumberOfTerms;
        this.naturalDatatypes = naturalDatatypes;
    }

    private LiteralGenerator(SingleRecordFunctionExecutor functionExecutor, SingleRecordFunctionExecutor languageExecutor, Term datatype, int maxNumberOfTerms) {
        this(functionExecutor, languageExecutor, datatype, maxNumberOfTerms, false);
    }

    public LiteralGenerator(SingleRecordFunctionExecutor functionExecutor, boolean naturalDatatypes) {
        this(functionExecutor, null, null, 0, naturalDatatypes);
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
        List<Term> objects = new ArrayList<>();
        Object result = this.functionExecutor.execute(record);
        List<String> objectStrings = FunctionUtils.functionObjectToList(result);
        List<String> naturalTypes = this.naturalDatatypes && this.functionExecutor instanceof ReferenceExtractor
                ? naturalDatatypesOf(result) : null;

        String dataTypeSource = null;
        if (this.functionExecutor instanceof ReferenceExtractor) {
            dataTypeSource = record.getDataType(((ReferenceExtractor) this.functionExecutor).reference);
        }

        if (!objectStrings.isEmpty()) {
            //add language tag if present
            String finalDataTypeSource = dataTypeSource;
            for (int i = 0; i < objectStrings.size(); i++) {
                String objectString = objectStrings.get(i);
                String naturalType = naturalTypes != null && naturalTypes.size() == objectStrings.size() ? naturalTypes.get(i) : null;
                if (languageExecutor != null) {
                    try {
                        List<String> languages = FunctionUtils.functionObjectToList(this.languageExecutor.execute(record));

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
                } else if (naturalType != null) {
                    // the canonical form of an xsd:double; an integer or a boolean is already canonical
                    String lexical = naturalType.equals(NAMESPACES.XSD + "double")
                            ? Utils.transformDatatypeString(objectString, naturalType) : objectString;
                    objects.add(new Literal(lexical, new NamedNode(naturalType)));
                } else {
                    objects.add(new Literal(objectString));
                }
            }
        }

        if (maxNumberOfTerms != 0) {
            return objects.subList(0, maxNumberOfTerms);

        } else {
            return objects;
        }
    }

    /**
     * The natural datatype of each value of a reference, in the order of
     * FunctionUtils.functionObjectToList: xsd:integer for an integer number,
     * xsd:double for another number, xsd:boolean for a boolean, null for a string.
     */
    private static List<String> naturalDatatypesOf(Object o) {
        List<String> result = new ArrayList<>();
        if (o instanceof Iterable<?>) {
            ((Iterable<?>) o).forEach(item -> result.addAll(naturalDatatypesOf(item)));
        } else if (o instanceof Object[]) {
            for (Object item : (Object[]) o) {
                result.addAll(naturalDatatypesOf(item));
            }
        } else if (o != null) {
            result.add(naturalDatatypeOf(o));
        }
        return result;
    }

    private static String naturalDatatypeOf(Object value) {
        if (value instanceof Boolean) {
            return NAMESPACES.XSD + "boolean";
        }
        if (value instanceof Integer || value instanceof Long || value instanceof Short
                || value instanceof Byte || value instanceof BigInteger) {
            return NAMESPACES.XSD + "integer";
        }
        if (value instanceof Number) {
            return NAMESPACES.XSD + "double";
        }
        return null;
    }
}
