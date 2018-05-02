package be.ugent.rml.cli;

import be.ugent.rml.DataFetcher;
import be.ugent.rml.Executor;
import be.ugent.rml.Utils;
import be.ugent.rml.functions.FunctionLoader;
import be.ugent.rml.records.RecordsFactory;
import be.ugent.rml.store.Quad;
import be.ugent.rml.store.QuadStore;
import be.ugent.rml.store.RDF4JStore;
import be.ugent.rml.store.TriplesQuads;
import org.apache.commons.cli.*;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;

import java.io.*;
import java.util.List;

public class Main {

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
        options.addOption(mappingdoc);
        options.addOption(outputfile);

        // create the parser
        CommandLineParser parser = new DefaultParser();
        try {
            // parse the command line arguments
            CommandLine line = parser.parse( options, args );

            if (line.hasOption("m")){
                File mappingFile = new File(line.getOptionValue("m"));

                if (!mappingFile.isAbsolute()) {
                    mappingFile = new File(System.getProperty("user.dir") + "/" + line.getOptionValue("m"));
                }

                InputStream mappingStream = new FileInputStream(mappingFile);

                Model model = Rio.parse(mappingStream, "", RDFFormat.TURTLE);
                RDF4JStore rmlStore = new RDF4JStore(model);

                Executor executor = new Executor(rmlStore, new RecordsFactory(new DataFetcher(System.getProperty("user.dir"), rmlStore)), new FunctionLoader());
                QuadStore result = executor.execute(null);

                TriplesQuads tq = Utils.getTriplesAndQuads(result.getQuads(null, null, null, null));

                if (!tq.getTriples().isEmpty()) {
                    //write triples
                    writeOutput("triple", tq.getTriples(), "nt", line);
                }

                if (!tq.getQuads().isEmpty()) {
                    //write quads
                    writeOutput("quad", tq.getTriples(), "nq", line);
                }
            }
        }
        catch( ParseException exp ) {
            // oops, something went wrong
            System.err.println( "Parsing failed. Reason: " + exp.getMessage() );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeOutput(String what, List<Quad> output, String extension, CommandLine line) {
        if (output.size() > 1) {
            //TODO
            //logger.info(`${output.length} ${what}s were generated.`);
        } else {
            //TODO
            //logger.info(`${output.length} ${what} were generated.`);
        }

        //if output file provided, write to triples output file
        if (line.hasOption("o")) {
            //TODO
            //logger.info(`Writing ${what}s to ${outputPath}...`);
            File outputFile = new File(line.getOptionValue("o") + "." + extension);

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
                //TODO
                //logger.info(`Writing to ${outputPath} is done.`);
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
