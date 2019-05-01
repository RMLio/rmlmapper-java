package be.ugent.rml.records;

import be.ugent.rml.Utils;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.*;

public class CSV {

    public static List<Record> get(InputStream inputStream, Charset charset, CSVFormat format) throws IOException {
        CSVParser parser = CSVParser.parse(inputStream, charset, format);

        List<org.apache.commons.csv.CSVRecord> myEntries = parser.getRecords();
        List<Record> records = new ArrayList<>();

        Set<String> headerSet = myEntries.get(0).toMap().keySet();
        String[] headers = headerSet.toArray(new String[headerSet.size()]);

        for (org.apache.commons.csv.CSVRecord myEntry : myEntries) {
            HashMap<String, List<Object>> values = new HashMap<>();

            for (int j = 0; j < headers.length; j++) {
                List<Object> temp = new ArrayList<>();
                temp.add(myEntry.get(j));
                values.put(headers[j], temp);
            }

            records.add(new CSVRecord(values));
        }

        return records;
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
