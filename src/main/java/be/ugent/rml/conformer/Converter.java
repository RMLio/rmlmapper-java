package be.ugent.rml.conformer;

import be.ugent.rml.term.Term;

import java.util.Map;

/**
 * Interface for converters of a mapping format to the RML mapping format. Used by MappingConformer
 * to convert to RML if needed.
 */
interface Converter {
    /**
     * Detection logic needed to determine mapping format of TriplesMap.
     * @param tm TriplesMap
     * @return true if is specific mapping format false if not
     */
    boolean detect(Term tm);

    /**
     * Try to convert mapping language TriplesMap to RML.
     * Has to be atomic. Original model must be recovered if conversion fails.
     * @param tm TriplesMap
     */
    void convert(Term tm, Map<String, String> mappingOptions) throws Exception;
}
