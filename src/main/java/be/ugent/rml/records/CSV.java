package be.ugent.rml.records;

import com.opencsv.CSVReader;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CSV {

    public static List<Record> get(String path) throws IOException {
        File file = new File(path);

        if (!file.isAbsolute()) {
            path = System.getProperty("user.dir") + "/" + path;
        }

        CSVReader reader = new CSVReader(new FileReader(path));
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
