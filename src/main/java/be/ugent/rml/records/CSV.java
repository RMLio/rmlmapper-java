package be.ugent.rml.records;

import be.ugent.rml.Utils;
import com.opencsv.CSVReader;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CSV {

    public List<Record> get(String path) throws IOException {
        return get(path, System.getProperty("user.dir"));
    }

    public List<Record> get(String path, String cwd) throws IOException {
        File file = Utils.getFile(path, new File(cwd));

        return _get(file);
    }

    private List<Record> _get(File file) throws IOException {
        CSVReader reader = new CSVReader(new FileReader(file));
        List<String[]> myEntries = reader.readAll();
        List<Record> records = new ArrayList<Record>();

        String[] headers = myEntries.get(0);

        for (int i = 1; i < myEntries.size(); i ++) {
            HashMap<String, List<String>> values= new HashMap<String, List<String>>();

            for (int j = 0; j < headers.length; j ++) {
                List<String> temp = new ArrayList<String>();
                temp.add(myEntries.get(i)[j]);
                values.put(headers[j], temp);
            }

            records.add(new CSVRecord(values));
        }

        return records;
    }
}
