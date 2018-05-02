package be.ugent.rml.store;

import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

import java.util.ArrayList;
import java.util.List;

public class RDF4JStore extends QuadStore {

    private Model model;

    public RDF4JStore(Model model) {
        this.model = model;
    }

    @Override
    public void removeDuplicates() {

    }

    @Override
    public void addTriple(String subject, String predicate, String object) {

    }

    @Override
    public void addQuad(String subject, String predicate, String object, String graph) {

    }

    @Override
    public List<Quad> getQuads(String subject, String predicate, String object, String graph) {
        return null;
    }

    @Override
    public List<Quad> getQuads(String subject, String predicate, String object) {
        ValueFactory vf = SimpleValueFactory.getInstance();
        Model result;

        Object filterSubject = null;
        Object filterPredicate = null;
        Object filterObject = null;
        if (subject != null) {
            if (subject.startsWith("_:")) {
                filterSubject= vf.createBNode(subject.replaceFirst("_:", ""));
            } else {
                filterSubject = vf.createIRI(subject);
            }
        }

        if (predicate != null) {
            filterPredicate = vf.createIRI(predicate);
        }

        if (object != null) {
            if (object.startsWith("\"")) {
                filterObject = vf.createLiteral(object);
            } else {
                filterObject = vf.createIRI(object);
            }
        }

        result = model.filter((Resource) filterSubject, (IRI) filterPredicate, (Value) filterObject);

        List<Quad> quads = new ArrayList<>();

        for (Statement st: result) {
            String s = st.getSubject().toString();
            String p = st.getPredicate().toString();
            String o = st.getObject().toString();
            quads.add(new Quad(s, p, o));
        }

        return quads;
    }
}
