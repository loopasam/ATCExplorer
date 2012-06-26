/**
 * 
 */
package models;

import java.io.File;
import java.util.Collections;
import java.util.Set;

import javax.persistence.Entity;

import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntaxEditorParser;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.expression.OWLEntityChecker;
import org.semanticweb.owlapi.expression.ParserException;
import org.semanticweb.owlapi.expression.ShortFormEntityChecker;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.util.BidirectionalShortFormProvider;
import org.semanticweb.owlapi.util.BidirectionalShortFormProviderAdapter;
import org.semanticweb.owlapi.util.ShortFormProvider;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;

import play.db.jpa.Model;

/**
 * @author Samuel Croset
 *
 */
public class QueryEngine {

    private OWLOntology ontology;
    private OWLReasoner reasoner;
    private ShortFormProvider shortFormProvider;
    private OWLOntologyManager manager;
    private BidirectionalShortFormProvider bidiShortFormProvider;

    public void setBidiShortFormProvider(BidirectionalShortFormProvider bidiShortFormProvider) {
	this.bidiShortFormProvider = bidiShortFormProvider;
    }
    public BidirectionalShortFormProvider getBidiShortFormProvider() {
	return bidiShortFormProvider;
    }
    public OWLOntologyManager getManager() {
	return manager;
    }
    public void setManager(OWLOntologyManager manager) {
	this.manager = manager;
    }
    public OWLOntology getOntology() {
	return ontology;
    }
    public void setOntology(OWLOntology ontology) {
	this.ontology = ontology;
    }
    public OWLReasoner getReasoner() {
	return reasoner;
    }
    public void setReasoner(OWLReasoner reasoner) {
	this.reasoner = reasoner;
    }
    public ShortFormProvider getShortFormProvider() {
	return shortFormProvider;
    }
    public void setShortFormProvider(ShortFormProvider shortFormProvider) {
	this.shortFormProvider = shortFormProvider;
    }


    /**
     * @param file
     * @throws OWLOntologyCreationException 
     */
    public QueryEngine(File file) throws OWLOntologyCreationException {
	this.setManager(OWLManager.createOWLOntologyManager());
	this.setOntology(this.getManager().loadOntologyFromOntologyDocument(file));
	System.out.println("Loaded ontology: " + this.getOntology().getOntologyID());
//	OWLReasonerFactory reasonerFactory = new Reasoner.ReasonerFactory();
	OWLReasonerFactory reasonerFactory = new ElkReasonerFactory();
	this.setReasoner(reasonerFactory.createReasoner(this.getOntology()));
	this.getReasoner().precomputeInferences(InferenceType.CLASS_HIERARCHY);
	this.setShortFormProvider(new SimpleShortFormProvider());
	Set<OWLOntology> importsClosure = this.getOntology().getImportsClosure();
	this.setBidiShortFormProvider(new BidirectionalShortFormProviderAdapter(this.getManager(), importsClosure, this.getShortFormProvider()));
    }


    /**
     * Gets the subclasses of a class expression parsed from a string.
     * @param expression The string from which the class expression will be parsed.
     * @param direct Specifies whether direct subclasses should be returned or not.
     * @return 
     * @return The subclasses of the specified class expression
     * @throws ParserException If there was a problem parsing the class expression.
     */
    public Set<OWLClass> getSubClasses(String expression) {
	if (expression.trim().length() == 0) {
	    System.out.println("No class expression specified");
	    return Collections.emptySet();
	} else {
	    OWLClassExpression classExpression = this.parseClassExpression(expression.trim());
	    NodeSet<OWLClass> subClasses = this.getReasoner().getSubClasses(classExpression, true);
	    return subClasses.getFlattened();
	}
    }
    
    /**
     * Parses a class expression string to obtain a class expression.
     * @param expression The class expression string
     * @return The corresponding class expression
     * @throws ParserException if the class expression string is malformed or contains unknown entity names.
     */
    private OWLClassExpression parseClassExpression(String expression) {
        OWLDataFactory dataFactory = this.getOntology().getOWLOntologyManager().getOWLDataFactory();
        ManchesterOWLSyntaxEditorParser parser = new ManchesterOWLSyntaxEditorParser(dataFactory, expression);
        parser.setDefaultOntology(this.getOntology());
        OWLEntityChecker entityChecker = new ShortFormEntityChecker(bidiShortFormProvider);
        parser.setOWLEntityChecker(entityChecker);
        OWLClassExpression owlExpression = null;
	try {
	    owlExpression = parser.parseClassExpression();
	} catch (ParserException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
        return owlExpression;
    }

}
