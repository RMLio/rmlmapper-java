package be.ugent.rml.records;

import be.ugent.rml.Utils;
import com.opencsv.CSVReader;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CSV {

    protected String getContentType() {
        return "text/csv";
    }

    public List<Record> get(String path) throws IOException {
        return get(path, System.getProperty("user.dir"));
    }

    public List<Record> get(String path, String cwd) throws IOException {
        Reader reader = Utils.getReaderFromLocation(path, new File(cwd), getContentType());

        return _get(reader);
    }

    public List<Record> _get(Reader reader) throws IOException {
        CSVReader csvReader = new CSVReader(reader);

        List<String[]> myEntries = csvReader.readAll();
        List<Record> records = new ArrayList<Record>();

        String[] headers = myEntries.get(0);

        for (int i = 1; i < myEntries.size(); i ++) {
            HashMap<String, List<String>> values = new HashMap<String, List<String>>();

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
