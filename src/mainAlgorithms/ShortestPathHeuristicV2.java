/*
 * All of the given code may be used on free will when referenced to the source.
 * Initial version created at 12:39:07
 */
package mainAlgorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import graph.Edge;
import graph.UndirectedGraph;
import graph.Vertex;

/**
 * (Class description)
 *
 * 
 * @author Joshua Scheidt
 */
public class ShortestPathHeuristicV2 implements SteinerTreeSolver {

	UndirectedGraph graph;

	public static class DijkstraInfo {
		public int dist;
		public Vertex parent = null;

		public DijkstraInfo(int dist) {
			this.dist = dist;
		}
	}

	@Override
	public List<Edge> solve(UndirectedGraph G) {
		System.out.println("in here");
		this.graph = G;
		List<Edge> bestResult = null;
		int bestVal = Integer.MAX_VALUE;
		for (int t : this.graph.getTerminals().keySet()) {
			System.out.println(Arrays.toString(this.dijkstraOrdering(t).toArray()));
			// List<Edge> result = this.dijkstraPathFinder(this.dijkstraOrdering(t));
			// int val = 0;
			// for (Edge e : result)
			// val += e.getCost().get();
			// if (val < bestVal)
			// bestResult = result;
		}
		return bestResult;
	}

	/**
	 * Returns the ordering on how the terminals need to be connected.
	 *
	 * @param graph
	 *            the graph
	 * @param sk
	 *            the starting key
	 * @return the ordering
	 *
	 * @author Joshua Scheidt
	 */
	private ArrayList<Integer> dijkstraOrdering(int sk) {
		ArrayList<Integer> ordering = new ArrayList<>();
		ArrayList<Integer> Q = new ArrayList<>();
		HashMap<Integer, DijkstraInfo> datamap = new HashMap<>();
		for (Integer v : this.graph.getVertices().keySet()) {
			datamap.put(v, new DijkstraInfo(Integer.MAX_VALUE));
			Q.add(v);
		}
		datamap.get(sk).dist = 0;
		int numReachedEnd = 0;
		while (!Q.isEmpty()) {
			int smallestDist = Integer.MAX_VALUE;
			Integer current = -1; // No vertex with -1 key
			if (numReachedEnd == this.graph.getNumberOfTerminals())
				break;
			for (Integer i : Q) {
				if (datamap.get(i).dist < smallestDist) {
					current = i;
					smallestDist = datamap.get(i).dist;
				}
			}
			if (current == -1)
				System.out.println("ERROR: No shortest distance vertex found with distance < INTEGER.MAX_VALUE");

			if (this.graph.getTerminals().keySet().contains(current)) {
				numReachedEnd++;
				ordering.add(current);
			}
			Q.remove(current);
			int distToCur = datamap.get(current).dist;
			int totDistToNb = 0;
			for (Vertex nb : this.graph.getVertices().get(current).getNeighbors()) {
				totDistToNb = distToCur + this.graph.getVertices().get(current).getConnectingEdge(nb).getCost().get();
				DijkstraInfo nbInfo = datamap.get(nb.getKey());
				if (nbInfo == null)
					System.out.println(nb.getKey() + " ???");
				if (totDistToNb < nbInfo.dist) {
					nbInfo.dist = totDistToNb;
				}
			}
		}
		return ordering;
	}

	/**
	 * Finds the path corresponding to the found ordering.
	 *
	 * @param ordering
	 *            the ordering of the terminals
	 * @return
	 *
	 * @author Marciano Geijselaers
	 * @author Joshua Scheidt
	 */
	private List<Edge> dijkstraPathFinder(ArrayList<Integer> ordering) {
		return null;

	}

	/**
	 * Performs Dijkstra's path finding algorithm and returns the new edges between
	 * the vertices.
	 *
	 * @param G
	 *            The graph in which Dijkstra has to be performed
	 * @param start
	 *            The starting vertex
	 * @param end
	 *            The endpoint vertices as an array
	 * @param edges
	 *            The allowed edges to traverse over
	 * @return The new edges with the lowest weights
	 *
	 * @author Joshua Scheidt
	 */
	public static ArrayList<Edge> DijkstraMultiPath(UndirectedGraph G, Vertex start, ArrayList<Vertex> end, ArrayList<Edge> edges) {
		ArrayList<Vertex> Q = new ArrayList<>();
		HashMap<Vertex, DijkstraInfo> datamap = new HashMap<>();
		for (Edge e : edges) {
			if (!datamap.containsKey(e.getVertices()[0])) {
				datamap.put(e.getVertices()[0], new DijkstraInfo(Integer.MAX_VALUE));
				Q.add(e.getVertices()[0]);
			}
			if (!datamap.containsKey(e.getVertices()[1])) {
				datamap.put(e.getVertices()[1], new DijkstraInfo(Integer.MAX_VALUE));
				Q.add(e.getVertices()[1]);
			}
		}
		datamap.get(start).dist = 0;

		int numReachedEnd = 0;
		// System.out.println(G.getVertices().get(5).getNeighbors().size());
		while (!Q.isEmpty()) {
			int smallestDist = Integer.MAX_VALUE;
			Vertex current = null;
			for (Vertex i : Q) {
				if (datamap.get(i).dist < smallestDist) {
					current = i;
					smallestDist = datamap.get(i).dist;
				}
			}
			if (numReachedEnd == end.size())
				break;
			if (current == null)
				System.out.println("ERROR: No shortest distance vertex found with distance < INTEGER.MAX_VALUE");
			for (Vertex i : end)
				if (i.getKey() == current.getKey())
					numReachedEnd++;
			Q.remove(current);
			int distToCur = datamap.get(current).dist;
			int totDistToNb = 0;
			for (Vertex nb : current.getNeighbors()) {
				if (edges.contains(current.getConnectingEdge(nb))) {
					totDistToNb = distToCur + current.getConnectingEdge(nb).getCost().get();
					DijkstraInfo nbInfo = datamap.get(nb);
					if (nbInfo == null)
						System.out.println(nb.getKey() + " ???");
					if (totDistToNb < nbInfo.dist) {
						nbInfo.dist = totDistToNb;
						nbInfo.parent = current;
					}
				}
			}

		}

		ArrayList<Edge> result = new ArrayList<>();
		for (Vertex v : end) {
			ArrayList<Vertex> path = new ArrayList<>();
			Vertex current = v;
			while (datamap.get(current).parent != null) {
				path.add(current);
				current = datamap.get(current).parent;
			}
			path.add(current);
			if (start.isNeighbor(v) && start.getConnectingEdge(v).getCost().get() <= datamap.get(v).dist)
				continue;
			else {
				Stack<int[]> pathStack = new Stack<>();
				for (int i = 0; i < path.size() - 1; i++) {
					pathStack.push(new int[] { path.get(i).getKey(), path.get(i + 1).getKey(), G.getVertices().get(path.get(i).getKey())
							.getConnectingEdge(G.getVertices().get(path.get(i + 1).getKey())).getCost().get() });
				}
				if (start.isNeighbor(v)) {
					start.getConnectingEdge(v).setCost(datamap.get(v).dist);
					start.getConnectingEdge(v).replaceStack(pathStack);
				} else {
					Edge newEdge = new Edge(start, v, datamap.get(v).dist);
					newEdge.pushStack(pathStack);
					result.add(newEdge);
				}
			}
		}
		return result;
	}
}