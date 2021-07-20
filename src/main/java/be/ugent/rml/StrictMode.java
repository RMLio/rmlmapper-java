package be.ugent.rml;

/**
 * Indicates how the executor will handle IRI-unsafe values.
 */
public enum StrictMode {
    /**
     * The executor will attempt to skip records that cannot be mapped to IRI-safe values.
     */
    BEST_EFFORT,
    /**
     * The executor will throw an exception when a record cannot be mapped to an IRI-safe triple.
     */
    STRICT,
}
