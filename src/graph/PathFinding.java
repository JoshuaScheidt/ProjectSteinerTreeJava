/*
 * All of the given code may be used on free will when referenced to the source.
 * Initial version created at 14:13:39
 */
package graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

/**
 * This class provides Path finding techniques which can be utilised in the
 * graph.
 *
 * 
 * @author Joshua Scheidt
 */
public class PathFinding {

	private static class DijkstraInfo {
		public int dist;
		public Vertex parent = null;

		public DijkstraInfo(int dist) {
			this.dist = dist;
		}
	}

	/**
	 * Performs Dijkstra's path finding algorithm and returns the new edge between
	 * the vertices.
	 *
	 * @param G
	 *            The graph in which Dijkstra has to be performed
	 * @param start
	 *            The starting vertex
	 * @param end
	 *            The endpoint vertex
	 * @return The new edge with the lowest weight
	 *
	 * @author Joshua Scheidt
	 */
	public static Edge DijkstraSingleEdge(UndirectedGraph G, Vertex start, Vertex end) {
		ArrayList<Vertex> Q = new ArrayList<>();
		HashMap<Integer, DijkstraInfo> datamap = new HashMap<>();
		for (Vertex i : G.getVertices().values()) {
			datamap.put(i.getKey(), new DijkstraInfo(Integer.MAX_VALUE));
			Q.add(i);
		}
		System.out
				.println("Start:" + start.getKey() + "  End:" + end.getKey() + "  G(V):" + G.getVertices().size() + "   G(E):" + G.getEdges().size());
		datamap.get(start.getKey()).dist = 0;

		boolean reachedEnd = false;

		System.out.println("In here");

		while (!Q.isEmpty()) {
			int smallestDist = Integer.MAX_VALUE;
			Vertex current = null;
			for (Vertex i : Q) {
				if (datamap.get(i.getKey()).dist < smallestDist) {
					current = i;
					smallestDist = datamap.get(i.getKey()).dist;
				}
			}
			if (reachedEnd && smallestDist > datamap.get(end.getKey()).dist)
				break;
			if (current == null)
				System.out.println("ERROR: No shortest distance vertex found with distance < INTEGER.MAX_VALUE");
			if (current == end)
				reachedEnd = true;
			Q.remove(current);
			int distToCur = datamap.get(current.getKey()).dist;
			int totDistToNb = 0;
			for (Vertex nb : current.getNeighbors()) {
				totDistToNb = distToCur + current.getConnectingEdge(nb).getCost().get();
				DijkstraInfo nbInfo = datamap.get(nb.getKey());
				if (nbInfo == null)
					System.out.println(nb.getKey() + " ???");
				if (totDistToNb < nbInfo.dist) {
					nbInfo.dist = totDistToNb;
					nbInfo.parent = current;
				}
			}

		}

		ArrayList<Vertex> path = new ArrayList<>();
		Vertex current = end;
		while (datamap.get(current.getKey()).parent != null) {
			path.add(current);
			current = datamap.get(current.getKey()).parent;
		}
		path.add(current);
		for (Vertex i : path)
			System.out.print(i.getKey() + " - ");
		System.out.println();

		Edge newEdge = new Edge(start, end, datamap.get(end.getKey()).dist, true);
		for (int i = 0; i < path.size() - 1; i++) {
			newEdge.pushSubsumed(
					new int[] { path.get(i).getKey(), path.get(i).getKey(), path.get(i).getConnectingEdge(path.get(i + 1)).getCost().get() });
		}

		return newEdge;
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
	public static ArrayList<EdgeFake> DijkstraMultiPathFakeEdges(UndirectedGraph G, Vertex start, ArrayList<Vertex> end, ArrayList<Edge> edges) {
		ArrayList<Vertex> Q = new ArrayList<>();
		HashMap<Vertex, DijkstraInfo> datamap = new HashMap<>();
		if (edges == null) {
			edges = new ArrayList<>();
			for (Edge e : G.getEdges())
				edges.add(e);
		}
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

		ArrayList<EdgeFake> result = new ArrayList<>();
		for (Vertex v : end) {
			ArrayList<Vertex> path = new ArrayList<>();
			Vertex current = v;
			while (datamap.get(current).parent != null) {
				path.add(current);
				current = datamap.get(current).parent;
			}
			path.add(current);
			if (start.isNeighbor(v) && start.getConnectingEdge(v).getCost().get() <= datamap.get(v).dist)
				result.add(new EdgeFake(start, v, start.getConnectingEdge(v).getCost().get(), start.getConnectingEdge(v).getStack()));
			else {
				Stack<int[]> pathStack = new Stack<>();
				for (int i = 0; i < path.size() - 1; i++) {
					pathStack.push(new int[] { path.get(i).getKey(), path.get(i + 1).getKey(), G.getVertices().get(path.get(i).getKey())
							.getConnectingEdge(G.getVertices().get(path.get(i + 1).getKey())).getCost().get() });
				}
				EdgeFake newEdge = new EdgeFake(start, v, datamap.get(v).dist, pathStack);
				result.add(newEdge);
			}
		}
		return result;
	}
}
