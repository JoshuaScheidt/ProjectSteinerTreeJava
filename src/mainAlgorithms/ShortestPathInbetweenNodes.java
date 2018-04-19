package mainAlgorithms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Stack;

import graph.Edge;
import graph.EdgeFake;
import graph.PathFinding.DijkstraInfo;
import graph.UndirectedGraph;
import graph.Vertex;

public class ShortestPathInbetweenNodes implements SteinerTreeSolver {

	@Override
	public List<Edge> solve(UndirectedGraph G) {
		// TODO Auto-generated method stub
		return null;
	}

	public static ArrayList<EdgeFake> dijkstraAllInbetween(UndirectedGraph graph, ArrayList<Vertex> source) {
		ArrayList<Integer[]> searches = new ArrayList<>();
		HashMap<Integer, ArrayList<Integer>> visited = new HashMap<>();
		for (Vertex nb : source.get(0).getNeighbors()) {
			if (searches.size() > 0) {
				binaryInsertion(searches, new Integer[] { source.get(0).getKey(), nb.getKey(), source.get(0).getConnectingEdge(nb).getCost().get() });
			} else
				searches.add(new Integer[] { source.get(0).getKey(), nb.getKey(), source.get(0).getConnectingEdge(nb).getCost().get() });
		}
		visited.put(source.get(0).getKey(), new ArrayList<>());
		visited.get(source.get(0).getKey()).add(source.get(0).getKey());

		while (source.size() < graph.getNumberOfTerminals()) {

		}

		return null;
	}

	public static void binaryInsertion(ArrayList<Integer[]> searches, Integer[] insert) {
		int upper = searches.size() - 1;
		int lower = 0;
		boolean inserted = false;
		while (!inserted) {
			if (upper == lower) {
				searches.add(upper, insert);
			}
			int index = (int) Math.ceil((upper - lower) / 2);
			if (searches.get(index)[2] > insert[2]) {
				upper = index;
			} else
				lower = index;
		}
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
	public static ArrayList<EdgeFake> DijkstraShortestPathHeuristic(UndirectedGraph G, ArrayList<Vertex> start,
			HashMap<Vertex, HashMap<Vertex, DijkstraInfo>> fullInfo, HashMap<Vertex, ArrayList<Vertex>> availableSearches,
			HashMap<Vertex, Integer> lowestCosts, HashMap<Vertex, ArrayList<Integer>> alreadyVisited) {
		// System.out.println(start.size());
		Vertex foundFrom = null, foundTerminal = null;
		while (true) {
			// System.out.println(Q.size());
			int smallestDist = Integer.MAX_VALUE;
			Vertex begin = null;
			Vertex chosen = null;
			Entry<Vertex, Integer> min = Collections.min(lowestCosts.entrySet(), Comparator.comparing(Entry::getValue));

			// for (Vertex v : start)
			for (Vertex i : availableSearches.get(min.getKey())) {
				// System.out.println(fullInfo.get(v).get(i).dist);
				if (fullInfo.get(min.getKey()).get(i).dist < smallestDist) {
					begin = min.getKey();
					chosen = i;
					smallestDist = fullInfo.get(min.getKey()).get(i).dist;
					// System.out.println("in here");
				}
			}
			// System.out.println(chosen.getKey());

			if (chosen == null)
				System.out.println("ERROR: No shortest distance vertex found with distance < INTEGER.MAX_VALUE");
			if (chosen.isTerminal() && !start.contains(chosen)) {
				foundFrom = begin;
				foundTerminal = chosen;
				break;
			}

			availableSearches.get(begin).remove(chosen);
			alreadyVisited.get(begin).add(chosen.getKey());

			int distToCur = fullInfo.get(begin).get(chosen).dist;
			int totDistToNb = 0;
			for (Vertex nb : chosen.getNeighbors()) {
				if (G.getEdges().contains(chosen.getConnectingEdge(nb)) && !alreadyVisited.get(begin).contains(nb.getKey())) {
					totDistToNb = distToCur + chosen.getConnectingEdge(nb).getCost().get();
					DijkstraInfo nbInfo = fullInfo.get(begin).get(nb);
					// System.out.println("before:" + nbInfo.dist);
					if (nbInfo == null)
						System.out.println(nb.getKey() + " ???");
					if (totDistToNb < nbInfo.dist) {
						nbInfo.dist = totDistToNb;
						nbInfo.parent = chosen;
						if (!availableSearches.get(begin).contains(nb)) {
							availableSearches.get(begin).add(nb);
						}
					}
					// System.out.println("after: " + nbInfo.dist);
				}
			}
			int lowest = Integer.MAX_VALUE;
			for (Vertex v : availableSearches.get(begin)) {
				if (lowest > fullInfo.get(begin).get(v).dist)
					lowest = fullInfo.get(begin).get(v).dist;
			}
			lowestCosts.put(begin, lowest);
		}

		ArrayList<EdgeFake> result = new ArrayList<>();
		ArrayList<Vertex> path = new ArrayList<>();
		Vertex current = foundTerminal;
		while (fullInfo.get(foundFrom).get(current).parent != null) {
			path.add(current);
			current = fullInfo.get(foundFrom).get(current).parent;
		}
		path.add(current);
		if (foundFrom.isNeighbor(foundTerminal))
			result.add(new EdgeFake(foundFrom, foundTerminal, foundFrom.getConnectingEdge(foundTerminal).getCost().get(),
					foundFrom.getConnectingEdge(foundTerminal).getStack()));
		else {
			Stack<int[]> pathStack = new Stack<>();
			for (int i = 0; i < path.size() - 1; i++) {
				pathStack.push(new int[] { path.get(i).getKey(), path.get(i + 1).getKey(),
						G.getVertices().get(path.get(i).getKey()).getConnectingEdge(G.getVertices().get(path.get(i + 1).getKey())).getCost().get() });
			}
			EdgeFake newEdge = new EdgeFake(foundFrom, foundTerminal, fullInfo.get(foundFrom).get(foundTerminal).dist, pathStack);
			result.add(newEdge);
		}

		return result;
	}

}
