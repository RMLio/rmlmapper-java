package be.ugent.rml.extractor;

import be.ugent.rml.functions.SingleRecordFunctionExecutor;
import be.ugent.rml.records.Record;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ConstantExtractor implements Extractor, SingleRecordFunctionExecutor {

    private final Object constant;
    private final List<Object> constantList;

    public ConstantExtractor(String constant) {
        List<Object> c = new ArrayList<>();
        c.add(constant);
        this.constantList = c;
        this.constant = c;
    }

    @Override
    public List<Object> extract(Record record) {
        return this.constantList;
    }

    @Override
    public Object execute(Record record) throws IOException {
        return this.constant;
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
