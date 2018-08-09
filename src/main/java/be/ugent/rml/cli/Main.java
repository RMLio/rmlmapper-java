package be.ugent.rml.cli;

import be.ugent.rml.*;
import be.ugent.rml.functions.FunctionLoader;
import be.ugent.rml.records.RecordsFactory;
import be.ugent.rml.store.Quad;
import be.ugent.rml.store.QuadStore;
import be.ugent.rml.store.TriplesQuads;
import be.ugent.rml.term.NamedNode;
import be.ugent.rml.term.Term;
import ch.qos.logback.classic.Level;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String [] args) {
        Options options = new Options();
        Option mappingdoc = Option.builder("m")
                .longOpt( "mapping" )
                .hasArg()
                .desc(  "path to mapping document" )
                .build();
        Option outputfile = Option.builder("o")
                .longOpt( "outputfile" )
                .hasArg()
                .desc(  "path to output file" )
                .build();
        Option functionfile = Option.builder("f")
                .longOpt("functionfile")
                .hasArg()
                .desc("path to functions.ttl file (dynamic functions are found relative to functions.ttl)")
                .build();
        Option triplesmaps = Option.builder("t")
                .longOpt( "triplesmaps" )
                .hasArg()
                .desc(  "IRIs of the triplesmaps that should be executed (default is all triplesmaps)" )
                .build();
        Option removeduplicates = Option.builder("d")
                .longOpt( "duplicates" )
                .desc(  "remove duplicates" )
                .build();
        Option metadatafile = Option.builder("md")
                .longOpt( "metadatafile" )
                .hasArg()
                .desc( "path to metadata output file" )
                .build();
        Option metadataDatasetLevel = Option.builder("ds")
                .longOpt( "datasetlevel" )
                .desc( "generate metadata on dataset level" )
                .build();
        options.addOption(mappingdoc);
        options.addOption(outputfile);
        options.addOption(functionfile);
        options.addOption(removeduplicates);
        options.addOption(triplesmaps);
        options.addOption(metadatafile);
        options.addOption(metadataDatasetLevel);
        options.addOption("v", "verbose", false, "verbose");
        options.addOption("h", "help", false, "get help info");

        // create the parser
        CommandLineParser parser = new DefaultParser();
        try {
            // parse the command line arguments
            CommandLine line = parser.parse( options, args );

            if (line.hasOption("h")) {
                printHelp(options);
                return;
            }

            if (line.hasOption("v")) {
                setLoggerLevel(Level.DEBUG);
            } else {
                setLoggerLevel(Level.ERROR);
            }

            if (line.hasOption("m")){// Let's map!
                File mappingFile = Utils.getFile(line.getOptionValue("m"));
                QuadStore rmlStore = Utils.readTurtle(mappingFile);
                RecordsFactory factory = new RecordsFactory(new DataFetcher(System.getProperty("user.dir"), rmlStore));
                Executor executor;

                if (line.hasOption("f")) {
                    File functionFile = Utils.getFile(line.getOptionValue("f"));
                    FunctionLoader functionLoader = new FunctionLoader(functionFile);
                    executor = new Executor(rmlStore, factory, functionLoader);
                } else {
                    executor = new Executor(rmlStore, factory);
                }

                List<Term> triplesMaps = new ArrayList<>();

                if (line.hasOption("t")) {
                    List<String> triplesMapsIRI = Arrays.asList(line.getOptionValue("t").split(","));

                    triplesMapsIRI.forEach(iri -> {
                        triplesMaps.add(new NamedNode(iri));
                    });
                }

                QuadStore result = executor.execute(triplesMaps, line.hasOption("d"));

                TriplesQuads tq = Utils.getTriplesAndQuads(result.getQuads(null, null, null, null));

                if (!tq.getTriples().isEmpty()) {
                    //write triples
                    writeOutput("triple", tq.getTriples(), "nt", line);
                }

                if (!tq.getQuads().isEmpty()) {
                    //write quads
                    writeOutput("quad", tq.getQuads(), "nq", line);
                }
            } else {
                printHelp(options);
            }
        }
        catch( ParseException exp ) {
            // oops, something went wrong
            logger.error("Parsing failed. Reason: " + exp.getMessage());
            printHelp(options);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
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

    private static void writeOutput(String what, List<Quad> output, String extension, CommandLine line) {
        if (output.size() > 1) {
            logger.info(output.size() + " " + what + "s were generated");
        } else {
            logger.info(output.size() + " " + what + " was generated");
        }

        //if output file provided, write to triples output file
        if (line.hasOption("o")) {
            File outputFile = new File(line.getOptionValue("o") + "." + extension);
            logger.info("Writing " + what + " to " + outputFile.getPath() + "...");

            if (!outputFile.isAbsolute()) {
                outputFile = new File(System.getProperty("user.dir") + "/" + line.getOptionValue("o") + "." +  extension);
            }

            try {
                BufferedWriter out = new BufferedWriter(new FileWriter(outputFile));

                if (what.equals("triple")) {
                    Utils.toNTriples(output, out);
                } else {
                    Utils.toNQuads(output, out);
                }

                out.close();
                logger.info("Writing to " + outputFile.getPath() + " is done.");
            } catch(IOException e) {
                System.err.println( "Writing output to file failed. Reason: " + e.getMessage() );
            }
        } else {
            if (what.equals("triple")) {
                System.out.println(Utils.toNTriples(output));
            } else {
                System.out.println(Utils.toNQuads(output));
            }
        }
    }
}
