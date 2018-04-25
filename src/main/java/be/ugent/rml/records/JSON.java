package be.ugent.rml.records;

import com.jayway.jsonpath.*;
import net.minidev.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JSON {

    public List<Record> get(String path, String iterator) throws IOException {
        return get(path, iterator, System.getProperty("user.dir"));
    }

    public List<Record> get(String path, String iterator, String cwd) throws IOException {
        File file = new File(path);

        if (!file.isAbsolute()) {
            path = cwd + "/" + path;
        }

        return _get(path, iterator);
    }

    private List<Record> _get(String path, String iterator) throws IOException {
        List<Record> records = new ArrayList<>();
        File initialFile = new File(path);
        InputStream targetStream = new FileInputStream(initialFile);

        Object document = Configuration.defaultConfiguration().jsonProvider().parse(targetStream, "utf-8");

        Configuration conf = Configuration.builder()
                .options(Option.AS_PATH_LIST).build();

        try {
            List<String> pathList = JsonPath.using(conf).parse(document).read(iterator);

            for(String p :pathList) {
                records.add(new JSONRecord(document, p));
            }
        } catch(PathNotFoundException e) {
            //TODO logger warn
        }

        return records;
    }
}
