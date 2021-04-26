package be.ugent.rml.store;

import be.ugent.rml.term.BlankNode;
import be.ugent.rml.term.Literal;
import be.ugent.rml.term.NamedNode;
import be.ugent.rml.term.Term;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.impl.TreeModel;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.rio.*;
import org.eclipse.rdf4j.rio.helpers.BasicParserSettings;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.NotSupportedException;
import java.io.InputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implementation of QuadStore with RDF4J
 * Package-private
 */
public class RDF4JStore extends QuadStore {

    private static final Logger logger = LoggerFactory.getLogger(RDF4JStore.class);
    private Model model;
    private int triplesWithGraphCounter;

    public RDF4JStore() {
        model = new TreeModel();
        triplesWithGraphCounter = 0;
    }

    @Override
    public void removeDuplicates() {
        /*
         * Model is an extension of the default Java Collection class java.util.Set<Statement>.
         * Sets don't contain any duplicates, thus no removal is needed.
         * See https://rdf4j.org/documentation/programming/model/
         */
    }

    @Override
    public void addQuad(Term subject, Term predicate, Term object, Term graph) {
        Resource s = getFilterSubject(subject);
        IRI p = getFilterPredicate(predicate);
        Value o = getFilterObject(object);
        Resource g = getFilterGraph(graph);

        model.add(s, p, o, g);

        if (g != null) {
            triplesWithGraphCounter++;
        }
    }

    @Override
    public List<Quad> getQuads(Term subject, Term predicate, Term object, Term graph) {
        Model result;
        Resource filterSubject = getFilterSubject(subject);
        IRI filterPredicate = getFilterPredicate(predicate);
        Value filterObject = getFilterObject(object);
        Resource filterGraph = getFilterGraph(graph);

        result = model.filter(filterSubject, filterPredicate, filterObject, filterGraph);

        List<Quad> quads = new ArrayList<>();

        for (Statement st : result) {
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

    @Override
    public void copyNameSpaces(QuadStore store) {
        if (store instanceof RDF4JStore) {
            RDF4JStore rdf4JStore = (RDF4JStore) store;

            rdf4JStore.getModel()
                    .getNamespaces()
                    .forEach(namespace -> model.setNamespace(namespace));
        } else {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public void read(InputStream is, String base, RDFFormat format) throws Exception {
        if (base == null) {
           base = "";
        }

        RDFParser parser = Rio.createParser(format);
        parser.set(BasicParserSettings.PRESERVE_BNODE_IDS, true);
        parser.setRDFHandler(new StatementCollector(model));
        parser.parse(is, base);
        is.close();
    }

    @Override
    public void write(Writer out, String format) throws Exception {
        switch (format) {
            case "turtle":
                Rio.write(model, out, RDFFormat.TURTLE);
                if (triplesWithGraphCounter > 0) {
                    logger.warn("There are graphs generated. However, Turtle does not support graphs. Use Trig instead.");
                }
                break;
            case "trig":
                Rio.write(model, out, RDFFormat.TRIG);
                break;
            case "trix":
                Rio.write(model, out, RDFFormat.TRIX);
                break;
            case "jsonld":
                Rio.write(model, out, RDFFormat.JSONLD);
                break;
            case "nquads":
                Rio.write(model, out, RDFFormat.NQUADS);
                break;
            case "ntriples":
                Rio.write(model, out, RDFFormat.NTRIPLES);
                break;
            default:
                throw new Exception("Serialization " + format + " not supported");
        }
    }

    @Override
    public boolean isEmpty() {
        return model.isEmpty();
    }

    @Override
    public int size() {
        return model.size();
    }

    /**
     * TODO remove all need for this. Currently:
     *  - store equality/isomorphism
     *  - namespace passing
     *  - store difference
     *  - store isSubset
     */
    public Model getModel() {
        return model;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof RDF4JStore) {
            RDF4JStore otherStore = (RDF4JStore) o;

            return Models.isomorphic(model, otherStore.getModel());
        } else {
            return false;
        }
    }

    @Override
    public void removeQuads(Term subject, Term predicate, Term object, Term graph) {
        Resource filterSubject = getFilterSubject(subject);
        IRI filterPredicate = getFilterPredicate(predicate);
        Value filterObject = getFilterObject(object);
        Resource filterGraph = getFilterGraph(graph);
        model.remove(filterSubject, filterPredicate, filterObject, filterGraph);
    }

    @Override
    public boolean contains(Term subject, Term predicate, Term object, Term graph) {
        Resource filterSubject = getFilterSubject(subject);
        IRI filterPredicate = getFilterPredicate(predicate);
        Value filterObject = getFilterObject(object);
        Resource filterGraph = getFilterGraph(graph);

        return model.contains(filterSubject, filterPredicate, filterObject, filterGraph);
    }

    @Override
    public boolean isIsomorphic(QuadStore store) {
        if (store instanceof RDF4JStore) {
            RDF4JStore rdf4JStore = (RDF4JStore) store;
            return Models.isomorphic(model, rdf4JStore.getModel());
        } else {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public boolean isSubset(QuadStore store) {
        if (store instanceof RDF4JStore) {
            RDF4JStore rdf4JStore = (RDF4JStore) store;
            return Models.isSubset(model, rdf4JStore.getModel());
        } else {
            throw new UnsupportedOperationException();
        }
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
            return SimpleValueFactory.getInstance().createIRI(predicate.getValue());
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
                Literal literal = (Literal) object;

                if (literal.getDatatype() != null) {
                    return vf.createLiteral(object.getValue(), vf.createIRI(literal.getDatatype().getValue()));
                } else if (literal.getLanguage() != null) {
                    return vf.createLiteral(object.getValue(), literal.getLanguage());
                } else {
                    return vf.createLiteral(object.getValue());
                }
            } else {
                return vf.createIRI(object.getValue());
            }
        } else {
            return null;
        }
    }

    private Resource getFilterGraph(Term graph) {
        return getFilterSubject(graph);
    }

    /**
     * Convert given string to Term
     *
     * @param str
     * @return
     */
    // TODO refactor Term class to use library (Jena/RDF4J) and have this built-in
    private Term convertStringToTerm(String str) {
        if (str.startsWith("_:")) {
            return new BlankNode(str.replace("_:", ""));
        } else if (str.startsWith("\"\"\"")) {
            // Triple quoted literal
            return new Literal(str.substring(4, str.length() - 3));
        } else if (str.startsWith("\"")) {
            Pattern pattern;
            boolean hasLanguage = str.contains("@") && str.lastIndexOf("@") > str.lastIndexOf("\"");
            boolean hasDatatype = str.contains("^^");
            if (hasLanguage) {
                pattern = Pattern.compile("^\"([^\"]*)\"@([^@]*)");
            } else if (hasDatatype) {
                pattern = Pattern.compile("^\"([^\"]*)\"\\^\\^<([^>]*)>");
            } else {
                pattern = Pattern.compile("^\"(.*)\"$", Pattern.DOTALL);
            }

            Matcher matcher = pattern.matcher(str);

            if (matcher.find()) {
                if (hasLanguage) {
                    return new Literal(matcher.group(1), matcher.group(2));
                } else if (hasDatatype) {
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
