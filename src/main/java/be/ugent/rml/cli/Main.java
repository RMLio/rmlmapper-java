package be.ugent.rml.cli;

import be.ugent.rml.*;
import be.ugent.rml.functions.FunctionLoader;
import be.ugent.rml.functions.lib.GrelProcessor;
import be.ugent.rml.functions.lib.IDLabFunctions;
import be.ugent.rml.metadata.MetadataGenerator;
import be.ugent.rml.records.RecordsFactory;
import be.ugent.rml.store.QuadStore;
import be.ugent.rml.store.RDF4JStore;
import be.ugent.rml.store.SimpleQuadStore;
import be.ugent.rml.term.NamedNode;
import be.ugent.rml.term.Term;
import ch.qos.logback.classic.Level;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Map;
import java.util.HashMap;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        Options options = new Options();
        Option mappingdocOption = Option.builder("m")
                .longOpt("mappingfile")
                .hasArg()
                .desc("path to mapping document")
                .build();
        Option outputfileOption = Option.builder("o")
                .longOpt("outputfile")
                .hasArg()
                .desc("path to output file (default: stdout)")
                .build();
        Option functionfileOption = Option.builder("f")
                .longOpt("functionfile")
                .hasArg()
                .desc("path to functions.ttl file (dynamic functions are found relative to functions.ttl)")
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
                .longOpt( "serialization" )
                .desc( "serialization format (nquads (default), turtle, trig, trix, jsonld, hdt)" )
                .hasArg()
                .build();
        options.addOption(mappingdocOption);
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

            String mOptionValue = getPriorityOptionValue(mappingdocOption, lineArgs, configFile);
            if (mOptionValue == null) {
                printHelp(options);
            } else {
                File mappingFile = Utils.getFile(mOptionValue);
                RDF4JStore rmlStore = Utils.readTurtle(mappingFile);
                RecordsFactory factory = new RecordsFactory(new DataFetcher(System.getProperty("user.dir"), rmlStore));

                String outputFormat = getPriorityOptionValue(serializationFormatOption, lineArgs, configFile);
                QuadStore outputStore;

                if (outputFormat == null || outputFormat.equals("nquads") || outputFormat.equals("hdt")) {
                    outputStore = new SimpleQuadStore();
                } else {
                    outputStore = new RDF4JStore();
                }

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
                        metadataGenerator = new MetadataGenerator(
                                detailLevel,
                                getPriorityOptionValue(metadataOption, lineArgs, configFile),
                                mOptionValue,
                                rmlStore
                        );
                    } else {
                        logger.error("Please specify the detail level when requesting metadata generation. Use the -h flag for more info.");
                    }
                }

                String fOptionValue = getPriorityOptionValue(functionfileOption, lineArgs, configFile);
                FunctionLoader functionLoader;

                Map<String, Class> libraryMap = new HashMap<>();
                libraryMap.put("GrelFunctions", GrelProcessor.class);
                libraryMap.put("IDLabFunctions", IDLabFunctions.class);

                if (fOptionValue == null) {
                    functionLoader = new FunctionLoader(null, null, libraryMap);
                } else {
                    functionLoader = new FunctionLoader(Utils.getFile(fOptionValue), null, libraryMap);
                }

                executor = new Executor(rmlStore, factory, functionLoader, outputStore, Utils.getBaseDirectiveTurtle(mappingFile));

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

                QuadStore result = executor.execute(triplesMaps, checkOptionPresence(removeduplicatesOption, lineArgs, configFile),
                        metadataGenerator);

                // Get stop timestamp for post mapping metadata
                String stopTimestamp = Instant.now().toString();

                // Generate post mapping metadata and output all metadata
                if (metadataGenerator != null) {
                    metadataGenerator.postMappingGeneration(startTimestamp, stopTimestamp,
                            result);

                    writeOutput(metadataGenerator.getResult(), metadataFile, outputFormat);
                }

                String outputFile = getPriorityOptionValue(outputfileOption, lineArgs, configFile);

                if (!result.isEmpty()) {
                    //write quads
                    result.setNamespaces(rmlStore.getNamespaces());
                    writeOutput(result, outputFile, outputFormat);
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
            logger.info(store.size() + " quads were generated");
        } else {
            logger.info(store.size() + " quad was generated");
        }

        try {
            BufferedWriter out;
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
        } catch(IOException e) {
            System.err.println( "Writing output failed. Reason: " + e.getMessage() );
        }

        return targetFile;
    }
}
