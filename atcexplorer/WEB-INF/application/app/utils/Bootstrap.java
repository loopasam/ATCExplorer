/**
 * 
 */
package utils;


import java.io.File;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import play.jobs.Job;
import play.jobs.OnApplicationStart;
import play.test.Fixtures;

/**
 * @author Samuel Croset
 *
 */
@OnApplicationStart
public class Bootstrap extends Job {
    public void doJob() throws OWLOntologyCreationException {
	System.out.println("Loading ontology...");
	Brain.learn("data/atc.owl", "http://www.ebi.ac.uk/atc/");
    }

}
