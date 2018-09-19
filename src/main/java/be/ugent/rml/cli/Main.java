package be.ugent.rml.cli;

import be.ugent.rml.*;
import be.ugent.rml.functions.FunctionLoader;
import be.ugent.rml.functions.lib.GrelProcessor;
import be.ugent.rml.metadata.MetadataGenerator;
import be.ugent.rml.records.RecordsFactory;
import be.ugent.rml.store.QuadStore;
import be.ugent.rml.store.TriplesQuads;
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

    public static void main(String [] args) {
        Options options = new Options();
        Option mappingdocOption = Option.builder("m")
                .longOpt( "mappingfile" )
                .hasArg()
                .desc(  "path to mapping document" )
                .build();
        Option outputfileOption = Option.builder("o")
                .longOpt( "outputfile" )
                .hasArg()
                .desc(  "path to output file" )
                .build();
        Option functionfileOption = Option.builder("f")
                .longOpt("functionfile")
                .hasArg()
                .desc("path to functions.ttl file (dynamic functions are found relative to functions.ttl)")
                .build();
        Option triplesmapsOption = Option.builder("t")
                .longOpt( "triplesmaps" )
                .hasArg()
                .desc(  "IRIs of the triplesmaps that should be executed (default is all triplesmaps)" )
                .build();
        Option removeduplicatesOption = Option.builder("d")
                .longOpt( "duplicates" )
                .desc(  "remove duplicates" )
                .build();
        Option configfileOption = Option.builder("c")
                .longOpt( "configfile" )
                .hasArg()
                .desc( "path to configuration file" )
                .build();
        Option helpOption = Option.builder("h")
                .longOpt( "help" )
                .desc( "get help info" )
                .build();
        Option verboseOption = Option.builder("v")
                .longOpt( "verbose" )
                .desc( "verbose" )
                .build();
        Option metadataOption = Option.builder( "e")
                .longOpt( "metadatafile" )
                .hasArg()
                .desc( "path to metadata-test-cases file" )
                .build();
        Option metadataDetailLevelOption = Option.builder("l")
                .longOpt( "metadataDetailLevel" )
                .hasArg()
                .desc( "generate metadata-test-cases on given detail level (dataset - triple - term)" )
                .build();
        options.addOption(mappingdocOption);
        options.addOption(outputfileOption);
        options.addOption(functionfileOption);
        options.addOption(removeduplicatesOption);
        options.addOption(triplesmapsOption);
        options.addOption(configfileOption);
        options.addOption(helpOption);
        options.addOption(verboseOption);
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
                QuadStore rmlStore = Utils.readTurtle(mappingFile);
                RecordsFactory factory = new RecordsFactory(new DataFetcher(System.getProperty("user.dir"), rmlStore));
                Executor executor;

                // Extract required information and create the MetadataGenerator
                MetadataGenerator metadataGenerator = null;
                String requestedDetailLevel = getPriorityOptionValue(metadataDetailLevelOption, lineArgs, configFile);
                if (checkOptionPresence(metadataOption, lineArgs, configFile)) {
                    if (requestedDetailLevel != null) {
                        MetadataGenerator.DETAIL_LEVEL detailLevel;
                        switch(requestedDetailLevel) {
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
                                logger.error("Unknown metadata-test-cases detail level option. Use the -h flag for more info.");
                                return;
                        }
                        metadataGenerator = new MetadataGenerator(
                                detailLevel,
                                getPriorityOptionValue(metadataOption, lineArgs, configFile),
                                mOptionValue,
                                rmlStore
                        );
                    } else {
                        logger.error("Please specify the detail level when requesting metadata-test-cases generation. Use the -h flag for more info.");
                    }
                }

                String fOptionValue = getPriorityOptionValue(functionfileOption, lineArgs, configFile);
                if (fOptionValue == null) {
                    executor = new Executor(rmlStore, factory);
                } else {
                    Map<String, Class> libraryMap = new HashMap<>();
                    libraryMap.put("GrelFunctions", GrelProcessor.class);
                    FunctionLoader functionLoader = new FunctionLoader(null, null, libraryMap);
                    executor = new Executor(rmlStore, factory, functionLoader);
                }

                List<Term> triplesMaps = new ArrayList<>();

                String tOptionValue = getPriorityOptionValue(triplesmapsOption, lineArgs, configFile);
                if (tOptionValue != null) {
                    List<String> triplesMapsIRI = Arrays.asList(tOptionValue.split(","));
                    triplesMapsIRI.forEach(iri -> {
                        triplesMaps.add(new NamedNode(iri));
                    });
                }

                if (metadataGenerator != null) {
                    metadataGenerator.preMappingGeneration((triplesMaps == null || triplesMaps.isEmpty()) ?
                            executor.getTriplesMaps() : triplesMaps, rmlStore);
                }

                // Get start timestamp for post mapping metadata-test-cases
                String startTimestamp = Instant.now().toString();

                QuadStore result = executor.execute(triplesMaps, checkOptionPresence(removeduplicatesOption, lineArgs, configFile),
                        metadataGenerator);

                // Get stop timestamp for post mapping metadata-test-cases
                String stopTimestamp = Instant.now().toString();

                // Generate post mapping metadata-test-cases and output all metadata-test-cases
                if (metadataGenerator != null) {
                    metadataGenerator.postMappingGeneration(startTimestamp, stopTimestamp,
                            result);
                    metadataGenerator.writeMetadata();
                }

                TriplesQuads tq = Utils.getTriplesAndQuads(result.getQuads(null, null, null, null));

                String outputFile = getPriorityOptionValue(outputfileOption, lineArgs, configFile);
                if (!tq.getTriples().isEmpty()) {
                    //write triples
                    Utils.writeOutput("triple", tq.getTriples(), "nt", outputFile);
                }

                if (!tq.getQuads().isEmpty()) {
                    //write quads
                    Utils.writeOutput("quad", tq.getQuads(), "nq", outputFile);
                }
            }
        } catch( ParseException exp ) {
            // oops, something went wrong
            logger.error("Parsing failed. Reason: " + exp.getMessage());
            printHelp(options);
        } catch (IOException e) {
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
}
