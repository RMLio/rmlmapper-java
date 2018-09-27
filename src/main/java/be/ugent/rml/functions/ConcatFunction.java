package be.ugent.rml.functions;

import be.ugent.rml.Utils;
import be.ugent.rml.extractor.Extractor;
import be.ugent.rml.extractor.ReferenceExtractor;
import be.ugent.rml.records.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class ConcatFunction implements SingleRecordFunctionExecutor {

    private static final Logger logger = LoggerFactory.getLogger(ConcatFunction.class);
    private boolean encodeURI;
    private List<Extractor> extractors;

    public ConcatFunction(List<Extractor> extractors, boolean encodeURI) {
        this.extractors = extractors;
        this.encodeURI = encodeURI;
    }

    public ConcatFunction(List<Extractor> extractors) {
        this(extractors, false);
    }

    @Override
    public List<?> execute(Record record) {
       ArrayList<String> result = new ArrayList<>();
       result.add(concat(record));

       return result;
    }

    private String concat(Record record) {
        String result = "";

        //we only return a result when all elements of the template are found
        boolean allValuesFound = true;
        int referenceCount = 0;
        String onlyConstants = "";

        //we iterate over all elements of the template, unless one is not found
        for (int i = 0; allValuesFound && i < extractors.size(); i++) {
            Extractor extractor = extractors.get(i);

            List<Object> extractedValues = extractor.extract(record);
            Object extractedValue = null;

            if (!extractedValues.isEmpty()) {
                extractedValue = extractedValues.get(0);
            }

            if (extractor instanceof ReferenceExtractor) {
                referenceCount ++;
            } else if (extractedValue != null) {
                onlyConstants += extractedValue.toString();
            }

            if (extractedValue != null) {
                String value = extractedValue.toString();

                if (encodeURI && extractor instanceof ReferenceExtractor) {
                    value = Utils.encodeURI(value);
                }

                result += value;
            }

            if (extractedValue == null) {
                logger.warn("Not all values for a template where found. More specific, the variable " + extractor + " did not provide any results.");
                allValuesFound = false;
            }
        }

        if ((allValuesFound && referenceCount > 0 && result.equals(onlyConstants)) || !allValuesFound) {
            result = null;

        }

        return result;
    }
}
