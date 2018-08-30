package be.ugent.rml.store;

import be.ugent.rml.term.BlankNode;
import be.ugent.rml.term.Literal;
import be.ugent.rml.term.NamedNode;
import be.ugent.rml.term.Term;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.impl.TreeModel;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RDF4JStore extends QuadStore {

    private Model model;
    private int triplesWithGraphCounter;
    private static final Logger logger = LoggerFactory.getLogger(RDF4JStore.class);

    public RDF4JStore(Model model) {
        this.model = model;
    }

    public RDF4JStore() {
        model = new TreeModel();
        triplesWithGraphCounter = 0;
    }

    @Override
    public void removeDuplicates() {

    }

    @Override
    public void addQuad(Term subject, Term predicate, Term object, Term graph) {
        ValueFactory vf = SimpleValueFactory.getInstance();
        Resource s = getRDF4JSubject(subject, vf);
        IRI p = getRDF4JPredicate(predicate, vf);
        Value o = getRDF4JObject(object, vf);
        Resource g = getRDF4JGraph(graph, vf);

        model.add(s, p, o, g);

        if (g != null) {
            triplesWithGraphCounter ++;
        }
    }

    @Override
    public List<Quad> getQuads(Term subject, Term predicate, Term object, Term graph) {
        throw new Error("Method getQuads() not implemented.");
    }

    @Override
    public List<Quad> getQuads(Term subject, Term predicate, Term object) {
        ValueFactory vf = SimpleValueFactory.getInstance();
        Model result;

        Resource filterSubject = getRDF4JSubject(subject, vf);
        IRI filterPredicate = getRDF4JPredicate(predicate, vf);
        Value filterObject = getRDF4JObject(object, vf);

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

    public void toTrig(Writer out) {
        Rio.write(model, out, RDFFormat.TRIG);
    }

    public void toTrix(Writer out) {
        Rio.write(model, out, RDFFormat.TRIX);
    }

    public void toJSONLD(Writer out) {
        Rio.write(model, out, RDFFormat.JSONLD);
    }

    @Override
    public void toNQuads(Writer out) {
        Rio.write(model, out, RDFFormat.NQUADS);
    }

    public void toTurtle(Writer out) {
        Rio.write(model, out, RDFFormat.TURTLE);

        if (triplesWithGraphCounter > 0) {
            logger.warn("There are graphs generated. However, Turtle does not support graphs. Use Trig instead.");
        }
    }

    public void setNamespaces(Set<Namespace> namespaces) {
        namespaces.forEach(namespace -> {
            model.setNamespace(namespace);
        });
    }

    public Set<Namespace> getNamespaces() {
        return model.getNamespaces();
    }

    @Override
    public boolean isEmpty() {
        return model.isEmpty();
    }

    public int size() {
        return model.size();
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

    private Resource getRDF4JSubject(Term subject, ValueFactory vf) {
        if (subject != null) {
            if (subject instanceof BlankNode) {
                return vf.createBNode(subject.getValue());
            } else {
                return vf.createIRI(subject.getValue());
            }
        } else {
            return null;
        }
    }

    private IRI getRDF4JPredicate(Term predicate, ValueFactory vf) {
        if (predicate != null) {
            return vf.createIRI(predicate.getValue());
        } else {
            return null;
        }
    }

    private Value getRDF4JObject(Term object, ValueFactory vf) {
        if (object != null) {
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

    private Resource getRDF4JGraph(Term graph, ValueFactory vf) {
        return getRDF4JSubject(graph, vf);
    }
}
