package be.ugent.rml.records;

import be.ugent.rml.*;
import be.ugent.rml.store.QuadStore;
import be.ugent.rml.term.NamedNode;
import be.ugent.rml.term.Term;
import org.apache.commons.lang.NotImplementedException;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecordsFactory {

    private DataFetcher dataFetcher;
    private Map<String, List<Record>> allCSVRecords;
    private Map<String, Map<String, List<Record>>> allJSONRecords;
    private Map<String, Map<String, List<Record>>> allXMLRecords;

    public RecordsFactory(DataFetcher dataFetcher) {
        this.dataFetcher = dataFetcher;
        allCSVRecords = new HashMap<>();
        allJSONRecords = new HashMap<>();
        allXMLRecords = new HashMap<>();
    }

    public List<Record> createRecords(Term triplesMap, QuadStore rmlStore) throws IOException {
        //get logical source
        List<Term> logicalSources = Utils.getObjectsFromQuads(rmlStore.getQuads(triplesMap, new NamedNode(NAMESPACES.RML + "logicalSource"), null));

        if (!logicalSources.isEmpty()) {
            Term logicalSource = logicalSources.get(0);
            //get referenceformulation
            List<Term> referenceFormulations = Utils.getObjectsFromQuads(rmlStore.getQuads(logicalSource, new NamedNode(NAMESPACES.RML + "referenceFormulation"), null));
            List<Term> sources = Utils.getObjectsFromQuads(rmlStore.getQuads(logicalSource, new NamedNode(NAMESPACES.RML + "source"), null));
            List<Term> iterators = Utils.getObjectsFromQuads(rmlStore.getQuads(logicalSource, new NamedNode(NAMESPACES.RML + "iterator"), null));

            if (referenceFormulations.isEmpty()) {
                throw new Error("The Logical Source of " + triplesMap + " does not have a reference formulation.");
            } else if (sources.isEmpty()) {
                throw new Error("The Logical Source of " + triplesMap + " does not have a source.");
            } else {
                String source = sources.get(0).getValue();

                if (referenceFormulations.get(0).equals(new NamedNode(NAMESPACES.QL + "CSV"))) {
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
                } else if (referenceFormulations.get(0).equals(new NamedNode(NAMESPACES.QL + "XPath"))) {
                    if (!iterators.isEmpty()) {
                        String iterator = iterators.get(0).getValue();

                        if (allXMLRecords.containsKey(source) && allXMLRecords.get(source).containsKey(iterator)) {
                            return allXMLRecords.get(source).get(iterator);
                        } else {
                            try {
                                XML xml = new XML();
                                List<Record> records = xml.get(source, iterator, dataFetcher.getCwd());

                                if (allXMLRecords.containsKey(source)) {
                                    allXMLRecords.get(source).put(iterator, records);
                                } else {
                                    Map<String, List<Record>> temp = new HashMap<>();
                                    temp.put(iterator, records);
                                    allXMLRecords.put(source, temp);
                                }

                                return records;
                            } catch (IOException e) {
                                throw e;
                            }
                        }
                    } else {
                        throw new Error("The Logical Source of " + triplesMap + "does not have iterator, while this is expected for XPath.");
                    }
                } else if (referenceFormulations.get(0).equals(new NamedNode(NAMESPACES.QL + "JSONPath"))) {
                    if (!iterators.isEmpty()) {
                        String iterator = iterators.get(0).getValue();

                        if (allJSONRecords.containsKey(source) && allJSONRecords.get(source).containsKey(iterator)) {
                            return allJSONRecords.get(source).get(iterator);
                        } else {
                            try {
                                JSON json = new JSON();
                                List<Record> records = json.get(source, iterator, dataFetcher.getCwd());

                                if (allJSONRecords.containsKey(source)) {
                                    allJSONRecords.get(source).put(iterator, records);
                                } else {
                                    Map<String, List<Record>> temp = new HashMap<>();
                                    temp.put(iterator, records);
                                    allJSONRecords.put(source, temp);
                                }

                                return records;
                            } catch (IOException e) {
                                throw e;
                            }
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
