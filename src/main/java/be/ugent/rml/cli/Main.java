package be.ugent.rml.cli;

import be.ugent.rml.*;
import be.ugent.rml.functions.FunctionLoader;
import be.ugent.rml.records.RecordsFactory;
import be.ugent.rml.store.QuadStore;
import be.ugent.rml.store.SimpleQuadStore;
import be.ugent.rml.store.TriplesQuads;
import be.ugent.rml.term.NamedNode;
import be.ugent.rml.term.Term;
import ch.qos.logback.classic.Level;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.time.Instant;
import java.util.*;

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
        Option metadataOption = Option.builder( "md")
                .longOpt( "metadatafile" )
                .hasArg()
                .desc( "path to metadata file" )
                .build();
        Option metadataDatasetLevelOption = Option.builder("ds")
                .longOpt( "datasetlevel" )
                .desc( "generate metadata on dataset level" )
                .build();
        Option metadataTripleLevelOption = Option.builder("tr")
                .longOpt( "tripleLevel" )
                .desc( "generate metadata on triple level" )
                .build();
        Option metadataTermLevelOption = Option.builder("te")
                .longOpt( "termLevel" )
                .desc(" generate metadata on term level" )
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
        options.addOption(metadataDatasetLevelOption);
        options.addOption(metadataTripleLevelOption);
        options.addOption(metadataTermLevelOption);

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
                MetadataGenerator.DETAIL_LEVEL detailLevel;
                if (checkOptionPresence(metadataTermLevelOption, lineArgs, configFile)) {
                    detailLevel = MetadataGenerator.DETAIL_LEVEL.TERM;
                } else if (checkOptionPresence(metadataTripleLevelOption, lineArgs, configFile)) {
                    detailLevel = MetadataGenerator.DETAIL_LEVEL.TRIPLE;
                } else if (checkOptionPresence(metadataDatasetLevelOption, lineArgs, configFile)) {
                    detailLevel = MetadataGenerator.DETAIL_LEVEL.DATASET;
                } else {
                    detailLevel = MetadataGenerator.DETAIL_LEVEL.PREVENT;
                }

                MetadataGenerator metadataGenerator = new MetadataGenerator(
                        detailLevel,
                        getPriorityOptionValue(metadataOption, lineArgs, configFile),
                        mOptionValue,
                        rmlStore
                );

                String fOptionValue = getPriorityOptionValue(functionfileOption, lineArgs, configFile);
                if (fOptionValue == null) {
                    executor = new Executor(rmlStore, factory);
                } else {
                    File functionFile = Utils.getFile(fOptionValue);
                    FunctionLoader functionLoader = new FunctionLoader(functionFile);
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

                // Get start timestamp for metadatafile
                String startTimestamp = Instant.now().toString();

                QuadStore result = executor.execute(triplesMaps, checkOptionPresence(removeduplicatesOption, lineArgs, configFile),
                        metadataGenerator);

                // Get stop timestamp for metadatafile
                String stopTimestamp = Instant.now().toString();

                // Generate post mapping metadata
                metadataGenerator.postMappingGeneration(startTimestamp, stopTimestamp, executor.getInitializer().getTriplesMaps(),
                        result);

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

                metadataGenerator.writeMetadata();
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
