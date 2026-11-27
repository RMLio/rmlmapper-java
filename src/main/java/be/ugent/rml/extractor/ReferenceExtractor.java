package be.ugent.rml.extractor;

import be.ugent.idlab.knows.dataio.record.JSONRecord;
import be.ugent.idlab.knows.dataio.record.Record;
import be.ugent.idlab.knows.dataio.record.RecordValue;
import be.ugent.rml.functions.SingleRecordFunctionExecutor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReferenceExtractor implements Extractor, SingleRecordFunctionExecutor {

    public String reference;
    private final boolean ignoreDoubleQuotes;
    private final boolean strictReferenceResolution;

    public ReferenceExtractor(String reference, boolean ignoreDoubleQuotes, boolean strictReferenceResolution) {
        this.reference = reference;
        this.ignoreDoubleQuotes = ignoreDoubleQuotes;
        this.strictReferenceResolution = strictReferenceResolution;
    }

    @Override
    public List<Object> extract(Record record) {
        String temp = this.reference;

        if (ignoreDoubleQuotes && temp.startsWith("\"") && temp.endsWith("\"")) {
            temp = temp.substring(1, temp.length() - 1);
        }

        if (record instanceof JSONRecord) {
            temp = jsonFieldName(temp);
        }

        RecordValue recordValue = record.get(temp);

        if (recordValue.isOk()) { // This means no error occurred during reference resolving and the value is not a null value
            Object value = recordValue.getValue();
            if (value instanceof Iterable<?>) {
                return new ArrayList<>((Collection<?>) value);
            } else {
                return List.of(value);
            }
        } else if (recordValue.isEmpty() ||  // The record has a null value
                recordValue.isNotFound() && !strictReferenceResolution) {   // The reference has not been found (e.g. nu field with that name)
            return List.of();
        } else {
            throw new IllegalArgumentException(recordValue.getMessage());
        }
    }

    // the bracket notation of a field of the current JSON object: $['Country Code']
    private static final Pattern JSON_ROOT_BRACKET = Pattern.compile("^\\$\\.?\\[(['\"])(.+)\\1\\]$");

    /**
     * A JSONPath reference in the bracket notation of a field of the iterated object,
     * $['Country Code'] (RML-Core RMLTC0010a-c), is the name of that field, which the
     * JSON record resolves (and quotes, for a name with a space) itself.
     */
    static String jsonFieldName(String reference) {
        Matcher matcher = JSON_ROOT_BRACKET.matcher(reference);
        return matcher.matches() ? matcher.group(2) : reference;
    }

    @Override
    public String toString() {
        return "ReferenceExecutor that works with " + reference;
    }

    @Override
    public Object execute(Record record) throws IOException {
        return extract(record);
    }

    public String getReference(){
        return this.reference;
    }
}
