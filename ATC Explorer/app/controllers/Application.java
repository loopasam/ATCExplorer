package controllers;

import play.*;
import play.mvc.*;
import utils.Brain;
import utils.OWLCLassToRender;

import java.util.*;

import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;

import models.*;

public class Application extends Controller {

    public static void index() {
	render();
    }

    public static void term(String id){
	OWLCLassToRender currentClass = Brain.getClassToRender(id);
	List<OWLCLassToRender> subClasses = Brain.getSubClassesToRender(id);
	List<OWLCLassToRender> superClasses = Brain.getSuperClassesToRender(id);
	render(currentClass, subClasses, superClasses);
    }
    
    public static void query(){
	
	render();
	
    }
    
    public static void download(){
	render();
    }

}