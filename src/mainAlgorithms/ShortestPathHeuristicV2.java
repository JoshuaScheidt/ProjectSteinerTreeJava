/*
 * All of the given code may be used on free will when referenced to the source.
 * Initial version created at 12:39:07
 */
package mainAlgorithms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;
import java.util.Stack;

import graph.Edge;
import graph.RandomMain;
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
		while (terminals.size() != 0 && counter < 15) {
			if (RandomMain.killed)
				break;
			int t = terminals.remove(rand.nextInt(terminals.size()));
			// System.out.println("Ordering for " + t + ": " +
			// Arrays.toString(this.dijkstraOrderingPrio(t).toArray()));
			List<Edge> result = this.dijkstraPathPrio(this.dijkstraOrderingPrio(t));
			if (result == null)
				return null;
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
				RandomMain.currentBest = bestResult;
				bestVal = val;
			}
			counter++;
		}
		// RandomMain.printSolution(RandomMain.currentBest, false);
		return this.improve(bestResult);
	}

	public List<Edge> improve(List<Edge> currentBest) {
		if (RandomMain.killed)
			return null;
		int upperBound = 3;
		int maxBound = 6;
		// First make a copy of the best result as graph without terminals
		UndirectedGraph currentPlain = new UndirectedGraph();

		for (Edge e : currentBest) {
			currentPlain.addEdge(e.getVertices()[0].getKey(), e.getVertices()[1].getKey(), e.getCost().get());
			if (e.getStack() != null) {
				currentPlain.getVertices().get(e.getVertices()[0].getKey())
						.getConnectingEdge(currentPlain.getVertices().get(e.getVertices()[1].getKey())).pushStack(e.getStack());
			}
			if (e.getVertices()[0].getSubsumed() != null && currentPlain.getVertices().get(e.getVertices()[0].getKey()).getSubsumed() == null) {
				currentPlain.getVertices().get(e.getVertices()[0].getKey()).pushStack(e.getVertices()[0].getSubsumed());
			}
			if (e.getVertices()[1].getSubsumed() != null && currentPlain.getVertices().get(e.getVertices()[1].getKey()).getSubsumed() == null) {
				currentPlain.getVertices().get(e.getVertices()[1].getKey()).pushStack(e.getVertices()[1].getSubsumed());
			}
		}
		ArrayList<Integer> terminals = new ArrayList<>();
		for (int key : this.graph.getTerminals().keySet()) {
			terminals.add(key);
		}

		boolean noNew = false;
		primary: while (!noNew) {
			if (RandomMain.killed)
				return null;
			noNew = true;
			HashSet<Integer> possibleStarts = new HashSet<>();
			// Check which vertices are worth looking at
			for (Vertex v : currentPlain.getVertices().values()) {
				if (terminals.contains(v.getKey()) && v.getEdges().size() >= 2) {
					possibleStarts.add(v.getKey());
				} else if (v.getEdges().size() > 2) {
					possibleStarts.add(v.getKey());
				}
			}
			while (!possibleStarts.isEmpty()) {
				Integer start = possibleStarts.iterator().next();
				HashSet<Edge> usedSet = new HashSet<>();
				// Perform breadth-first search on the start until it has found between lower
				// and upper bound terminals
				// Calculate the distance of the subtree
				HashSet<Integer> visited = new HashSet<>();
				LinkedList<Integer> queue = new LinkedList<>();
				HashSet<Integer> currentTerminals = new HashSet<>();
				int expectedTerminalsCount = 0;
				visited.add(start);
				queue.add(start);
				if (terminals.contains(start)) {
					expectedTerminalsCount++;
					currentTerminals.add(start);
				}
				Integer s;
				while (queue.size() > 0 && expectedTerminalsCount < upperBound) {
					if (RandomMain.killed)
						return null;
					// System.out.println(queue.size() + " " + expectedTerminalsCount + " " +
					// currentTerminals.size());
					s = queue.poll();
					if (!terminals.contains(s)) {
						expectedTerminalsCount--;
					} else
						currentTerminals.add(s);
					// System.out.println(currentPlain.getVertices().get(s));
					if (currentPlain.getVertices().get(s).getEdges().size() - 1 > upperBound - expectedTerminalsCount) {
						currentTerminals.add(s);
					} else
						for (Edge nb : currentPlain.getVertices().get(s).getEdges()) {
							if (!visited.contains(nb.getOtherSide(currentPlain.getVertices().get(s)).getKey())) {
								usedSet.add(nb);
								visited.add(nb.getOtherSide(currentPlain.getVertices().get(s)).getKey());
								queue.add(nb.getOtherSide(currentPlain.getVertices().get(s)).getKey());
								expectedTerminalsCount++;
							}
						}
				}
				// Check if all endPoints are terminals, if not, make them terminals.
				while (queue.size() > 0) {
					if (RandomMain.killed)
						return null;
					s = queue.poll();
					if (!terminals.contains(s)) {
						if (currentPlain.getVertices().get(s).getEdges().size() > 2) {
							currentTerminals.add(s);
						} else if (currentPlain.getVertices().get(s).getEdges().size() == 2) {
							for (Vertex v : currentPlain.getVertices().get(s).getNeighbors())
								if (!visited.contains(v.getKey())) {
									queue.add(v.getKey());
									usedSet.add(currentPlain.getVertices().get(s).getConnectingEdge(v));
									visited.add(v.getKey());
								}
						}
					} else {
						currentTerminals.add(s);
					}
				}

				int dist = 0;
				for (Edge e : usedSet) {
					dist += e.getCost().get();
				}
				// System.out.println("Number of terminals: " + currentTerminals.size());
				UndirectedGraph adapterClone = this.graph.cloneNoTerminal();
				for (Integer t : currentTerminals)
					adapterClone.setTerminal(t);
				// System.out.println("dist: " + dist);
				List<Edge> tmp = new mainAlgorithms.ImprovedDreyfusWagner().solve(adapterClone);
				int newDist = 0;
				for (Edge e : tmp) {
					newDist += e.getCost().get();
				}
				// System.out.println("new dist: " + newDist);

				// RandomMain.printSolution(RandomMain.currentBest, false);

				possibleStarts.removeAll(visited);
				// possibleStarts.remove(start);

				if (newDist < dist) {
					// System.out.println("Improvement from " + dist + " to " + newDist);
					// Better subgraph found
					noNew = false;
					for (Edge e : usedSet)
						currentPlain.removeEdge(e);
					for (Edge e : tmp) {
						currentPlain.addEdge(e.getVertices()[0].getKey(), e.getVertices()[1].getKey(), e.getCost().get());
						if (e.getStack() != null) {
							currentPlain.getVertices().get(e.getVertices()[0].getKey())
									.getConnectingEdge(currentPlain.getVertices().get(e.getVertices()[1].getKey())).pushStack(e.getStack());
						}
					}
					List<Edge> newBestSolution = new ArrayList<>();

					newBestSolution.addAll(currentPlain.getEdges());
					RandomMain.currentBest = newBestSolution;
					continue primary;
				}
				// RandomMain.printSolution(RandomMain.currentBest, false);
				// System.exit(1);
				// System.out.println("Iteration done");
			}
			// System.out.println("All starts done");
			if (noNew && upperBound != maxBound) {
				noNew = false;
				upperBound++;
			}
		}

		List<Edge> newOpt = new ArrayList<>();
		for (Edge e : currentPlain.getEdges())
			newOpt.add(e);

		return newOpt;
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
			if (RandomMain.killed)
				return null;
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
		if (ordering == null)
			return null;
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
				if (RandomMain.killed)
					return null;
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