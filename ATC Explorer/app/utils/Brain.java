/**
 * 
 */
package utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntaxEditorParser;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.expression.OWLEntityChecker;
import org.semanticweb.owlapi.expression.ParserException;
import org.semanticweb.owlapi.expression.ShortFormEntityChecker;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyFactory;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.util.BidirectionalShortFormProvider;
import org.semanticweb.owlapi.util.BidirectionalShortFormProviderAdapter;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import org.semanticweb.owlapi.util.ShortFormProvider;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import play.vfs.VirtualFile;

/**
 * @author Samuel Croset
 *
 */
public class Brain {

    public static OWLOntology ontology;
    public static OWLReasoner reasoner;
    public static OWLOntologyManager manager;
    public static OWLDataFactory factory;
    public static OWLReasonerFactory reasonerFactory;
    public static String base;
    public static ShortFormProvider shortFormProvider;
    public static BidirectionalShortFormProvider bidiShortFormProvider;
    public static OWLEntityChecker entityChecker;


    public static void learn(String pathToFile, String base) throws OWLOntologyCreationException {
	VirtualFile vf = VirtualFile.fromRelativePath(pathToFile);
	File file = vf.getRealFile();
	manager = OWLManager.createOWLOntologyManager();
	factory = manager.getOWLDataFactory();
	ontology = manager.loadOntologyFromOntologyDocument(file);
	System.out.println("Loaded ontology: " + ontology.getOntologyID());
	reasonerFactory = new ElkReasonerFactory();
	reasoner = reasonerFactory.createReasoner(ontology);
	reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
	Brain.base = base;
	shortFormProvider = new SimpleShortFormProvider();
	Set<OWLOntology> importsClosure = ontology.getImportsClosure();
	bidiShortFormProvider = new BidirectionalShortFormProviderAdapter(manager, importsClosure, shortFormProvider);
	entityChecker = new ShortFormEntityChecker(bidiShortFormProvider);
    }

    public static String getLabel(String id) {
	OWLClass owlClass = (OWLClass) bidiShortFormProvider.getEntity(id);
	OWLAnnotationProperty label = factory.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI());
	String standardizedLabel = null;
	for (OWLAnnotation annotation : owlClass.getAnnotations(ontology, label)) {
	    if (annotation.getValue() instanceof OWLLiteral) {
		OWLLiteral val = (OWLLiteral) annotation.getValue();
		standardizedLabel = val.getLiteral().substring(0, 1).toUpperCase() + val.getLiteral().substring(1).toLowerCase();

	    }
	}
	return standardizedLabel;
    }

    private static String getSeeAlso(String id) {
	OWLClass owlClass = (OWLClass) bidiShortFormProvider.getEntity(id);
	if(owlClass == null){
	    return "No see also";
	}
	OWLAnnotationProperty seeAlso = factory.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_SEE_ALSO.getIRI());
	String labelValue = null;
	for (OWLAnnotation annotation : owlClass.getAnnotations(ontology, seeAlso)) {
	    if (annotation.getValue() instanceof OWLLiteral) {
		OWLLiteral val = (OWLLiteral) annotation.getValue();
		labelValue = val.getLiteral();
	    }
	}
	return labelValue;
    }

    public static List<OWLCLassToRender> getSubClassesToRender(String id, boolean directOnly) throws ParserException {
	OWLClassExpression classExpression = parseClassExpression(id);
	Set<OWLClass> subClasses = reasoner.getSubClasses(classExpression, directOnly).getFlattened();
	List<OWLCLassToRender> subClassesToRender = new ArrayList<OWLCLassToRender>();
	for (OWLEntity subclass : subClasses) {
	    OWLCLassToRender classToRender = new OWLCLassToRender();
	    if(!isTopEntity(subclass)){
		classToRender.logo = getLogo(shortFormProvider.getShortForm(subclass));
		classToRender.name = shortFormProvider.getShortForm(subclass);
		classToRender.label = getLabel(classToRender.name);
		subClassesToRender.add(classToRender);
	    }
	}

	return sort(subClassesToRender);
    }

    private static List<OWLCLassToRender> sort(List<OWLCLassToRender> classes) {
	OWLClassCompare classCompare = new OWLClassCompare();
	Collections.sort(classes, classCompare);
	return classes;
    }

    private static String getLogo(String id) {
	Pattern patternDrug = Pattern.compile("^\\w\\d\\d\\w\\w\\d\\d$");
	Matcher matcherDrug = patternDrug.matcher(id);
	Pattern patternDrugBank = Pattern.compile("^DB\\d.*");
	Matcher matcherDrugBank = patternDrugBank.matcher(id);
	if (matcherDrug.find()){
	    return "drug";
	}else if(matcherDrugBank.find()){
	    return "drug-bank";
	}else{
	    return "dot";
	}
    }

    private static boolean isTopEntity(OWLEntity owlEntity) {
	if(owlEntity.getIRI().toString().contains("http://www.w3.org/2002/07/owl")){
	    return true;
	}
	return false;
    }

    public static OWLClassExpression parseClassExpression(String expression) throws ParserException {
	ManchesterOWLSyntaxEditorParser parser = new ManchesterOWLSyntaxEditorParser(factory, expression);
	parser.setDefaultOntology(ontology);
	parser.setOWLEntityChecker(entityChecker);
	OWLClassExpression owlExpression = null;
	owlExpression = parser.parseClassExpression();
	return owlExpression;
    }

    public static OWLCLassToRender getClassToRender(String id) {
	OWLCLassToRender classToRender = new OWLCLassToRender();
	classToRender.label = getLabel(id);
	classToRender.name = id;
	classToRender.seeAlso = getSeeAlso(id);
	classToRender.logo = getLogo(id);
	return classToRender;
    }

    public static List<OWLCLassToRender> getSuperClassesToRender(String id, boolean onlyDirect) throws ParserException {
	OWLClassExpression classExpression = null;
	classExpression = parseClassExpression(id);
	Set<OWLClass> superClasses = reasoner.getSuperClasses(classExpression, onlyDirect).getFlattened();
	List<OWLCLassToRender> superClassesToRender = new ArrayList<OWLCLassToRender>();
	for (OWLEntity superclass : superClasses) {
	    OWLCLassToRender classToRender = new OWLCLassToRender();
	    if(!isTopEntity(superclass)){
		classToRender.logo = getLogo(shortFormProvider.getShortForm(superclass));
		classToRender.name = shortFormProvider.getShortForm(superclass);
		classToRender.label = getLabel(classToRender.name);
		superClassesToRender.add(classToRender);
	    }
	}
	return sort(superClassesToRender);
    }


    public static boolean knowsClass(String classNameToTest) {
	ManchesterOWLSyntaxEditorParser parser = new ManchesterOWLSyntaxEditorParser(factory, classNameToTest);
	parser.setDefaultOntology(ontology);
	parser.setOWLEntityChecker(entityChecker);
	if(parser.isClassName(classNameToTest)){
	    return true;
	}else{
	    return false;
	}
    }
}
