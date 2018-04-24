package be.ugent.rml.store;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
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

        if (subject == null && predicate == null && object == null) {
            result = model.filter(null, null, null);
        } else {
            if (object == null) {
                if (subject == null) {
                    result = model.filter(null, vf.createIRI(predicate), null);
                } else if (subject.startsWith("_:")) {
                    result = model.filter(vf.createBNode(subject.replaceFirst("_:", "")), vf.createIRI(predicate), null);
                } else {
                    result = model.filter(vf.createIRI(subject), vf.createIRI(predicate), null);
                }
            } else if (object.startsWith("\"")) {
                result = model.filter(vf.createIRI(subject), vf.createIRI(predicate), vf.createLiteral(object));
            } else {
                if (subject == null) {
                    result = model.filter(null, vf.createIRI(predicate), vf.createIRI(object));
                } else {
                    result = model.filter(vf.createIRI(subject), vf.createIRI(predicate), vf.createIRI(object));
                }
            }
        }

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
