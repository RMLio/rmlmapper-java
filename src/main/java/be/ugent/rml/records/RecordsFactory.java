package be.ugent.rml.records;

import be.ugent.rml.DataFetcher;
import be.ugent.rml.NAMESPACES;
import be.ugent.rml.Utils;
import be.ugent.rml.store.QuadStore;
import org.apache.commons.lang.NotImplementedException;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecordsFactory {

    private DataFetcher dataFetcher;
    private Map<String, List<Record>> allCSVRecords;
    private Map<String, List<Record>> allJSONRecords;
    private Map<String, List<Record>> allXMLRecords;

    public RecordsFactory(DataFetcher dataFetcher) {
        this.dataFetcher = dataFetcher;
        allCSVRecords = new HashMap<String, List<Record>>();
        allJSONRecords = new HashMap<String, List<Record>>();
        allXMLRecords = new HashMap<String, List<Record>>();
    }

    public List<Record> createRecords(String triplesMap, QuadStore rmlStore) throws IOException {
        //get logical source
        List<String> logicalSources = Utils.getObjectsFromQuads(rmlStore.getQuads(triplesMap, NAMESPACES.RML + "logicalSource", null));

        if (!logicalSources.isEmpty()) {
            String logicalSource = logicalSources.get(0);
            //get referenceformulation
            List<String> referenceFormulations = Utils.getObjectsFromQuads(rmlStore.getQuads(logicalSource, NAMESPACES.RML + "referenceFormulation", null));
            List<String> sources = Utils.getObjectsFromQuads(rmlStore.getQuads(logicalSource, NAMESPACES.RML + "source", null));
            List<String> iterators = Utils.getObjectsFromQuads(rmlStore.getQuads(logicalSource, NAMESPACES.RML + "iterator", null));

            if (referenceFormulations.isEmpty()) {
                throw new Error("The Logical Source of " + triplesMap + " does not have a reference formulation.");
            } else if (sources.isEmpty()) {
                throw new Error("The Logical Source of " + triplesMap + " does not have a source.");
            } else {
                String source = Utils.getLiteral(sources.get(0));

                if (referenceFormulations.get(0).equals(NAMESPACES.QL + "CSV")) {
                    if (allCSVRecords.containsKey(source)){
                        return allCSVRecords.get(source);
                    } else {
                        try {
                            CSV csv = new CSV();
                            allCSVRecords.put(source, csv.get(source, dataFetcher.getCwd()));
                        } catch (IOException e) {
                            throw e;
                        }

                        return allCSVRecords.get(source);
                    }
                } else if (referenceFormulations.get(0).equals(NAMESPACES.QL + "XPath")) {
                    if (!iterators.isEmpty()) {
                        String iterator = Utils.getLiteral(iterators.get(0));
                        String key = source + iterator;

                        if (allXMLRecords.containsKey(key)) {
                            return allXMLRecords.get(key);
                        } else {
                            try {
                                XML xml = new XML();
                                allXMLRecords.put(key, xml.get(source, iterator, dataFetcher.getCwd()));
                            } catch (IOException e) {
                                throw e;
                            }

                            return allXMLRecords.get(key);
                        }
                    } else {
                        throw new Error("The Logical Source of " + triplesMap + "does not have iterator, while this is expected for XPath.");
                    }
                } else if (referenceFormulations.get(0).equals(NAMESPACES.QL + "JSONPath")) {
                    if (!iterators.isEmpty()) {
                        String iterator = Utils.getLiteral(iterators.get(0));
                        String key = source + iterator;

                        if (allJSONRecords.containsKey(key)) {
                            return allJSONRecords.get(key);
                        } else {
                            try {
                                JSON json = new JSON();
                                allJSONRecords.put(key, json.get(source, iterator, dataFetcher.getCwd()));
                            } catch (IOException e) {
                                throw e;
                            }

                            return allJSONRecords.get(key);
                        }
                    } else {
                        throw new Error("The Logical Source of " + triplesMap + "does not have iterator, while this is expected for JSONPath.");
                    }
                } else {
                    throw new NotImplementedException();
                }
            }
        } else {
            throw new Error("No Logical Source is found for " + triplesMap + ". Exact one Logical Source is required per Triples Map.");
        }
    }
}
