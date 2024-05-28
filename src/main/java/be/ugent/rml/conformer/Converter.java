package be.ugent.rml.conformer;

import java.util.Map;

/**
 * Interface for converters of a mapping format to the RML mapping format. Used by MappingConformer
 * to convert to RML if needed.
 */
interface Converter {
    /**
     * Try to convert R2RML and old RML to new RML.
     * Has to be atomic. Original model must be recovered if conversion fails.
     */
    void convert(Map<String, String> mappingOptions) throws Exception;
}
