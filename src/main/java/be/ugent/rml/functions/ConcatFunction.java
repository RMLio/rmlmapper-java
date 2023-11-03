package be.ugent.rml.functions;

import be.ugent.idlab.knows.dataio.record.Record;
import be.ugent.rml.Utils;
import be.ugent.rml.extractor.ConstantExtractor;
import be.ugent.rml.extractor.Extractor;
import be.ugent.rml.extractor.ReferenceExtractor;
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

    @Override
    public List<?> execute(Record record) {
       return concat(record);
    }

    private List<String> concat(Record record) {
        List<String> results = new ArrayList<>();
        results.add("");

        //we only return a result when all elements of the template are found
        boolean allValuesFound = true;
        int referenceCount = 0;
        StringBuilder onlyConstants = new StringBuilder();

        //we iterate over all elements of the template, unless one is not found
        for (int i = 0; allValuesFound && i < extractors.size(); i++) {
            Extractor extractor = extractors.get(i);
            final boolean isReferenceExtractor = extractor instanceof ReferenceExtractor;
            final boolean isConstantExtractor = extractor instanceof ConstantExtractor;
            List<String> extractedValues = FunctionUtils.functionObjectToList(extractor.extract(record));

            if (!extractedValues.isEmpty()) {
                List<String> temp = new ArrayList<>();

                for (String result : results) {
                    for (String value : extractedValues) {
                        if (isReferenceExtractor) {
                            if (encodeURI)
                                value = Utils.encodeURI(value);
                            referenceCount ++;
                        } else if (isConstantExtractor) {
                            onlyConstants.append(value);
                        }

                        temp.add(result + value);
                    }
                }

                results = temp;
            }

            if (extractedValues.isEmpty()) {
                logger.warn("Not all values for a template where found. More specific, the variable {} did not provide any results.", extractor);
                allValuesFound = false;
            }
        }

        if (!allValuesFound || (referenceCount > 0 && results.contains(onlyConstants.toString())))
            return new ArrayList<>();

        return results;
    }
}
