package be.ugent.rml.extractor;

import be.ugent.idlab.knows.dataio.record.Record;
import be.ugent.rml.functions.SingleRecordFunctionExecutor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ConstantExtractor implements Extractor, SingleRecordFunctionExecutor {

    private final String constant;

    public ConstantExtractor(String constant) {
        this.constant = constant;
    }

    @Override
    public List<Object> extract(Record source) {
        ArrayList<Object> result = new ArrayList<>();
        result.add(constant);

        return result;
    }

    @Override
    public Object execute(Record record) throws IOException {
        return extract(record);
    }

    /**
     * to String method
     *
     * @return string
     */
    @Override
    public String toString() {
        return "\"" + constant + '\"';
    }
}
