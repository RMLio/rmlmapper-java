package be.ugent.rml.cli;

import be.ugent.rml.Executor;
import be.ugent.rml.NAMESPACES;
import be.ugent.rml.Utils;
import be.ugent.rml.access.DatabaseType;
import be.ugent.rml.conformer.MappingConformer;
import be.ugent.rml.functions.FunctionLoader;
import be.ugent.rml.functions.lib.IDLabFunctions;
import be.ugent.rml.metadata.MetadataGenerator;
import be.ugent.rml.records.RecordsFactory;
import be.ugent.rml.store.QuadStore;
import be.ugent.rml.store.RDF4JStore;
import be.ugent.rml.store.SimpleQuadStore;
import be.ugent.rml.target.Target;
import be.ugent.rml.target.TargetFactory;
import be.ugent.rml.term.NamedNode;
import be.ugent.rml.term.Term;
import ch.qos.logback.classic.Level;
import org.apache.commons.cli.*;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.eclipse.rdf4j.rio.RDFParseException;

import java.io.*;
import java.nio.charset.Charset;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static final Marker fatal = MarkerFactory.getMarker("FATAL");

    public static void main(String[] args) {
        main(args, System.getProperty("user.dir"));
    }

    /**
     * Main method use for the CLI. Allows to also set the current working directory via the argument basePath.
     *
     * @param args     the CLI arguments
     * @param basePath the basePath used during the execution.
     */
    public static void main(String[] args, String basePath) {
        Options options = new Options();
        Option mappingdocOption = Option.builder("m")
                .longOpt("mappingfile")
                .hasArg()
                .numberOfArgs(Option.UNLIMITED_VALUES)
                .desc("one or more mapping file paths and/or strings (multiple values are concatenated). " +
                        "r2rml is converted to rml if needed using the r2rml arguments.")
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
                .desc("remove duplicates in the output")
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

        CommandLineParser parser = new DefaultParser();
        try {
            // parse the command line arguments
            CommandLine lineArgs = parser.parse(options, args);

            // Check if config file is given
            Properties configFile = null;
            if (lineArgs.hasOption("c")) {
                configFile = new Properties();
                configFile.load(Utils.getReaderFromLocation(lineArgs.getOptionValue("c")));
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
            if (mOptionValue == null) {
                printHelp(options);
            } else {
                // Concatenate all mapping files
                List<InputStream> lis = Arrays.stream(mOptionValue)
                        .map(Utils::getInputStreamFromFileOrContentString)
                        .collect(Collectors.toList());
                InputStream is = new SequenceInputStream(Collections.enumeration(lis));

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
                    System.exit(1);
                }

                // Private security data is optionally
                if (lineArgs.hasOption("psd")) {
                    // Read the private security data.
                    String[] mOptionValuePrivateSecurityData = getOptionValues(privateSecurityDataOption, lineArgs, configFile);
                    List<InputStream> lisPrivateSecurityData = Arrays.stream(mOptionValuePrivateSecurityData)
                            .map(Utils::getInputStreamFromFileOrContentString)
                            .collect(Collectors.toList());
                    InputStream isPrivateSecurityData = new SequenceInputStream(Collections.enumeration(lisPrivateSecurityData));
                    try {
                        rmlStore.read(isPrivateSecurityData, null, RDFFormat.TURTLE);
                    } catch (RDFParseException e) {
                        logger.debug(e.getMessage());
                        logger.error(fatal, "Unable to parse private security data as Turtle. Does the file exist and is it valid Turtle?");
                        System.exit(1);
                    }
                }

                // Convert mapping file to RML if needed.
                MappingConformer conformer = new MappingConformer(rmlStore, mappingOptions);

                try {
                    boolean conversionNeeded = conformer.conform();

                    if (conversionNeeded) {
                        logger.info("Conversion to RML was needed.");
                    }
                } catch (Exception e) {
                    logger.error(fatal, "Failed to make mapping file conformant to RML spec.", e);
                }

                RecordsFactory factory = new RecordsFactory(basePath);

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

                String[] fOptionValue = getOptionValues(functionfileOption, lineArgs, configFile);
                FunctionLoader functionLoader;

                // Read function description files.
                if (fOptionValue == null) {
                    functionLoader = new FunctionLoader();
                } else {
                    logger.debug("Using custom path to functions.ttl file: " + Arrays.toString(fOptionValue));
                    RDF4JStore functionDescriptionTriples = new RDF4JStore();
                    functionDescriptionTriples.read(Utils.getInputStreamFromFile(Utils.getFile("functions_idlab.ttl")), null, RDFFormat.TURTLE);
                    Map<String, Class> libraryMap = new HashMap<>();
                    libraryMap.put("IDLabFunctions", IDLabFunctions.class);
                    List<InputStream> lisF = Arrays.stream(fOptionValue)
                            .map(Utils::getInputStreamFromFileOrContentString)
                            .collect(Collectors.toList());
                    for (int i = 0; i < lisF.size(); i++) {
                        functionDescriptionTriples.read(lisF.get(i), null, RDFFormat.TURTLE);
                    }
                    functionLoader = new FunctionLoader(functionDescriptionTriples, libraryMap);
                }

                // We have to get the. InputStreams of the RML documents again,
                //                // because we can only use an InputStream once
                lis = Arrays.stream(mOptionValue)
                        .map(Utils::getInputStreamFromFileOrContentString)
                        .collect(Collectors.toList());
                is = new SequenceInputStream(Collections.enumeration(lis));

                executor = new Executor(rmlStore, factory, functionLoader, outputStore, Utils.getBaseDirectiveTurtle(is));

                List<Term> triplesMaps = new ArrayList<>();

                String tOptionValue = getPriorityOptionValue(triplesmapsOption, lineArgs, configFile);
                if (tOptionValue != null) {
                    List<String> triplesMapsIRI = Arrays.asList(tOptionValue.split(","));
                    triplesMapsIRI.forEach(iri -> {
                        triplesMaps.add(new NamedNode(iri));
                    });
                }

                if (metadataGenerator != null) {
                    metadataGenerator.preMappingGeneration(triplesMaps.isEmpty() ?
                            executor.getTriplesMaps() : triplesMaps, rmlStore);
                }

                // Get start timestamp for post mapping metadata
                String startTimestamp = Instant.now().toString();

                try {
                    HashMap<Term, QuadStore> targets = executor.executeV5(triplesMaps, checkOptionPresence(removeduplicatesOption, lineArgs, configFile),
                            metadataGenerator);
                    QuadStore result = targets.get(new NamedNode("rmlmapper://default.store"));

                    // Get stop timestamp for post mapping metadata
                    String stopTimestamp = Instant.now().toString();

                    // Generate post mapping metadata and output all metadata
                    if (metadataGenerator != null) {
                        metadataGenerator.postMappingGeneration(startTimestamp, stopTimestamp,
                                result);

                        writeOutput(metadataGenerator.getResult(), metadataFile, outputFormat);
                    }

                    String outputFile = getPriorityOptionValue(outputfileOption, lineArgs, configFile);
                    result.copyNameSpaces(rmlStore);

                    writeOutputTargets(targets, rmlStore, basePath, outputFile, outputFormat);
                } catch (Exception e) {
                    logger.error(e.getMessage());
                }
            }
        } catch (ParseException exp) {
            // oops, something went wrong
            logger.error("Parsing failed. Reason: " + exp.getMessage());
            printHelp(options);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private static void writeOutputTargets(HashMap<Term, QuadStore> targets, QuadStore rmlStore, String basePath, String outputFileDefault, String outputFormatDefault) throws Exception {
        boolean hasNoResults = true;

        logger.debug("Writing to Targets: " + targets.keySet());
        TargetFactory targetFactory = new TargetFactory(basePath);

        // Go over each term and export to the Target if needed
        for (Map.Entry<Term, QuadStore> termTargetMapping: targets.entrySet()) {
            Term term = termTargetMapping.getKey();
            QuadStore store = termTargetMapping.getValue();

            if (store.size() > 0) {
                hasNoResults = false;
                logger.info("Target: " + term + " has " + store.size() + " results");
            }

            // Default target is exported separately for backwards compatibility reasons
            if (term.getValue().equals("rmlmapper://default.store")) {
                logger.debug("Exporting to default Target");
                writeOutput(store, outputFileDefault, outputFormatDefault);
            }
            else {
                logger.debug("Exporting to Target: " + term);
                if (store.size() > 1) {
                    logger.info(store.size() + " quads were generated for " + term + " Target");
                } else {
                    logger.info(store.size() + " quad was generated " + term + " Target");
                }

                Target target = targetFactory.getTarget(term, rmlStore);
                String serializationFormat = target.getSerializationFormat();
                OutputStream output = target.getOutputStream();

                // Set character encoding
                Writer out = new BufferedWriter(new OutputStreamWriter(output, Charset.defaultCharset()));

                // Write store to target
                store.write(out, serializationFormat);

                // Close OS resources
                out.close();
                target.close();
            }
        }

        if (hasNoResults) {
            logger.info("No results!");
        }
    }

    private static boolean checkOptionPresence(Option option, CommandLine lineArgs, Properties properties) {
        return lineArgs.hasOption(option.getOpt()) || (properties != null
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
            logger.info(store.size() + " quads were generated for default Target");
        } else {
            logger.info(store.size() + " quad was generated for default Target");
        }

        try {
            Writer out;
            String doneMessage = null;

            //if output file provided, write to triples output file
            if (outputFile != null) {
                targetFile = new File(outputFile);
                logger.info("Writing quads to " + targetFile.getPath() + "...");

                if (!targetFile.isAbsolute()) {
                    targetFile = new File(System.getProperty("user.dir") + "/" + outputFile);
                }

                doneMessage = "Writing to " + targetFile.getPath() + " is done.";

                out = new BufferedWriter(new FileWriter(targetFile));

            } else {
                out = new BufferedWriter(new OutputStreamWriter(System.out));
            }

            store.write(out, format);
            out.close();

            if (doneMessage != null) {
                logger.info(doneMessage);
            }
        } catch (Exception e) {
            System.err.println("Writing output failed. Reason: " + e.getMessage());
        }

        return targetFile;
    }

    private static QuadStore getStoreForFormat(String outputFormat) {
        if (outputFormat == null || outputFormat.equals("nquads") || outputFormat.equals("hdt")) {
            return new SimpleQuadStore();
        } else {
            return new RDF4JStore();
        }
    }
}
