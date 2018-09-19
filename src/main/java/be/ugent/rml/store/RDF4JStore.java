package be.ugent.rml.store;

import be.ugent.rml.term.BlankNode;
import be.ugent.rml.term.Literal;
import be.ugent.rml.term.NamedNode;
import be.ugent.rml.term.Term;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.Models;

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
        throw new Error("Method getQuads() not implemented.");
    }

    @Override
    public List<Quad> getQuads(Term subject, Term predicate, Term object) {
        Model result;

        Resource filterSubject = getFilterSubject(subject);
        IRI filterPredicate = getFilterPredicate(predicate);
        Value filterObject = getFilterObject(object);

        result = model.filter(filterSubject, filterPredicate, filterObject);

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

    public Model getModel() {
        return model;
    }

    public boolean equals(Object o) {
        if (o instanceof RDF4JStore) {
            RDF4JStore otherStore = (RDF4JStore) o;

            return Models.isomorphic(model, otherStore.getModel());
        } else {
            return false;
        }
    }

    public void removeQuads(Term subject, Term predicate, Term object, Term graph) {
        throw new Error("Method removeQuads() not implemented.");
    }

    public void removeQuads(Term subject, Term predicate, Term object) {
        Resource filterSubject = getFilterSubject(subject);
        IRI filterPredicate = getFilterPredicate(predicate);
        Value filterObject = getFilterObject(object);

        model.remove(filterSubject, filterPredicate, filterObject);
    }

    private Resource getFilterSubject(Term subject) {
        if (subject != null) {
            ValueFactory vf = SimpleValueFactory.getInstance();

            if (subject instanceof BlankNode) {
                return vf.createBNode(subject.getValue());
            } else {
                return vf.createIRI(subject.getValue());
            }
        } else {
            return null;
        }
    }

    private IRI getFilterPredicate(Term predicate) {
        if (predicate != null) {
            ValueFactory vf = SimpleValueFactory.getInstance();

            return vf.createIRI(predicate.getValue());
        } else {
            return null;
        }
    }

    private Value getFilterObject(Term object) {
        if (object != null) {
            ValueFactory vf = SimpleValueFactory.getInstance();

            if (object instanceof BlankNode) {
                return vf.createBNode(object.getValue());
            } else if (object instanceof Literal) {
                return vf.createLiteral(object.getValue());
            } else {
                return vf.createIRI(object.getValue());
            }
        } else {
            return null;
        }
    }

    private Term convertStringToTerm(String str) {
        if (str.startsWith("_:")) {
            return new BlankNode(str.replace("_:", ""));
        } else if (str.startsWith("\"")) {
            Pattern pattern;
            if (str.contains("@")) {
                pattern = Pattern.compile("^\"([^\"]*)\"@<([^>]*)>");
            } else if (str.contains("^^")) {
                pattern = Pattern.compile("^\"([^\"]*)\"\\^\\^<([^>]*)>");
            } else {
                pattern = Pattern.compile("^\"([^\"]*)\"");
            }
            Matcher matcher = pattern.matcher(str);

            if (matcher.find()) {
                if (str.contains("@")) {
                    return new Literal(matcher.group(1), matcher.group(2));
                } else if (str.contains("^^")) {
                    return new Literal(matcher.group(1), new NamedNode(matcher.group(2)));
                } else {
                    return new Literal(matcher.group(1));
                }
            } else {
                throw new Error("Invalid Literal: " + str);
            }
        } else {
            return new NamedNode(str);
        }
    }
}
