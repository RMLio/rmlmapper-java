package be.ugent.rml;

import be.ugent.rml.records.Record;
import be.ugent.rml.store.QuadStore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataFetcher {

    private String cwd;
    private QuadStore rmlStore;
    private Map<String, List<Record>> sources;

    public DataFetcher(QuadStore rmlStore) {
        this(System.getProperty("user.dir"), rmlStore);
    }

    public DataFetcher(String cwd, QuadStore rmlStore) {
        this.cwd = cwd;
        this.rmlStore = rmlStore;
        sources = new HashMap<String, List<Record>>();
    }

    public void add(String id, List<Record> data) {
        sources.put(id, data);
    }

    public List<Record> get(String id) {
        //if data is already loaded before, we return it immediately
        if (!sources.containsKey(id)) {
            sources.put(id, getData(id));
        }

        return sources.get(id);
    }

    private List<Record> getData(String id) {
        String originalID = id;
        return null;
    }

    public String getCwd() {
        return cwd;
    }
}
