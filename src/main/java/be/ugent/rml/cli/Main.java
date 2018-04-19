package be.ugent.rml.cli;

import be.ugent.rml.DataFetcher;
import be.ugent.rml.Executor;
import be.ugent.rml.functions.FunctionLoader;
import be.ugent.rml.records.RecordsFactory;
import be.ugent.rml.store.QuadStore;
import be.ugent.rml.store.RDF4JStore;
import org.apache.commons.cli.*;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class Main {

    public static void main(String [] args) {
        Options options = new Options();
        Option mappingdoc = Option.builder("m")
                .longOpt( "mapping" )
                .hasArg()
                .desc(  "path to mapping document" )
                .build();
        options.addOption(mappingdoc);

        // create the parser
        CommandLineParser parser = new DefaultParser();
        try {
            // parse the command line arguments
            CommandLine line = parser.parse( options, args );

            if (line.hasOption("m")){
                System.out.println(System.getProperty("user.dir") + "/" + line.getOptionValue("m"));
                File mappingFile = new File(System.getProperty("user.dir") + "/" + line.getOptionValue("m"));
                InputStream mappingStream = new FileInputStream(mappingFile);

                Model model = Rio.parse(mappingStream, "", RDFFormat.TURTLE);
                RDF4JStore rmlStore = new RDF4JStore(model);

                Executor executor = new Executor(rmlStore, new RecordsFactory(new DataFetcher(System.getProperty("user.dir"), rmlStore)), new FunctionLoader());
                QuadStore result = executor.execute(null);

                System.out.println(result.toString());
            }
        }
        catch( ParseException exp ) {
            // oops, something went wrong
            System.err.println( "Parsing failed.  Reason: " + exp.getMessage() );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
