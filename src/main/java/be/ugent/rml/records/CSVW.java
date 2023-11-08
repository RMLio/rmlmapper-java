package be.ugent.rml.records;

import be.ugent.idlab.knows.dataio.access.Access;
import be.ugent.idlab.knows.dataio.iterators.CSVWSourceIterator;
import be.ugent.idlab.knows.dataio.iterators.csvw.CSVWConfiguration;
import be.ugent.idlab.knows.dataio.iterators.csvw.CSVWConfigurationBuilder;
import be.ugent.idlab.knows.dataio.record.Record;
import be.ugent.rml.NAMESPACES;
import be.ugent.rml.Utils;
import be.ugent.rml.store.QuadStore;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class has as main goal to create a CSVParser for a Logical Source with CSVW.
 */
public class CSVW {

    private static final ValueFactory valueFactory = SimpleValueFactory.getInstance();

    private final QuadStore rmlStore;
    private final Value logicalSource;

    CSVW(QuadStore rmlStore, Value logicalSource) {
        this.rmlStore = rmlStore;
        this.logicalSource = logicalSource;
    }

    /**
     * Read the records from the given Access
     *
     * @param access The access containing the records
     * @return The list of records in the Access
     */
    List<Record> getRecords(Access access) throws Exception {
        List<Value> sources = Utils.getObjectsFromQuads(this.rmlStore.getQuads(this.logicalSource, valueFactory.createIRI(NAMESPACES.RML + "source"), null));
        Value source = sources.get(0);

        CSVWConfiguration config = getConfiguration(source);
        List<Record> records = new ArrayList<>();
        try (CSVWSourceIterator iterator = new CSVWSourceIterator(access, config)) {
            iterator.forEachRemaining(records::add);
        }

        return records;
    }

    private CSVWConfiguration getConfiguration(Value logicalSource) {
        CSVWConfigurationBuilder configBuilder = CSVWConfiguration.builder();

        configBuilder = setOptionList(logicalSource, "null", configBuilder, CSVWConfigurationBuilder::withNulls);

        // extract data from dialect
        List<Value> dialectTerms = Utils.getObjectsFromQuads(rmlStore.getQuads(logicalSource, valueFactory.createIRI(NAMESPACES.CSVW + "dialect"), null));
        if (!dialectTerms.isEmpty()) {
            Value dialect = dialectTerms.get(0);
            configBuilder = setDialectOptions(dialect, configBuilder);
        }

        return configBuilder.build();
    }

    private CSVWConfigurationBuilder setDialectOptions(Value dialect, CSVWConfigurationBuilder configBuilder) {
        configBuilder = setOptionString(dialect, "commentPrefix", configBuilder, CSVWConfigurationBuilder::withCommentPrefix);

        configBuilder = setOptionChar(dialect, "delimiter", configBuilder, CSVWConfigurationBuilder::withDelimiter);

        configBuilder = setOptionChar(dialect, "doubleQuote", configBuilder, CSVWConfigurationBuilder::withEscapeCharacter);

        configBuilder = setOptionString(dialect, "trim", configBuilder, CSVWConfigurationBuilder::withTrim);

        configBuilder = setOptionChar(dialect, "quoteChar", configBuilder, CSVWConfigurationBuilder::withQuoteCharacter);

        configBuilder = setOptionString(dialect, "encoding", configBuilder, CSVWConfigurationBuilder::withEncoding);

        return configBuilder;
    }

    /**
     * Sets an option in CSVWConfigurationBuilder that expects a string
     *
     * @param dialect Term containing the dialect
     * @param option  option to read form dialect
     * @param builder CSVWConfigurationBuilder to set the option in
     * @param setter  method of CSVWConfigurationBuilder to call
     * @return a CSVWConfigurationBuilder with the option set if the option is present in the dialect, otherwise the original CSVWConfigurationBuilder is returned
     */
    private CSVWConfigurationBuilder setOptionString(Value dialect, String option, CSVWConfigurationBuilder builder, StringOptionSetter setter) {
        List<Value> optionTerms = Utils.getObjectsFromQuads(this.rmlStore.getQuads(dialect, valueFactory.createIRI(NAMESPACES.CSVW + option), null));
        if (!optionTerms.isEmpty()) {
            builder = setter.call(builder, optionTerms.get(0).stringValue());
        }

        return builder;
    }

    /**
     * Sets an option in CSVWConfigurationBuilder that expects a character
     *
     * @param term    Term containing the option
     * @param option  option to read form term
     * @param builder CSVWConfigurationBuilder to set the option in
     * @param setter  method of CSVWConfigurationBuilder to call
     * @return a CSVWConfigurationBuilder with the option set if the option is present in the term, otherwise the original CSVWConfigurationBuilder is returned
     */
    private CSVWConfigurationBuilder setOptionChar(Value term, String option, CSVWConfigurationBuilder builder, CharacterOptionSetter setter) {
        List<Value> optionTerms = Utils.getObjectsFromQuads(this.rmlStore.getQuads(term, valueFactory.createIRI(NAMESPACES.CSVW + option), null));
        if (!optionTerms.isEmpty()) {
            builder = setter.call(builder, optionTerms.get(0).stringValue().charAt(0));
        }

        return builder;
    }

    private CSVWConfigurationBuilder setOptionList(Value term, String option, CSVWConfigurationBuilder builder, ListOptionSetter setter) {
        List<Value> optionTerms = Utils.getObjectsFromQuads(this.rmlStore.getQuads(term, valueFactory.createIRI(NAMESPACES.CSVW + option), null));
        if (!optionTerms.isEmpty()) {
            List<String> nulls = optionTerms.stream().map(Value::stringValue).collect(Collectors.toList());
            builder = setter.call(builder, nulls);
        }

        return builder;
    }

    /**
     * Functional interface to set a value of CSVWConfigurationBuilder that expects a string.
     */
    private interface StringOptionSetter {
        CSVWConfigurationBuilder call(CSVWConfigurationBuilder builder, String value);
    }

    /**
     * Functional interface to set a value of CSVWConfigurationBuilder that expects a character.
     */
    private interface CharacterOptionSetter {
        CSVWConfigurationBuilder call(CSVWConfigurationBuilder builder, Character value);
    }

    private interface ListOptionSetter<T> {
        CSVWConfigurationBuilder call(CSVWConfigurationBuilder builder, List<T> values);
    }
}
