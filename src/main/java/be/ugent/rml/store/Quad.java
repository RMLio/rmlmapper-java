package be.ugent.rml.store;

public class Quad implements Comparable<Quad> {

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

    @Override
    public int compareTo(Quad o) {
        int compareGraph;
        String oGraph = o.getGraph();
        if (this.graph == null && oGraph == null) {
            compareGraph = 0;
        } else {
            if (this.graph == null) {
                compareGraph = -1;
            } else if (oGraph == null) {
                compareGraph = 1;
            } else {
                compareGraph = this.graph.compareTo(oGraph);
            }
        }
        if (compareGraph == 0) {
            int compareSubject = this.subject.compareTo(o.getSubject());
            if (compareSubject == 0) {
                int comparePredicate = this.predicate.compareTo(o.getPredicate());
                if (comparePredicate == 0) {
                    return this.object.compareTo(o.getObject());
                }
                return comparePredicate;
            } else {
                return compareSubject;
            }
        } else {
            return compareGraph;
        }
    }
}
