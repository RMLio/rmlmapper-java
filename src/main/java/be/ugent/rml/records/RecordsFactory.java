package be.ugent.rml.records;

import be.ugent.rml.DataFetcher;
import be.ugent.rml.NAMESPACES;
import be.ugent.rml.Utils;
import be.ugent.rml.store.QuadStore;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecordsFactory {

    private DataFetcher dataFetcher;
    private Map<String, List<Record>> allCSVRecords;

    public RecordsFactory(DataFetcher dataFetcher) {
        this.dataFetcher = dataFetcher;
        allCSVRecords = new HashMap<String, List<Record>>();
    }

    public List<Record> createRecords(String triplesMap, QuadStore rmlStore) {
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
                String source = sources.get(0);

                if (referenceFormulations.get(0).equals(NAMESPACES.QL + "CSV")) {
                    if (allCSVRecords.containsKey(source)){
                        return allCSVRecords.get(source);
                    } else {
                        try {
                            allCSVRecords.put(source, CSV.get(source));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        return allCSVRecords.get(source);
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
