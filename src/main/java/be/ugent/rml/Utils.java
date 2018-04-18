package be.ugent.rml;

import be.ugent.rml.records.Record;
import be.ugent.rml.store.Quad;

import java.util.ArrayList;
import java.util.List;

public class Utils {

    public static List<String> applyTemplate(String template, Record record) {
        return null;
    }

    public static List<String> getSubjectsFromQuads(List<Quad> quads) {
        ArrayList<String> subjects = new ArrayList<String>();

        for (Quad quad : quads) {
            subjects.add(quad.getSubject());
        }

        return subjects;
    }

    public static List<String> getObjectsFromQuads(List<Quad> quads) {
        ArrayList<String> objects = new ArrayList<String>();

        for (Quad quad : quads) {
            objects.add(quad.getObject());
        }

        return objects;
    }

    public static List<String> getLiteralObjectsFromQuads(List<Quad> quads) {
        ArrayList<String> objects = new ArrayList<String>();

        for (Quad quad : quads) {
            objects.add(getLiteral(quad.getObject()));
        }

        return objects;
    }

    public static String getLiteral(String value) {
        return value;
    }
}
