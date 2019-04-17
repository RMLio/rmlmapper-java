package be.ugent.rml.records;

import be.ugent.rml.Utils;
import com.opencsv.CSVParser;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

import java.io.File;
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
        return get(path, System.getProperty("user.dir"), new CSVParser());
    }

    public List<Record> get(String path, String cwd) throws IOException {
        Reader reader = Utils.getReaderFromLocation(path, new File(cwd), getContentType());
        return _get(reader);
    }

    public List<Record> get(String path, CSVParser parser) throws IOException {
        return get(path, System.getProperty("user.dir"), parser);
    }

    public List<Record> get(String path, String cwd, CSVParser parser) throws IOException {
        Reader reader = Utils.getReaderFromLocation(path, new File(cwd), getContentType());
        return _get(reader, parser);
    }

    public List<Record> _get(Reader reader, CSVParser parser) throws IOException {
        CSVReader csvReader = new CSVReaderBuilder(reader)
                .withCSVParser(parser)
                .build();
        return _get(csvReader);
    }

    public List<Record> _get(Reader reader) throws IOException {
        CSVReader csvReader = new CSVReader(reader);
        return _get(csvReader);
    }

    public List<Record> _get(CSVReader csvReader) throws IOException {
        List<String[]> myEntries = csvReader.readAll();
        List<Record> records = new ArrayList<>();

        String[] headers = myEntries.get(0);

        for (int i = 1; i < myEntries.size(); i ++) {
            HashMap<String, List<Object>> values = new HashMap<>();

            for (int j = 0; j < headers.length; j ++) {
                List<Object> temp = new ArrayList<>();
                temp.add(myEntries.get(i)[j]);
                values.put(headers[j], temp);
            }

            records.add(new CSVRecord(values));
        }

        return records;
    }
}
