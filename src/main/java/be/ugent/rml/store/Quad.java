package be.ugent.rml.store;

public class Quad {

    private String subject, predicate, object, graph;

    public Quad(String subject, String predicate, String object, String graph) {
        this.subject = subject;
        this.predicate = predicate;
        this.object = object;
        this.graph = graph;
    }

    public Quad(String subject, String predicate, String object) {
        this(subject, predicate, object, null);
    }

    public String getSubject() {
        return subject;
    }

    public String getPredicate() {
        return predicate;
    }

    public String getObject() {
        return object;
    }

    public String getGraph() {
        return graph;
    }
}
