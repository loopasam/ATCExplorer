package controllers;

import play.*;
import play.mvc.*;
import utils.Brain;
import utils.OWLCLassToRender;

import java.util.*;

import org.semanticweb.owlapi.expression.ParserException;
import org.semanticweb.owlapi.model.OWLClass;


import models.*;

public class Application extends Controller {

    public static void index() {
	render();
    }

    public static void parsingError() {
	render();
    }

    public static void term(String id){
	if(Brain.knowsNamedClass(id)){
	    OWLCLassToRender currentClass = null;
	    try {
		currentClass = Brain.getClassToRender(id);
	    } catch (ParserException currentClassException) {
		error(404, "Named class '" + id + "' does not exist");
	    }
	    List<OWLCLassToRender> subClasses = null;
	    try {
		subClasses = Brain.getSubClassesToRender(id, true);
	    } catch (ParserException e) {
		error(404, "Named class '" + id + "' does not exist");
		e.printStackTrace();
	    }
	    List<OWLCLassToRender> superClasses = null;
	    try {
		superClasses = Brain.getSuperClassesToRender(id, true);
	    } catch (ParserException e) {
		error(404, "Named class '" + id + "' does not exist");
		e.printStackTrace();
	    }
	    render(currentClass, subClasses, superClasses);
	}else{
	    error(404, "Named class '" + id + "' does not exist");
	}
    }

    public static void query(){
	render();
    }

    public static void subclasses(String expression){
	OWLCLassToRender currentClass = null;
	try {
	    currentClass = Brain.getClassToRender(expression);
	} catch (ParserException e1) {
	    error(404, "'" + expression + "' is not a valid OWL class expression");
	}
	List<OWLCLassToRender> subClasses = null;
	try {
	    subClasses = Brain.getSubClassesToRender(expression, false);
	} catch (ParserException e) {
	    error(404, "'" + expression + "' is not a valid OWL class expression");
	}
	render(expression, currentClass, subClasses);
    }

    public static void superclasses(String expression){
	OWLCLassToRender currentClass = null;
	try {
	    currentClass = Brain.getClassToRender(expression);
	} catch (ParserException e1) {
	    error(404, "'" + expression + "' is not a valid OWL class expression");
	}
	List<OWLCLassToRender> superClasses = null;
	try {
	    superClasses = Brain.getSuperClassesToRender(expression, false);
	} catch (ParserException e) {
	    error(404, "'" + expression + "' is not a valid OWL class expression");
	}
	render(expression, currentClass, superClasses);
    }

    public static void calculateResults(String expression, String resultsType){
	try {
	    Brain.parseClassExpression(expression);
	} catch (ParserException e) {
	    //If error, the query form is re-shown to the user
	    String trace = e.getLocalizedMessage();
	    flash.put("parsingError", trace);
	    flash.put("expression", expression);
	    query();
	}

	if(resultsType.equals("subclasses")){
	    subclasses(expression);
	}else{
	    superclasses(expression);
	}
    }

    public static void download(){
	render();
    }

}