package be.ugent.rml.cli;

import be.ugent.idlab.knows.functions.agent.Agent;
import be.ugent.idlab.knows.functions.agent.AgentFactory;
import be.ugent.knows.idlabFunctions.IDLabFunctions;
import be.ugent.rml.Executor;
import be.ugent.rml.StrictMode;
import be.ugent.rml.Utils;
import be.ugent.rml.metadata.MetadataGenerator;
import be.ugent.rml.records.RecordsFactory;
import be.ugent.rml.store.Quad;
import be.ugent.rml.store.QuadStore;
import be.ugent.rml.store.RDF4JStore;
import be.ugent.rml.target.Target;
import be.ugent.rml.target.TargetFactory;
import be.ugent.rml.term.NamedNode;
import be.ugent.rml.term.Term;
import ch.qos.logback.classic.Level;
import org.apache.commons.cli.*;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static be.ugent.rml.StrictMode.BEST_EFFORT;
import static be.ugent.rml.StrictMode.STRICT;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static final Marker fatal = MarkerFactory.getMarker("FATAL");

    private static final String defaultBaseIRI = "http://example.com";

    public static void main(String[] args) {
        try {
            run(args, System.getProperty("user.dir"));
        } catch (Exception e) {
            System.out.println(e);
            System.exit(1);
        }
    }

    public static void run(String[] args) throws Exception {
        run(args, System.getProperty("user.dir"));
    }

    /**
     * Main method use for the CLI. Allows to also set the current working directory
     * via the argument basePath.
     *
     * @param args     the CLI arguments
     * @param basePath the basePath used during the execution.
     */
    public static void run(String[] args, String basePath) throws Exception {
        Options options = new Options();
        Option mappingdocOption = Option.builder("m")
                .longOpt("mappingfile")
                .hasArg()
                .numberOfArgs(Option.UNLIMITED_VALUES)
                .desc("one or more mapping file paths and/or strings (multiple values are concatenated). " +
                        "r2rml is converted to rml if needed using the r2rml arguments."
                + "RDF Format is determined based on extension.")
                .build();
        Option privateSecurityDataOption = Option.builder("psd")
                .longOpt("privatesecuritydata")
                .hasArg()
                .numberOfArgs(Option.UNLIMITED_VALUES)
                .desc("one or more private security files containing all private security information such as " +
                        "usernames, passwords, certificates, etc.")
                .build();
        Option outputfileOption = Option.builder("o")
                .longOpt("outputfile")
                .hasArg()
                .desc("path to output file (default: stdout)")
                .build();
        Option functionfileOption = Option.builder("f")
                .longOpt("functionfile")
                .hasArg()
                .numberOfArgs(Option.UNLIMITED_VALUES)
                .desc("one or more function file paths (dynamic functions with relative paths are found relative to the cwd)")
                .build();
        Option triplesmapsOption = Option.builder("t")
                .longOpt("triplesmaps")
                .hasArg()
                .desc("IRIs of the triplesmaps that should be executed in order, split by ',' (default is all triplesmaps)")
                .build();
        Option removeduplicatesOption = Option.builder("d")
                .longOpt("duplicates")
                .desc("remove duplicates in the HDT, N-Triples, or N-Quads output")
                .build();
        Option configfileOption = Option.builder("c")
                .longOpt("configfile")
                .hasArg()
                .desc("path to configuration file")
                .build();
        Option helpOption = Option.builder("h")
                .longOpt("help")
                .desc("show help info")
                .build();
        Option verboseOption = Option.builder("v")
                .longOpt("verbose")
                .desc("show more details in debugging output")
                .build();
        Option metadataOption = Option.builder("e")
                .longOpt("metadatafile")
                .hasArg()
                .desc("path to output metadata file")
                .build();
        Option metadataDetailLevelOption = Option.builder("l")
                .longOpt("metadataDetailLevel")
                .hasArg()
                .desc("generate metadata on given detail level (dataset - triple - term)")
                .build();
        Option serializationFormatOption = Option.builder("s")
                .longOpt("serialization")
                .desc("serialization format (nquads (default), turtle, trig, trix, jsonld, hdt)")
                .hasArg()
                .build();
        Option jdbcDSNOption = Option.builder("dsn")
                .longOpt("r2rml-jdbcDSN")
                .desc("DSN of the database when using R2RML rules")
                .hasArg()
                .build();
        Option passwordOption = Option.builder("p")
                .longOpt("r2rml-password")
                .desc("password of the database when using R2RML rules")
                .hasArg()
                .build();
        Option usernameOption = Option.builder("u")
                .longOpt("r2rml-username")
                .desc("username of the database when using R2RML rules")
                .hasArg()
                .build();
        Option strictModeOption = Option.builder()
                .longOpt("strict")
                .desc("Enable strict mode. In strict mode, the mapper will fail on invalid IRIs instead of skipping them.")
                .build();
        Option baseIriOption = Option.builder("b")
                .longOpt("base-iri")
                .desc("Base IRI used to expand relative IRIs in generated terms in the output.")
                .hasArg()
                .build();
        Option provideOwnEOFMarkerOption = Option.builder()
                .longOpt("disable-automatic-eof-marker")
                .desc("Setting this option assumes input data has a kind of End-of-File marker. " +
                        "Don't use unless you're absolutely sure what you're doing!")
                .build();

        options.addOption(mappingdocOption);
        options.addOption(privateSecurityDataOption);
        options.addOption(outputfileOption);
        options.addOption(functionfileOption);
        options.addOption(removeduplicatesOption);
        options.addOption(triplesmapsOption);
        options.addOption(configfileOption);
        options.addOption(helpOption);
        options.addOption(verboseOption);
        options.addOption(serializationFormatOption);
        options.addOption(metadataOption);
        options.addOption(metadataDetailLevelOption);
        options.addOption(jdbcDSNOption);
        options.addOption(passwordOption);
        options.addOption(usernameOption);
        options.addOption(strictModeOption);
        options.addOption(baseIriOption);
        options.addOption(provideOwnEOFMarkerOption);

        CommandLineParser parser = new DefaultParser();
        try {
            // parse the command line arguments
            CommandLine lineArgs = parser.parse(options, args);

            // Check if config file is given
            Properties configFile = null;
            if (lineArgs.hasOption("c")) {
                configFile = new Properties();
                try (Reader reader = Utils.getReaderFromLocation(lineArgs.getOptionValue("c"))) {
                    configFile.load(reader);
                }
            }

            if (checkOptionPresence(helpOption, lineArgs, configFile)) {
                printHelp(options);
                return;
            }

            if (checkOptionPresence(verboseOption, lineArgs, configFile)) {
                setLoggerLevel(Level.DEBUG);
            } else {
                setLoggerLevel(Level.ERROR);
            }

            String[] mOptionValue = getOptionValues(mappingdocOption, lineArgs, configFile);
            List<InputStream> lis = new ArrayList<>();

            if (mOptionValue == null && System.console() != null) {
                printHelp(options);
                throw new IllegalArgumentException("No mapping file nor via stdin found!");
            }

            String outputFile = getPriorityOptionValue(outputfileOption, lineArgs, configFile);
            // If output path exists and contains 'directory-like' characters
            if (outputFile != null) {
                // Windows paths 🤷‍♂️
                outputFile = outputFile.replaceAll("\\\\", "/");
                if (!Utils.checkPathParent(outputFile, null)) {
                    logger.error(fatal, "The given output path does not exist.");
                    throw new IllegalArgumentException("The given output path does not exist.");
                }
            }

            if (mOptionValue != null) {
                // Concatenate all mapping files
                lis = Arrays.stream(mOptionValue)
                        .map(Utils::getInputStreamFromFileOrContentString)
                        .collect(Collectors.toList());
            }

            try {
                BufferedInputStream bis = new BufferedInputStream(System.in);
                int available = bis.available();
                if (available > 0) {
                    // This little hack solves Maven tests: if the console is detached
                    // the normal System.in could send EOT bytes to indicate that there is no
                    // input.
                    // So we check if there are other bytes than the <End of Transmission> (EOT) / End of File (EOF) bytes: 04
                    byte[] firstBytes = new byte[32];
                    bis.mark(32);
                    int bytesRead = bis.read(firstBytes);
                    bis.reset();
                    if (bytesRead > 0) {
                        boolean addStream = false;
                        for (byte aByte : firstBytes) {
                            if (aByte != 0 && aByte != 4) {     // 4 is the EOF / EOT byte
                                addStream = true;
                                break;
                            }
                        }
                        if (addStream) {
                            lis.add(bis);
                        }
                    }
                }
            } catch (IOException ex) {
                logger.warn("Error trying to check System.in: {}", ex.getMessage());
                // The inputstream is closed when read. Leads to IOExceptions for tests that don't provide their own inputstream
            }

            InputStream is = new SequenceInputStream(Collections.enumeration(lis));;

            Map<String, String> mappingOptions = new HashMap<>();
            for (Option option : new Option[]{jdbcDSNOption, passwordOption, usernameOption}) {
                if (checkOptionPresence(option, lineArgs, configFile)) {
                    mappingOptions.put(option.getLongOpt().replace("r2rml-", ""), getOptionValues(option, lineArgs, configFile)[0]);
                }
            }

            // Read mapping file.
            RDF4JStore rmlStore = new RDF4JStore();
            try {
                rmlStore.read(is, null, RDFFormat.TURTLE);
            }
            catch (RDFParseException e) {
                logger.error(fatal, "Unable to parse mapping rules as Turtle. Does the file exist and is it valid Turtle?", e);
                throw new IllegalArgumentException("Unable to parse mapping rules as Turtle. Does the file exist and is it valid Turtle?");
            }

            // Private security data is optionally
            if (lineArgs.hasOption("psd")) {
                // Read the private security data.
                String[] mOptionValuePrivateSecurityData = getOptionValues(privateSecurityDataOption, lineArgs, configFile);
                List<InputStream> lisPrivateSecurityData = Arrays.stream(mOptionValuePrivateSecurityData)
                        .map(Utils::getInputStreamFromFileOrContentString)
                        .collect(Collectors.toList());

                try (InputStream isPrivateSecurityData = new SequenceInputStream(Collections.enumeration(lisPrivateSecurityData))) {
                    rmlStore.read(isPrivateSecurityData, null, RDFFormat.TURTLE);
                } catch (RDFParseException e) {
                    logger.debug(e.getMessage());
                    logger.error(fatal, "Unable to parse private security data as Turtle. Does the file exist and is it valid Turtle?");
                    throw new IllegalArgumentException("Unable to parse private security data as Turtle. Does the file exist and is it valid Turtle?");
                }
            }
            
            String mappingPath = "";
            try {
                mappingPath = Utils.getFile(lineArgs.getOptionValue('m')).getParent();
            } catch (Exception e) {
                logger.debug("Mapping path unknown as mapping file supplied via stdin");
            }
            RecordsFactory factory = new RecordsFactory(basePath, mappingPath);

            String outputFormat = getPriorityOptionValue(serializationFormatOption, lineArgs, configFile);
            QuadStore outputStore = getStoreForFormat(outputFormat);

            Executor executor;

            // Extract required information and create the MetadataGenerator
            MetadataGenerator metadataGenerator = null;
            String metadataFile = getPriorityOptionValue(metadataOption, lineArgs, configFile);
            String requestedDetailLevel = getPriorityOptionValue(metadataDetailLevelOption, lineArgs, configFile);

            if (checkOptionPresence(metadataOption, lineArgs, configFile)) {
                if (requestedDetailLevel != null) {
                    MetadataGenerator.DETAIL_LEVEL detailLevel;
                    switch (requestedDetailLevel) {
                        case "dataset":
                            detailLevel = MetadataGenerator.DETAIL_LEVEL.DATASET;
                            break;
                        case "triple":
                            detailLevel = MetadataGenerator.DETAIL_LEVEL.TRIPLE;
                            break;
                        case "term":
                            detailLevel = MetadataGenerator.DETAIL_LEVEL.TERM;
                            break;
                        default:
                            logger.error("Unknown metadata detail level option. Use the -h flag for more info.");
                            return;
                    }

                    QuadStore metadataStore = getStoreForFormat(outputFormat);

                    metadataGenerator = new MetadataGenerator(
                            detailLevel,
                            getPriorityOptionValue(metadataOption, lineArgs, configFile),
                            mOptionValue,
                            rmlStore,
                            metadataStore
                    );
                } else {
                    logger.error("Please specify the detail level when requesting metadata generation. Use the -h flag for more info.");
                }
            }

            boolean strict = checkOptionPresence(strictModeOption, lineArgs, configFile);
            StrictMode strictMode = strict ? STRICT : BEST_EFFORT;

            // get the base IRI
            String baseIRI = getPriorityOptionValue(baseIriOption, lineArgs, configFile);
            if (baseIRI == null || baseIRI.isEmpty()) {
                // if no explicit base IRI is set
                if (strictMode.equals(STRICT)) {
                    throw new Exception("When running in strict mode, a base IRI argument must be set.");
                } else {
                    if (mOptionValue != null) {
                        /*
                         * We have to get the InputStreams of the RML documents again,
                         * because we can only use an InputStream once
                         */
                        lis = Arrays.stream(mOptionValue)
                                .map(Utils::getInputStreamFromFileOrContentString)
                                .collect(Collectors.toList());
                    }
                    // Best-effort mode, use the @base directive as a fallback
                    try (InputStream is2 = new SequenceInputStream(Collections.enumeration(lis))) {
                        baseIRI = Utils.getBaseDirectiveTurtleOrDefault(is2, defaultBaseIRI);
                    }
                }
            }

            String[] fOptionValue = getOptionValues(functionfileOption, lineArgs, configFile);
            final Agent functionAgent;

            List<Term> triplesMaps = new ArrayList<>();

            String tOptionValue = getPriorityOptionValue(triplesmapsOption, lineArgs, configFile);
            if (tOptionValue != null) {
                List<String> triplesMapsIRI = Arrays.asList(tOptionValue.split(","));
                triplesMapsIRI.forEach(iri -> {
                    triplesMaps.add(new NamedNode(iri));
                });
            }

            // Read function description files.
            if (fOptionValue == null) {
                // default initialisation with IDLab functions and GREL functions...
                functionAgent = AgentFactory.createFromFnO(
                        "fno/functions_idlab.ttl", "fno/functions_idlab_classes_java_mapping.ttl",
                        "fno_idlab_old/functions_idlab.ttl", "fno_idlab_old/functions_idlab_classes_java_mapping.ttl",
                        "functions_grel.ttl",
                        "grel_java_mapping.ttl");
            } else {
                logger.debug("Using custom path to functions.ttl file: {}", Arrays.toString(fOptionValue));
                String[] optionWithIDLabFunctionArgs = new String[fOptionValue.length + 4];
                optionWithIDLabFunctionArgs[0] = "fno/functions_idlab.ttl" ;
                optionWithIDLabFunctionArgs[1] = "fno/functions_idlab_classes_java_mapping.ttl" ;
                optionWithIDLabFunctionArgs[2] = "fno_idlab_old/functions_idlab.ttl" ;
                optionWithIDLabFunctionArgs[3] = "fno_idlab_old/functions_idlab_classes_java_mapping.ttl" ;
                System.arraycopy(fOptionValue, 0, optionWithIDLabFunctionArgs, 4, fOptionValue.length);
                functionAgent = AgentFactory.createFromFnO(optionWithIDLabFunctionArgs);
            }
            executor = new Executor(rmlStore, factory, outputStore, baseIRI, strictMode, functionAgent, mappingOptions);

            if (checkOptionPresence(provideOwnEOFMarkerOption, lineArgs, configFile)) {
                logger.warn("Automatic EOF marker disabled!");
                executor.setEOFProvidedInData();
            }

            executor.verifySources(basePath, mappingPath);
            if (metadataGenerator != null) {
                metadataGenerator.preMappingGeneration(triplesMaps.isEmpty() ?
                        executor.getTriplesMaps() : triplesMaps, rmlStore);
            }

            // Get start timestamp for post mapping metadata
            String startTimestamp = Instant.now().toString();
            QuadStore result = null;

            try {
                Map<Term, QuadStore> targets = executor.execute(triplesMaps, checkOptionPresence(removeduplicatesOption, lineArgs, configFile), metadataGenerator);
            } catch (Exception e) {
                logger.error(e.getMessage());
                throw e;
            } finally {
                functionAgent.close();
            }

            Map<Term, QuadStore> targets = executor.getTargets();
            if (targets != null) {
                result = targets.get(new NamedNode("rmlmapper://default.store"));
                if(result != null) {
                    result.copyNameSpaces(rmlStore);
                }

                result.copyNameSpaces(rmlStore);

                IDLabFunctions.saveState();

                writeOutputTargets(targets, rmlStore, basePath, outputFile, outputFormat);
            }
            // Get stop timestamp for post mapping metadata
            String stopTimestamp = Instant.now().toString();

            // Generate post mapping metadata and output all metadata
            if (metadataGenerator != null && targets != null) {
                metadataGenerator.postMappingGeneration(startTimestamp, stopTimestamp, result);
                writeOutput(metadataGenerator.getResult(), metadataFile, outputFormat);
            }

        } catch (ParseException exp) {
            // oops, something went wrong
            logger.error("Parsing failed. Reason: {}", exp.getMessage());
            printHelp(options);
        } catch (IllegalArgumentException exp) {
            throw exp;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw e;
        }
    }

    private static void writeOutputTargets(Map<Term, QuadStore> targets, QuadStore rmlStore, String basePath, String outputFileDefault, String outputFormatDefault) throws Exception {
        boolean hasNoResults = true;

        logger.debug("Writing to Targets: {}", targets.keySet());
        TargetFactory targetFactory = new TargetFactory(basePath);

        // Go over each term and export to the Target if needed
        for (Map.Entry<Term, QuadStore> termTargetMapping: targets.entrySet()) {
            Term term = termTargetMapping.getKey();
            QuadStore store = termTargetMapping.getValue();

            if (!store.isEmpty()) {
                hasNoResults = false;
                logger.info("Target: {} has {} results", term, store.size());
            }

            /* Remove magic marker from output */
            List<Quad> quads = store.getQuads(null, null, null, null);
            for (Quad q: quads) {
                String subject = q.getSubject().toString();
                String object = q.getObject().toString();
                if (subject.contains(IDLabFunctions.MAGIC_MARKER_ENCODED)
                        || subject.contains(IDLabFunctions.MAGIC_MARKER)
                        || object.contains(IDLabFunctions.MAGIC_MARKER_ENCODED)
                        || object.contains(IDLabFunctions.MAGIC_MARKER) ) {
                    store.removeQuads(q.getSubject(), q.getPredicate(), q.getObject(), q.getGraph());
                }
            }

            // Default target is exported separately for backwards compatibility reasons
            if (term.getValue().equals("rmlmapper://default.store")) {
                logger.debug("Exporting to default Target");
                writeOutput(store, outputFileDefault, outputFormatDefault);
            }
            else {
                logger.debug("Exporting to Target: {}", term);
                if (store.size() > 1) {
                    logger.info("{} quads were generated for {} Target", store.size(), term);
                } else {
                    logger.info("{} quad was generated {} Target", store.size(), term);
                }

                Target target = targetFactory.getTarget(term, rmlStore, store);
                String serializationFormat = target.getSerializationFormat();
                OutputStream output = target.getOutputStream();
                store.addQuads(target.getMetadata());

                // Set character encoding
                try (Writer out = new BufferedWriter(new OutputStreamWriter(output, Charset.defaultCharset()))) {
                    // Write store to target
                    store.write(out, serializationFormat);
                }
                // Close OS resources
                target.close();
            }
        }

        if (hasNoResults) {
            logger.info("No results!");
        }
    }

    private static boolean checkOptionPresence(Option option, CommandLine lineArgs, Properties properties) {
        return (option.getOpt() != null && lineArgs.hasOption(option.getOpt()))
                || (option.getLongOpt() != null && lineArgs.hasOption(option.getLongOpt()))
                || (properties != null
                && properties.getProperty(option.getLongOpt()) != null
                && !properties.getProperty(option.getLongOpt()).equals("false"));  // ex: 'help = false' in the config file shouldn't return the help text
    }

    private static String getPriorityOptionValue(Option option, CommandLine lineArgs, Properties properties) {
        if (lineArgs.hasOption(option.getOpt())) {
            return lineArgs.getOptionValue(option.getOpt());
        } else if (properties != null && properties.getProperty(option.getLongOpt()) != null) {
            return properties.getProperty(option.getLongOpt());
        } else {
            return null;
        }
    }

    private static String[] getOptionValues(Option option, CommandLine lineArgs, Properties properties) {
        if (lineArgs.hasOption(option.getOpt())) {
            return lineArgs.getOptionValues(option.getOpt());
        } else if (properties != null && properties.getProperty(option.getLongOpt()) != null) {
            return properties.getProperty(option.getLongOpt()).split(" ");
        } else {
            return null;
        }
    }


    private static void printHelp(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("java -jar mapper.jar <options>\noptions:", options);
    }

    private static void setLoggerLevel(Level level) {
        Logger root = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        ((ch.qos.logback.classic.Logger) root).setLevel(level);
    }

    private static void writeOutput(QuadStore store, String outputFile, String format) {
        boolean hdt = format != null && format.equals("hdt");

        if (hdt) {
            try {
                format = "nquads";
                File tmpFile = File.createTempFile("file", ".nt");
                tmpFile.deleteOnExit();
                String uncompressedOutputFile = tmpFile.getAbsolutePath();

                File nquadsFile = writeOutputUncompressed(store, uncompressedOutputFile, format);
                Utils.ntriples2hdt(uncompressedOutputFile, outputFile);
                nquadsFile.deleteOnExit();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            if (format != null) {
                format = format.toLowerCase();
            } else {
                format = "nquads";
            }

            writeOutputUncompressed(store, outputFile, format);
        }
    }

    private static File writeOutputUncompressed(QuadStore store, String outputFile, String format) {
        File targetFile = null;

        if (store.size() > 1) {
            logger.info("{} quads were generated for default Target", store.size());
        } else {
            logger.info("{} quad was generated for default Target", store.size());
        }

        Writer out = null;
        try {

            String doneMessage = null;
            boolean isSystemOut = false;
            //if output file provided, write to triples output file
            if (outputFile != null) {
                targetFile = new File(outputFile);
                logger.info("Writing quads to {}...", targetFile.getPath());

                if (!targetFile.isAbsolute()) {
                    targetFile = new File(System.getProperty("user.dir") + "/" + outputFile);
                }

                doneMessage = "Writing to " + targetFile.getPath() + " is done.";

                out = Files.newBufferedWriter(targetFile.toPath(), StandardCharsets.UTF_8);

            } else {
                isSystemOut = true;
                out = new BufferedWriter(new OutputStreamWriter(System.out, StandardCharsets.UTF_8));
            }

            store.write(out, format);
            if (isSystemOut) {
                out.flush(); // flush System.out stream
                out = null; // replace with null, so it won't be closed later;
            }

            if (doneMessage != null) {
                logger.info(doneMessage);
            }
        } catch (Exception e) {
            logger.error("Writing output failed. Reason: " + e.getMessage());
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    logger.error("Could not close writer. ", e);
                }
            }

        }

        return targetFile;
    }

    private static QuadStore getStoreForFormat(String outputFormat) {
        return new RDF4JStore();
    }
}
