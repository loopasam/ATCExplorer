package controllers;

import play.*;
import play.mvc.*;
import utils.Brain;
import utils.OWLCLassToRender;

import java.util.*;


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