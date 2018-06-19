/*
 * All of the given code may be used on free will when referenced to the source.
 * Initial version created at 12:39:07
 */
package mainAlgorithms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
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
		int counter = 0;
		long start = System.currentTimeMillis();
		while (terminals.size() != 0 && counter < 500) {
			int t = terminals.remove(rand.nextInt(terminals.size()));
			// System.out.println("Ordering: " +
			// Arrays.toString(this.dijkstraOrderingPrio(t).toArray()));
			List<Edge> result = this.dijkstraPathPrio(this.dijkstraOrderingPrio(t));
			// List<Edge> result = this.dijkstraPathFinder(this.dijkstraOrdering(t));
			int val = 0;
			for (Edge e : result) {
				if (e.getStack() == null) {
					val += e.getCost().get();
				} else {
					for (int[] i : e.getStack()) {
						val += i[2];
					}
				}
			}
			// System.out.println("Terminal " + t + " got val " + val);
			if (val < bestVal) {
				bestResult = result;
				bestVal = val;
			}
			counter++;
		}
		// for (Edge e : bestResult) {
		// if (e.getStack() == null) {
		// System.out.println(e.getVertices()[0].getKey() + " " +
		// e.getVertices()[1].getKey() + " " + e.getCost().get());
		// } else {
		// System.out.println("total stack cost: " + e.getCost().get());
		// for (int[] i : e.getStack()) {
		// System.out.println("stack " + i[0] + " " + i[1] + " " + i[2]);
		// }
		// }
		// }
		// System.out.println("Best val: " + bestVal + " took " +
		// (System.currentTimeMillis() - start));
		return bestResult;
	}

	private static class QueuePair implements Comparable<QueuePair> {

		int dist;
		int node;

		public QueuePair(int dist, int node) {
			this.dist = dist;
			this.node = node;
		}

		@Override
		public int compareTo(QueuePair o) {
			if (this.dist == o.dist)
				return 0;
			else if (this.dist < o.dist)
				return -1;
			else
				return 1;
		}
	}

	/**
	 * Returns the ordering on how the terminals need to be connected using priority
	 * queue.
	 *
	 * @param graph
	 *            the graph
	 * @param sk
	 *            the starting key
	 * @return the ordering
	 *
	 * @author Joshua Scheidt
	 */
	private ArrayList<Integer> dijkstraOrderingPrio(int sk) {
		ArrayList<Integer> order = new ArrayList<>();
		HashMap<Integer, Integer> distance = new HashMap<>();
		HashSet<Integer> processed = new HashSet<>();
		for (Vertex v : this.graph.getVertices().values())
			distance.put(v.getKey(), Integer.MAX_VALUE);

		Queue<QueuePair> Q = new PriorityQueue<>();
		Q.add(new QueuePair(0, sk));
		distance.replace(sk, 0);
		int numTerms = 0;
		int totTerms = this.graph.getNumberOfTerminals();

		while (!Q.isEmpty() && numTerms < totTerms) {
			int current = Q.poll().node;
			if (processed.contains(current))
				continue;
			processed.add(current);
			if (this.graph.getTerminals().containsKey(current)) {
				order.add(current);
				// System.out.println("Found node " + current);
				numTerms++;
			}
			for (Vertex nb : this.graph.getVertices().get(current).getNeighbors()) {
				Edge e = this.graph.getVertices().get(current).getConnectingEdge(nb);
				if (distance.get(current) + e.getCost().get() < distance.get(nb.getKey())) {
					distance.replace(nb.getKey(), distance.get(current) + e.getCost().get());
					Q.add(new QueuePair(distance.get(nb.getKey()), nb.getKey()));
				}
			}
		}
		// for (Integer i : order)
		// System.out.println("Node " + i + " has dist " + distance.get(i));
		return order;
	}

	/**
	 * Finds the path corresponding to the found ordering using priority queue.
	 *
	 * @param ordering
	 *            the ordering of the terminals
	 * @return
	 *
	 * @author Joshua Scheidt
	 */
	private List<Edge> dijkstraPathPrio(ArrayList<Integer> ordering) {
		HashSet<Integer> solNodes = new HashSet<>();
		List<Edge> result = new ArrayList<>();
		solNodes.add(ordering.get(0));
		for (int i = 1; i < ordering.size(); i++) {
			// System.out.println("solnodes:" + Arrays.toString(solNodes.toArray()));
			HashMap<Integer, Integer> distance = new HashMap<>();
			HashSet<Integer> processed = new HashSet<>();
			HashMap<Integer, Integer> parent = new HashMap<>();
			for (Vertex v : this.graph.getVertices().values())
				distance.put(v.getKey(), Integer.MAX_VALUE);

			Queue<QueuePair> Q = new PriorityQueue<>();
			Q.add(new QueuePair(0, ordering.get(i)));
			int current = -1;
			while (!Q.isEmpty()) {
				current = Q.poll().node;
				if (processed.contains(current)) {
					// System.out.println("processed contains:" + current);
					continue;
				}
				processed.add(current);
				if (solNodes.contains(current)) {
					// System.out.println("current=" + current);
					break;
				}
				for (Vertex nb : this.graph.getVertices().get(current).getNeighbors()) {
					// System.out.println("nb from " + current + " with key " + nb.getKey());
					if (processed.contains(nb.getKey())) {
						// System.out.println("Processed:" + nb.getKey());
						continue;
					}
					Edge e = this.graph.getVertices().get(current).getConnectingEdge(nb);
					if (distance.get(current) + e.getCost().get() < distance.get(nb.getKey())) {
						distance.replace(nb.getKey(), distance.get(current) + e.getCost().get());
						parent.put(nb.getKey(), current);
						Q.add(new QueuePair(distance.get(nb.getKey()), nb.getKey()));
					}
				}
			}
			int parentVal;
			while (parent.containsKey(current)) {
				parentVal = parent.get(current);
				solNodes.add(current);
				result.add(this.graph.getVertices().get(current).getConnectingEdge(this.graph.getVertices().get(parentVal)));
				current = parentVal;
			}
			solNodes.add(current);
		}
		return result;
	}

	// UNUSED
	// UNUSED
	// UNUSED
	// UNUSED
	// UNUSED
	// UNUSED
	// UNUSED
	// UNUSED
	// UNUSED
	// UNUSED
	// UNUSED
	// UNUSED
	// UNUSED
	// UNUSED

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
		HashSet<Integer> Q = new HashSet<>();
		HashSet<Integer> seen = new HashSet<>();
		HashMap<Integer, DijkstraInfo> datamap = new HashMap<>();
		// for (Integer v : this.graph.getVertices().keySet()) {
		// datamap.put(v, new DijkstraInfo(Integer.MAX_VALUE));
		// Q.add(v);
		// }
		Q.add(sk);
		datamap.put(sk, new DijkstraInfo(0));
		int numReachedEnd = 0;
		while (true) {
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
			// System.out.println(current);
			Q.remove(current);
			if (current == -1)
				System.out.println("ERROR: No shortest distance vertex found with distance < INTEGER.MAX_VALUE");

			if (this.graph.getTerminals().keySet().contains(current)) {
				numReachedEnd++;
				ordering.add(current);
			}
			Q.remove(current);
			seen.add(current);
			int distToCur = datamap.get(current).dist;
			int totDistToNb = 0;
			for (Vertex nb : this.graph.getVertices().get(current).getNeighbors()) {
				if (seen.contains(nb.getKey()))
					continue;
				Q.add(nb.getKey());
				totDistToNb = distToCur + this.graph.getVertices().get(current).getConnectingEdge(nb).getCost().get();
				DijkstraInfo nbInfo = datamap.get(nb.getKey());
				if (nbInfo == null) {
					datamap.put(nb.getKey(), new DijkstraInfo(Integer.MAX_VALUE));
					nbInfo = datamap.get(nb.getKey());
				}
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