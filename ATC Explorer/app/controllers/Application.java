package controllers;

import play.*;
import play.mvc.*;
import utils.Brain;
import utils.OWLCLassToRender;

import java.util.*;

import org.semanticweb.owlapi.expression.ParserException;


import models.*;

public class Application extends Controller {

    public static void index() {
	render();
    }

    public static void parsingError() {
	render();
    }

    public static void term(String id){
	if(Brain.knowsClass(id)){
	    OWLCLassToRender currentClass = Brain.getClassToRender(id);
	    List<OWLCLassToRender> subClasses = null;
	    try {
		subClasses = Brain.getSubClassesToRender(id, true);
	    } catch (ParserException e) {
		//TODO 404
		e.printStackTrace();
	    }
	    List<OWLCLassToRender> superClasses = null;
	    try {
		superClasses = Brain.getSuperClassesToRender(id, true);
	    } catch (ParserException e) {
		// TODO Auto-generated catch block - 404
		e.printStackTrace();
	    }
	    render(currentClass, subClasses, superClasses);
	}else{
	    //TODO hadling error - 404
	    System.out.println("Not a class: " + id);
	}
    }

    public static void query(){
	render();
    }

    public static void subclasses(String query){

	try {
	    Brain.parseClassExpression(query);
	} catch (ParserException e1) {
	    // TODO Auto-generated catch block - 404 wrong expression
	    e1.printStackTrace();
	}
	
	if(!Brain.knowsClass(query)){
	    //TODO: named query
	}else{
	    //TODO: clomplex query
	    //Artifact named class to remove after
	}
	    //TODO throw an error sur method comme sub and super class
	    //TODO important the getClass methodsa should throw the exception
	    OWLCLassToRender currentClass = Brain.getClassToRender(query);
	    
	    List<OWLCLassToRender> subClasses = null;
	    try {
		subClasses = Brain.getSubClassesToRender(query, false);
	    } catch (ParserException e) {
		//TODO 404
		e.printStackTrace();
	    }
	    render(query, currentClass, subClasses);
	


    }

    public static void superclasses(String query){
	//Check if it is a named class
	if(Brain.knowsClass(query)){
	    OWLCLassToRender currentClass = Brain.getClassToRender(query);
	    List<OWLCLassToRender> superClasses = null;
	    try {
		superClasses = Brain.getSuperClassesToRender(query, false);
	    } catch (ParserException e) {
		//TODO 404
		e.printStackTrace();
	    }
	    render(query, currentClass, superClasses);
	}else{
	    //TODO create a temporary named class
	    System.out.println("Complex query: " + query);
	}

    }

    public static void calculateResults(String query, String resultsType){
	try {
	    Brain.parseClassExpression(query);
	} catch (ParserException e) {
	    //If error, the query form is re-shown to the user
	    String trace = e.getLocalizedMessage();
	    flash.put("parsingError", trace);
	    flash.put("query", query);
	    query();
	}

	if(resultsType.equals("subclasses")){
	    subclasses(query);
	}else{
	    superclasses(query);
	}
    }

    public static void download(){
	render();
    }

}