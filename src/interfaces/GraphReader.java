/*
 * All of the given code may be used on free will when referenced to the source.
 * Initial version created at 12:30:31
 */
package interfaces;

import java.io.File;

/**
 * General Reader interface for Graph creations.
 *
 * 
 * @author Marciano Geijselaers
 * @author Joshua Scheidt
 */
public interface GraphReader {
	
	public Graph read(File f);

}
