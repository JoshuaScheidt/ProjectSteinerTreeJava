/*
 * All of the given code may be used on free will when referenced to the source.
 * Initial version created at 15:54:49
 */
package mainAlgorithms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import graph.Edge;
import graph.EdgeFake;
import graph.PathFinding;
import graph.UndirectedGraph;
import graph.Vertex;

/**
 * @see <a href=
 *      "http://www.cs.uu.nl/docs/vakken/an/teoud/an-steiner.ppt">Slides</a>
 * 
 * @author Joshua Scheidt
 */
public class ShortestPathHeuristic implements SteinerTreeSolver {

	int upperBound;
	int lowerBound;
	UndirectedGraph G;

	/*
	 * (non-Javadoc)
	 * 
	 * @see mainAlgorithms.SteinerTreeSolver#solve(graph.UndirectedGraph)
	 */
	@Override
	public List<Edge> solve(UndirectedGraph Graph) {
		this.G = Graph.clone();
		Vertex startingTerminal = this.G.getTerminals().get(this.G.getTerminals().keySet().iterator().next());// Take first terminal to find as
																												// starting
		// terminal for shortest path heuristic
		this.shortestPathConnected(startingTerminal);
		System.out.println("done with shortest path");
		System.out.println(this.G.getVertices().size());
		System.out.println(this.G.getEdges().size());
		this.primAlgorithm(this.G.getVertices().get(this.G.getVertices().keySet().iterator().next()));
		System.out.println("Created MST");
		System.out.println(this.G.getVertices().size());
		System.out.println(this.G.getEdges().size());
		return null;
	}

	/**
	 * Prim's algorithm for finding a minimal spanning tree
	 *
	 * @author Joshua Scheidt
	 */
	private void primAlgorithm(Vertex startingVertex) {

		HashSet<Vertex> currentSet = new HashSet<>();
		currentSet.add(startingVertex);
		HashMap<Vertex, Boolean> visited = new HashMap<>();
		for (Vertex v : this.G.getVertices().values())
			visited.put(v, false);
		HashSet<Edge> chosenEdges = new HashSet<>();

		while (!currentSet.containsAll(this.G.getVertices().values())) {
			Edge bestEdge = null;
			int closestWeight = Integer.MAX_VALUE;
			Edge e;
			for (Vertex v : currentSet) {
				for (Vertex nb : v.getNeighbors()) {
					e = v.getConnectingEdge(nb);
					if (chosenEdges.contains(e))
						continue;
					if (currentSet.contains(nb)) {
						this.G.removeEdge(e);
					} else if (e.getCost().get() < closestWeight) {
						closestWeight = e.getCost().get();
						bestEdge = e;
					}
				}
			}
			chosenEdges.add(bestEdge);
			currentSet.add(bestEdge.getVertices()[0]);
			currentSet.add(bestEdge.getVertices()[1]);
		}
	}

	/**
	 * A shortest paths heuristics which creates a connected component between
	 * terminals by taking the shortest paths.
	 *
	 * @param startingTerminal
	 *            The first terminal from which shortest paths are taken
	 *
	 * @author Joshua Scheidt
	 */
	private void shortestPathConnected(Vertex startingTerminal) {
		HashMap<Vertex, HashMap<Vertex, EdgeFake>> shortestPaths = new HashMap<>();
		ArrayList<Integer> keys = new ArrayList<>();
		Iterator<Integer> it = this.G.getTerminals().keySet().iterator();
		while (it.hasNext()) {
			keys.add(it.next());
		}

		ArrayList<Vertex> endpoints;
		ArrayList<EdgeFake> paths;
		// System.out.println("Total terminals: " + keys.size());
		for (int i = 0; i < keys.size(); i++) {
			// System.out.println("starting terminal: " + i);
			endpoints = new ArrayList<>();
			for (int j = i + 1; j < keys.size(); j++) {
				endpoints.add(this.G.getTerminals().get(keys.get(j)));
			}

			paths = PathFinding.DijkstraMultiPathFakeEdges(this.G, this.G.getTerminals().get(keys.get(i)), endpoints, null);
			HashMap<Vertex, EdgeFake> tmp = new HashMap<>();
			for (EdgeFake e : paths) {
				tmp.put(e.getVertices()[1], e);
			}
			shortestPaths.put(this.G.getTerminals().get(keys.get(i)), tmp);
		}

		ArrayList<Vertex> currentSet = new ArrayList<>();
		currentSet.add(startingTerminal);
		ArrayList<EdgeFake> currentEdges = new ArrayList<>();
		Vertex currentClosest = null, connectedWith = null;
		int closestWeight = Integer.MAX_VALUE;
		while (currentSet.size() != this.G.getTerminals().size()) {
			for (Vertex t : this.G.getTerminals().values()) {
				if (!currentSet.contains(t)) {
					for (Vertex u : this.G.getTerminals().values()) {
						if (currentSet.contains(u)) {
							if ((shortestPaths.containsKey(t) && shortestPaths.get(t).containsKey(u))
									&& shortestPaths.get(t).get(u).getCost() < closestWeight) {
								currentClosest = t;
								connectedWith = u;
								closestWeight = shortestPaths.get(t).get(u).getCost();
							} else if ((shortestPaths.containsKey(u) && shortestPaths.get(u).containsKey(t)
									&& shortestPaths.get(u).get(t).getCost() < closestWeight)) {
								currentClosest = t;
								connectedWith = u;
								closestWeight = shortestPaths.get(u).get(t).getCost();
							}
						}
					}
				}
			}
			currentSet.add(currentClosest);
			currentEdges.add((shortestPaths.containsKey(currentClosest) && shortestPaths.get(currentClosest).containsKey(connectedWith))
					? shortestPaths.get(currentClosest).get(connectedWith)
					: shortestPaths.get(connectedWith).get(currentClosest));
			closestWeight = Integer.MAX_VALUE;
		}

		HashSet<int[]> result = new HashSet<>();
		for (EdgeFake e : currentEdges) {
			if (e.getStack() != null)
				for (int[] i : e.getStack())
					result.add(i);
			else
				result.add(new int[] { e.getVertices()[0].getKey(), e.getVertices()[1].getKey(), e.getCost() });
		}
		// return result;

		ArrayList<Vertex> tbrVertices = new ArrayList<>();
		for (Vertex v : this.G.getVertices().values())
			tbrVertices.add(v);
		for (int[] i : result) {
			tbrVertices.remove(this.G.getVertices().get(i[0]));
			tbrVertices.remove(this.G.getVertices().get(i[1]));
		}
		for (int i = 0; i < tbrVertices.size(); i++) {
			this.G.removeVertex(tbrVertices.get(i));
		}
	}

}