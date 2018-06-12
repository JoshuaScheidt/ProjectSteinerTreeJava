/*
 * All of the given code may be used on free will when referenced to the source.
 * Initial version created at 12:39:07
 */
package mainAlgorithms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
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
		public Integer parent = -1;

		public DijkstraInfo(int dist) {
			this.dist = dist;
		}
	}

	@Override
	public List<Edge> solve(UndirectedGraph G) {
		this.graph = G;
		List<Edge> bestResult = null;
		int bestVal = Integer.MAX_VALUE;
		List<Integer> terminals = new ArrayList<>();
		for (int i : this.graph.getTerminals().keySet())
			terminals.add(i);
		Random rand = new Random(42);
		while (terminals.size() != 0) {
			int t = terminals.remove(rand.nextInt(terminals.size()));
			List<Edge> result = this.dijkstraPathFinder(this.dijkstraOrdering(t));
			int val = 0;
			for (Edge e : result)
				val += e.getCost().get();
			System.out.println("Terminal " + t + " scores " + val);
			if (val < bestVal) {
				bestResult = result;
				bestVal = val;
			}
		}
		System.out.println("Best val: " + bestVal);
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
	 * @author Joshua Scheidt
	 */
	private List<Edge> dijkstraPathFinder(ArrayList<Integer> ordering) {
		HashSet<Integer> solNodes = new HashSet<>();
		List<Edge> result = new ArrayList<>();
		solNodes.add(ordering.get(0));
		for (int i = 1; i < ordering.size(); i++) {
			ArrayList<Integer> Q = new ArrayList<>();
			HashMap<Integer, DijkstraInfo> datamap = new HashMap<>();
			for (Integer v : this.graph.getVertices().keySet()) {
				datamap.put(v, new DijkstraInfo(Integer.MAX_VALUE));
				Q.add(v);
			}
			datamap.get(ordering.get(i)).dist = 0;
			int foundEnd = -1;
			while (!Q.isEmpty()) {
				int smallestDist = Integer.MAX_VALUE;
				Integer current = -1;
				for (Integer v : Q) {
					if (datamap.get(v).dist < smallestDist) {
						current = v;
						smallestDist = datamap.get(v).dist;
					}
				}
				if (current == null)
					System.out.println("ERROR: No shortest distance vertex found with distance < INTEGER.MAX_VALUE");
				if (solNodes.contains(current)) {
					foundEnd = current;
					break;
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
						nbInfo.parent = current;
					}
				}

			}
			Integer current = foundEnd;
			while (datamap.get(current).parent != -1) {
				solNodes.add(current);
				if (this.graph.getVertices().get(current).getConnectingEdge(this.graph.getVertices().get(datamap.get(current).parent))
						.getStack() != null) {
					Stack<int[]> stack = this.graph.getVertices().get(current)
							.getConnectingEdge(this.graph.getVertices().get(datamap.get(current).parent)).getStack();
					for (int j = 0; j < stack.size(); j++) {
						solNodes.add(stack.get(j)[0]);
						result.add(this.graph.getVertices().get(stack.get(j)[0]).getConnectingEdge(this.graph.getVertices().get(stack.get(j)[1])));
					}
				} else
					result.add(this.graph.getVertices().get(current).getConnectingEdge(this.graph.getVertices().get(datamap.get(current).parent)));
				current = datamap.get(current).parent;
			}
			solNodes.add(current);
		}
		return result;
	}

}