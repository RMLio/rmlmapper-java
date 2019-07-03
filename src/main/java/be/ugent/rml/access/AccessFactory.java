package be.ugent.rml.access;

import be.ugent.rml.NAMESPACES;
import be.ugent.rml.Utils;
import be.ugent.rml.records.SPARQL;
import be.ugent.rml.store.QuadStore;
import be.ugent.rml.term.Literal;
import be.ugent.rml.term.NamedNode;
import be.ugent.rml.term.Term;
import org.apache.commons.lang.NotImplementedException;

import java.util.List;

import static be.ugent.rml.Utils.isRemoteFile;

public class AccessFactory {

    private String basePath;

    public AccessFactory(String basePath) {
        this.basePath = basePath;
    }

    public Access getAccess(Term logicalSource, QuadStore rmlStore) {
        List<Term> sources = Utils.getObjectsFromQuads(rmlStore.getQuads(logicalSource, new NamedNode(NAMESPACES.RML + "source"), null));
        Access access = null;

        if (!sources.isEmpty()) {
            Term source = sources.get(0);

            if (sources.get(0) instanceof Literal) {
                String value = sources.get(0).getValue();

                if (isRemoteFile(value)) {
                    access = new RemoteFileAccess(value);
                } else {
                    access = new LocalFileAccess(value, this.basePath);
                }
            } else {
                List<Term> sourceType = Utils.getObjectsFromQuads(rmlStore.getQuads(source, new NamedNode(NAMESPACES.RDF + "type"), null));

                switch(sourceType.get(0).getValue()) {
                    case NAMESPACES.D2RQ + "Database":  // RDBs
                        // Check if SQL version is given
                        List<Term> sqlVersion = Utils.getObjectsFromQuads(rmlStore.getQuads(logicalSource, new NamedNode(NAMESPACES.RR + "sqlVersion"), null));

                        if (sqlVersion.isEmpty()) {
                            throw new Error("No SQL version identifier detected.");
                        }

                        // TODO support RDBs

                        //return getRDBsRecords(rmlStore, source, logicalSource, triplesMap, tables, referenceFormulations);
                        break;
                    case NAMESPACES.SD + "Service":  // SPARQL
                        // Check if SPARQL Endpoint is given
                        List<Term> endpoint = Utils.getObjectsFromQuads(rmlStore.getQuads(source, new NamedNode(NAMESPACES.SD + "endpoint"),
                                null));

                        if (endpoint.isEmpty()) {
                            throw new Error("No SPARQL endpoint detected.");
                        }

                        // Get query
                        List<Term> query = Utils.getObjectsFromQuads(rmlStore.getQuads(logicalSource, new NamedNode(NAMESPACES.RML + "query"), null));
                        if (query.isEmpty()) {
                            throw new Error("No SPARQL query detected");
                        }

                        List<Term> referenceFormulations = Utils.getObjectsFromQuads(rmlStore.getQuads(logicalSource, new NamedNode(NAMESPACES.RML + "referenceFormulation"), null));

                        // Get result format
                        List<Term> resultFormatObject = Utils.getObjectsFromQuads(rmlStore.getQuads(source, new NamedNode(NAMESPACES.SD + "resultFormat"), null));
                        SPARQL.ResultFormat resultFormat = getSPARQLResultFormat(resultFormatObject, referenceFormulations);

                        String queryString = query.get(0).getValue().replaceAll("[\r\n]+", " ").trim();

                        access = new SPARQLEndpointAccess(resultFormat.getMediaType(), endpoint.get(0).getValue(), queryString);;

                        break;
                    case NAMESPACES.CSVW + "Table": // CSVW
                        List<Term> urls = Utils.getObjectsFromQuads(rmlStore.getQuads(source, new NamedNode(NAMESPACES.CSVW + "url"), null));

                        //TODO check whether there are actually urls or not
                        String value = urls.get(0).getValue();

                        if (isRemoteFile(value)) {
                            access = new RemoteFileAccess(value);
                        } else {
                            access = new LocalFileAccess(value, this.basePath);
                        }

                        break;
                    default:
                        throw new NotImplementedException();
                }
            }

            return access;
        } else {
            throw new Error("The Logical Source does not have a source.");
        }
    }

    private SPARQL.ResultFormat getSPARQLResultFormat(List<Term> resultFormat, List<Term> referenceFormulation) {
        if (resultFormat.isEmpty() && referenceFormulation.isEmpty()) {     // This will never be called atm but may come in handy later
            throw new Error("Please specify the sd:resultFormat of the SPARQL endpoint or a rml:referenceFormulation.");
        } else if (referenceFormulation.isEmpty()) {
            for (SPARQL.ResultFormat format: SPARQL.ResultFormat.values()) {
                if (resultFormat.get(0).getValue().equals(format.getUri())) {
                    return format;
                }
            }
            // No matching SPARQL.ResultFormat found
            throw new Error("Unsupported sd:resultFormat: " + resultFormat.get(0));

        } else if (resultFormat.isEmpty()) {
            for (SPARQL.ResultFormat format: SPARQL.ResultFormat.values()) {
                if (format.getReferenceFormulations().contains(referenceFormulation.get(0).getValue())) {
                    return format;
                }
            }
            // No matching SPARQL.ResultFormat found
            throw new Error("Unsupported rml:referenceFormulation for a SPARQL source.");

        } else {
            for (SPARQL.ResultFormat format : SPARQL.ResultFormat.values()) {
                if (resultFormat.get(0).getValue().equals(format.getUri())
                        && format.getReferenceFormulations().contains(referenceFormulation.get(0).getValue())) {
                    return format;
                }
            }
            throw new Error("Format specified in sd:resultFormat doesn't match the format specified in rml:referenceFormulation.");
        }
    }
}
