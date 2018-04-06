/*
 * All of the given code may be used on free will when referenced to the source.
 * Initial version created at 16:32:26
 */
package mainAlgorithms;

import java.util.List;

import graph.Edge;
import graph.UndirectedGraph;

/**
 * The interface for the Steiner Tree solving algorithms
 *
 * 
 * @author Marciano Geijselaers
 * @author Joshua Scheidt
 */
public interface SteinerTreeSolver {

	/**
	 * Solves the Steiner Tree using any of the implementations of this interface.
	 *
	 * @param G
	 *            The given graph
	 * @return The set of edges connecting the Steiner Tree
	 *
	 * @author Joshua Scheidt
	 */
	public List<Edge> solve(UndirectedGraph G);

}
