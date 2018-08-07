package be.ugent.rml.store;

import be.ugent.rml.BlankNode;
import be.ugent.rml.Literal;
import be.ugent.rml.NamedNode;
import be.ugent.rml.Term;
import be.ugent.rml.Utils;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RDF4JStore extends QuadStore {

    private Model model;

    public RDF4JStore(Model model) {
        this.model = model;
    }

    @Override
    public void removeDuplicates() {

    }

    @Override
    public void addTriple(Term subject, Term predicate, Term object) {

    }

    @Override
    public void addQuad(Term subject, Term predicate, Term object, Term graph) {

    }

    @Override
    public List<Quad> getQuads(Term subject, Term predicate, Term object, Term graph) {
        throw new NotImplementedException();
    }

    @Override
    public List<Quad> getQuads(Term subject, Term predicate, Term object) {
        ValueFactory vf = SimpleValueFactory.getInstance();
        Model result;

        Object filterSubject = null;
        Object filterPredicate = null;
        Object filterObject = null;

        if (subject != null) {
            if (subject instanceof BlankNode) {
                filterSubject= vf.createBNode(subject.getValue());
            } else {
                filterSubject = vf.createIRI(subject.getValue());
            }
        }

        if (predicate != null) {
            filterPredicate = vf.createIRI(predicate.getValue());
        }

        if (object != null) {
            if (object instanceof BlankNode) {
                filterObject = vf.createBNode(object.getValue());
            } else if (object instanceof Literal) {
                filterObject = vf.createLiteral(object.getValue());
            } else {
                filterObject = vf.createIRI(object.getValue());
            }
        }

        result = model.filter((Resource) filterSubject, (IRI) filterPredicate, (Value) filterObject);

        List<Quad> quads = new ArrayList<>();

        for (Statement st: result) {
            Term s = convertStringToTerm(st.getSubject().toString());
            Term p = convertStringToTerm(st.getPredicate().toString());
            Term o = convertStringToTerm(st.getObject().toString());

            if (st.getContext() == null) {
                quads.add(new Quad(s, p, o));
            } else {
                quads.add(new Quad(s, p, o, convertStringToTerm(st.getContext().toString())));
            }
        }

        return quads;
    }

    private Term convertStringToTerm(String str) {
        if (str.startsWith("_:")) {
            return new BlankNode(str.replace("_:", ""));
        } else if (str.startsWith("\"")) {
            Pattern pattern = Pattern.compile("^\"(.*)\"");
            Matcher matcher = pattern.matcher(str);

            if (matcher.find()) {
                return new Literal(matcher.group(1));
            } else {
                throw new Error("Invalid Literal: " + str);
            }
        } else {
            return new NamedNode(str);
        }
    }
}
