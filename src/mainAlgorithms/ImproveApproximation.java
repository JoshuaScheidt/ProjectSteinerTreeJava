/*
 * All of the given code may be used on free will when referenced to the source.
 * Initial version created at 11:08:40
 */
package mainAlgorithms;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import graph.Edge;
import graph.UndirectedGraph;

/**
 * Tries to improve an already approximated answer
 *
 * @author Joshua Scheidt
 * @author Marciano Geijselaers
 */
public class ImproveApproximation {
	ArrayList<Edge> edges;
	UndirectedGraph graph;
	Consumer<ArrayList<Edge>> setBest;

	public ImproveApproximation(List<Edge> edges, UndirectedGraph graph, Consumer<ArrayList<Edge>> object) {
		this.edges = (ArrayList<Edge>) edges;
		this.graph = graph;
		this.setBest = object;
	}

	public List<Edge> improve() {
		return null;
	}

}
