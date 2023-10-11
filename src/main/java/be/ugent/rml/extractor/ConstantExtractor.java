package be.ugent.rml.extractor;

import be.ugent.idlab.knows.functions.agent.functionModelProvider.fno.NAMESPACES;
import be.ugent.rml.functions.SingleRecordFunctionExecutor;
import be.ugent.rml.records.Record;

import java.io.IOException;
import java.util.List;

public class ConstantExtractor implements Extractor, SingleRecordFunctionExecutor {

    private final String constant;
    private final List<Object> constantList;

    private final boolean needsMagic;

    public ConstantExtractor(String constant) {
        this. constantList = List.of(constant);
        this.constant = constant;
        needsMagic = constant.equals(NAMESPACES.IDLABFN + "implicitDelete");
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
     * Returns true id this extractor needs a magic value at the end of the dataset.
     * At this moment only http://example.com/idlab/function/implicitDelete needs one.
     * @return {@code true} if a magic value is required.
     */
    @Override
    public boolean needsMagicEndValue() {
        return needsMagic;
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
