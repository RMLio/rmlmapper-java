package be.ugent.rml.functions;

import be.ugent.rml.Utils;
import be.ugent.rml.extractor.ConstantExtractor;
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
       return concat(record);
    }

    private List<String> concat(Record record) {
        ArrayList<String> results = new ArrayList<>();
        results.add("");

        //we only return a result when all elements of the template are found
        boolean allValuesFound = true;
        int referenceCount = 0;
        String onlyConstants = "";

        //we iterate over all elements of the template, unless one is not found
        for (int i = 0; allValuesFound && i < extractors.size(); i++) {
            Extractor extractor = extractors.get(i);

            List<String> extractedValues = new ArrayList<>();
            FunctionUtils.functionObjectToList(extractor.extract(record), extractedValues);

            if (!extractedValues.isEmpty()) {
                ArrayList<String> temp = new ArrayList<>();

                for (int k = 0; k < results.size(); k ++) {

                    for (int j = 0; j < extractedValues.size(); j ++) {
                        String result = results.get(k);
                        String value = extractedValues.get(j);

                        if (encodeURI && extractor instanceof ReferenceExtractor) {
                            value = Utils.encodeURI(value);
                        }

                        result += value;

                        if (extractor instanceof ConstantExtractor) {
                            onlyConstants += value;
                        }

                        temp.add(result);
                    }

                    if (extractor instanceof ReferenceExtractor) {
                        referenceCount ++;
                    }
                }

                results = temp;
            }

            if (extractedValues.isEmpty()) {
                logger.warn("Not all values for a template where found. More specific, the variable " + extractor + " did not provide any results.");
                allValuesFound = false;
            }
        }

        if ((allValuesFound && referenceCount > 0 && results.contains(onlyConstants)) || !allValuesFound) {
            results = new ArrayList<>();
        }

        return results;
    }
}
