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
import java.util.Map;

import graph.Edge;
import graph.EdgeFake;
import graph.PathFinding;
import graph.PathFinding.DijkstraInfo;
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
		Vertex startingTerminal = (this.G.getTerminals().size() > 0 ? this.G.getTerminals().get(this.G.getTerminals().keySet().iterator().next())
				: null);// Take first terminal to find as
		// starting
		if (startingTerminal == null) {
			System.out.println("No terminals, STOP");
			System.exit(1);
		}
		// terminal for shortest path heuristic
		this.shortestPathConnected(startingTerminal);
		// System.out.println("done with shortest path");
		// System.out.println(this.G.getVertices().size());
		// System.out.println(this.G.getEdges().size());

		// for (Vertex v : this.G.getVertices().values())
		// System.out.println("Vertex: " + v.getKey());
		// for (Edge v : this.G.getEdges())
		// System.out.println("Edge: " + v.getVertices()[0].getKey() + " " +
		// v.getVertices()[1].getKey() + " " + v.getCost().get());

		this.primAlgorithm(this.G.getVertices().get(this.G.getVertices().keySet().iterator().next()));
		// System.out.println("Created MST");
		// System.out.println(this.G.getVertices().size());
		// System.out.println(this.G.getEdges().size());
		// for (Vertex v : this.G.getVertices().values())
		// System.out.println("Vertex: " + v.getKey());
		// for (Edge v : this.G.getEdges())
		// System.out.println("Edge: " + v.getVertices()[0].getKey() + " " +
		// v.getVertices()[1].getKey() + " " + v.getCost().get());

		this.removeNonTerminalDegree1();
		// System.out.println("Removed non-terminal degree 1");
		ArrayList<Edge> result = new ArrayList<>();
		for (Edge e : this.G.getEdges())
			result.add(e);
		return result;
	}

	private void removeNonTerminalDegree1() {
		for (Iterator<Map.Entry<Integer, Vertex>> it = this.G.getVertices().entrySet().iterator(); it.hasNext();) {
			Map.Entry<Integer, Vertex> entry = it.next();
			if (entry.getValue().getNeighbors().size() <= 1)
				it.remove();
		}
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
		this.G.getEdges().retainAll(chosenEdges);
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
		HashSet<EdgeFake> edges = new HashSet<>();
		ArrayList<Integer> keys = new ArrayList<>();
		Iterator<Integer> it = this.G.getTerminals().keySet().iterator();
		while (it.hasNext()) {
			keys.add(it.next());
		}

		HashMap<Vertex, HashMap<Vertex, DijkstraInfo>> dijkstraInfo = new HashMap<>();
		dijkstraInfo.put(startingTerminal, new HashMap<>());
		dijkstraInfo.get(startingTerminal).put(startingTerminal, new DijkstraInfo(0));

		HashMap<Vertex, Integer> lowestCosts = new HashMap<>();
		lowestCosts.put(startingTerminal, 0);

		HashMap<Vertex, ArrayList<Integer>> alreadyVisited = new HashMap<>();

		ArrayList<Vertex> currentSet = new ArrayList<>();
		currentSet.add(startingTerminal);
		ArrayList<EdgeFake> path;
		HashMap<Vertex, ArrayList<Vertex>> availableSearches = new HashMap<>();
		availableSearches.put(startingTerminal, new ArrayList<>());
		availableSearches.get(startingTerminal).add(startingTerminal);
		alreadyVisited.put(startingTerminal, new ArrayList<>());
		alreadyVisited.get(startingTerminal).add(startingTerminal.getKey());
		for (Vertex other : this.G.getVertices().values())
			if (other != startingTerminal)
				dijkstraInfo.get(startingTerminal).put(other, new DijkstraInfo(Integer.MAX_VALUE));
		while (currentSet.size() != this.G.getTerminals().size()) {
			// System.out.println("currentSet: " + currentSet.size());
			path = PathFinding.DijkstraShortestPathHeuristic(this.G, currentSet, dijkstraInfo, availableSearches, lowestCosts, alreadyVisited);
			edges.addAll(path);
			for (EdgeFake e : path) {
				if (e.getVertices()[0].isTerminal() && !currentSet.contains(e.getVertices()[0])) {
					Vertex v = e.getVertices()[0];
					currentSet.add(v);
					availableSearches.put(v, new ArrayList<>());
					availableSearches.get(v).add(v);
					alreadyVisited.put(v, new ArrayList<>());
					alreadyVisited.get(v).add(v.getKey());
					dijkstraInfo.put(v, new HashMap<>());
					dijkstraInfo.get(v).put(v, new DijkstraInfo(0));
					lowestCosts.put(v, 0);
					for (Vertex other : this.G.getVertices().values())
						dijkstraInfo.get(v).put(other, new DijkstraInfo(Integer.MAX_VALUE));
					break;
				}
				if (e.getVertices()[1].isTerminal() && !currentSet.contains(e.getVertices()[1])) {
					Vertex v = e.getVertices()[1];
					currentSet.add(v);
					availableSearches.put(v, new ArrayList<>());
					availableSearches.get(v).add(v);
					alreadyVisited.put(v, new ArrayList<>());
					alreadyVisited.get(v).add(v.getKey());
					dijkstraInfo.put(v, new HashMap<>());
					dijkstraInfo.get(v).put(v, new DijkstraInfo(0));
					lowestCosts.put(v, 0);
					for (Vertex other : this.G.getVertices().values())
						if (other != v)
							dijkstraInfo.get(v).put(other, new DijkstraInfo(Integer.MAX_VALUE));
					break;
				}
			}
		}
		// System.out.println("done here");
		ArrayList<Vertex> tbrVerts = new ArrayList<>();
		for (Vertex v : this.G.getVertices().values()) {
			tbrVerts.add(v);
		}
		for (EdgeFake e : edges) {
			if (e.getStack() != null) {
				for (int[] stack : e.getStack()) {
					tbrVerts.remove(this.G.getVertices().get(stack[0]));
					tbrVerts.remove(this.G.getVertices().get(stack[1]));
				}

			} else
				for (Vertex v : e.getVertices())
					tbrVerts.remove(v);
		}
		for (int i = 0; i < tbrVerts.size(); i++) {
			this.G.removeVertex(tbrVerts.get(i));
		}
	}

}