/**
 * 
 */
package utils;

import java.util.Comparator;

/**
 * @author Samuel Croset
 *
 */
public class OWLClassCompare implements  Comparator<OWLCLassToRender> {

    /* (non-Javadoc)
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare(OWLCLassToRender o1, OWLCLassToRender o2) {
	return o1.name.compareTo(o2.name);
    }

}
