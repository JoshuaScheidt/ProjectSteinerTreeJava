/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Stack;

/**
 *
 * @author Marciano
 */
public class UndirectedGraph {

	private HashMap<Integer, Vertex> vertices = new HashMap<>();
	private HashSet<Edge> edges = new HashSet<>();
	private HashMap<Integer, Vertex> terminals = new HashMap<>();
	private ArrayList<double[]> preliminaryResult = new ArrayList<>();

	/**
	 * Empty constructor to allow for iterative additions of edges and vertices
	 */
	public UndirectedGraph() {
	}

	/**
	 * Constructor to create a graph from a predefined set of edges and vertices
	 *
	 * @param V
	 *            Vertices to be added to the new graph
	 * @param E
	 *            Edges to be added to the new graph
	 */
	public UndirectedGraph(ArrayList<Vertex> V, ArrayList<Edge> E) {
		V.forEach((v) -> {
			this.addVertex(v);
		});
		Vertex[] n;
		for (Edge e : E) {
			n = e.getVertices();
			if (this.vertices.containsKey(n[0].getKey()) && this.vertices.containsKey(n[1].getKey())) {
				this.edges.add(e);
			} else if (this.vertices.containsKey(n[0].getKey())) {
				this.vertices.put(n[1].getKey(), n[1]);
				this.edges.add(e);
			} else {
				this.vertices.put(n[0].getKey(), n[0]);
				this.edges.add(e);
			}
		}
	}

	/**
	 * This Method removes any edges and vertices not contained within the remainder
	 * List. This is to check if a solution is correct (in the matter of
	 * connectivity not optimality)
	 *
	 * @param remainder
	 *            Your resulting edge list that you want to have in the graph
	 */
	public void checkConnectivity(ArrayList<Edge> remainder) {
		HashSet<Integer> doNotRemove = new HashSet<>(), remove = new HashSet<>();
		remainder.forEach((e) -> {
			if (!(doNotRemove.contains(e.getVertices()[0].getKey()))) {
				doNotRemove.add(e.getVertices()[0].getKey());
			}
			if (!(doNotRemove.contains(e.getVertices()[1].getKey()))) {
				doNotRemove.add(e.getVertices()[1].getKey());
			}
		});
		System.out.println(Arrays.toString(doNotRemove.toArray()));
		for (Vertex v : this.vertices.values()) {
			if (!(doNotRemove.contains(v.getKey()))) {
				if (this.terminals.containsKey(v.getKey())) {
					System.out.println("You removed a terminal from the graph: " + v.getKey());
					System.exit(0);
				}
				remove.add(v.getKey());
			}
		}
		for (Integer i : remove) {
			this.removeVertex(this.vertices.get(i));
		}
		HashMap<Integer, Boolean> visited = new HashMap<>();
		for (Integer i : doNotRemove) {
			visited.put(i, false);
		}
		Stack<Integer> traversal = new Stack<>();
		traversal.add((int) doNotRemove.iterator().next());
		int current;
		int numberOfTerminals = 0;
		while (!traversal.isEmpty()) {
			current = traversal.pop();
			if (visited.get(current) == false) {
				visited.replace(current, true);
				if (this.vertices.get(current).isTerminal()) {
					numberOfTerminals++;
				}
			}
			for (Vertex v : this.vertices.get(current).getNeighbors()) {
				if (visited.get(v.getKey()) == false) {
					traversal.add(v.getKey());
				}
			}
		}
		if (!(this.getNumberOfTerminals() == numberOfTerminals)) {
			System.out.println("Number of terminals mismatch " + this.getNumberOfTerminals() + " and found " + numberOfTerminals);
			System.exit(1);
		}
		for (Boolean b : visited.values()) {
			if (b == false) {
				System.out.println("Not connected. Some Vertices cannot be reached from any other Vertex");
				System.exit(2);
			}
		}
		System.out.println("It is connected");
	}

	public void rangeCheck() {
		HashMap<Integer, Integer> ranges = new HashMap<>();
		int cost = 0;
		for (Edge e : this.getEdges()) {
			cost = e.getCost().get();
			if (ranges.containsKey(e.getCost().get())) {
				ranges.replace(cost, ranges.get(cost).intValue(), ranges.get(cost) + 1);
			} else {
				ranges.put(cost, 1);
			}
		}
		for (Integer i : ranges.keySet()) {
			System.out.println("Cost: " + i + " is found " + ranges.get(i) + " times");
		}
	}

	/**
	 * This method adds a vertex to the graph it also checks if it already contains
	 * this vertex if it does it will not add it
	 *
	 * @param N
	 *            The vertex to be added
	 */
	public void addVertex(Vertex N) {
		if (!(N instanceof Vertex) || this.vertices.containsValue(N)) {
			return;
		}
		this.vertices.put(N.getKey(), N);
	}

	/**
	 * Method for constructing edges using only keys. It checks if these keys have
	 * been used and else will use the initialised Vertex If neither has been
	 * initialised this will be done
	 *
	 * @param key1
	 *            Key of the first Vertex
	 * @param key2
	 *            Key of the second Vertex
	 * @param cost
	 *            Cost of the Edge between the Vertices
	 */
	public Edge addEdge(int key1, int key2, int cost) {
		Edge e = null;
		if (this.vertices.containsKey(key1) || this.vertices.containsKey(key2)) {
			if (this.vertices.containsKey(key1) && this.vertices.containsKey(key2)) {
				e = new Edge(this.vertices.get(key1), this.vertices.get(key2), cost);
				this.edges.add(e);
			} else if (this.vertices.containsKey(key1)) {
				this.vertices.put(key2, new Vertex(key2));
				e = new Edge(this.vertices.get(key1), this.vertices.get(key2), cost);
				this.edges.add(e);
			} else if (this.vertices.containsKey(key2)) {
				this.vertices.put(key1, new Vertex(key1));
				e = new Edge(this.vertices.get(key1), this.vertices.get(key2), cost);
				this.edges.add(e);
			}
		} else {
			this.vertices.put(key1, new Vertex(key1));
			this.vertices.put(key2, new Vertex(key2));
			e = new Edge(this.vertices.get(key1), this.vertices.get(key2), cost);
			this.edges.add(e);
		}
		return e;
	}

	/**
	 * Adds edge to the graph given 2 vertices and a cost It will add vertices to
	 * the graph if they weren't already in there
	 *
	 * @param v1
	 *            First Vertex to be added
	 * @param v2
	 *            Second Vertex to be added
	 * @param cost
	 *            Cost of the Edge
	 * @return The created Edge
	 */
	public Edge addEdge(Vertex v1, Vertex v2, int cost) {
		Edge e = null;
		if (this.vertices.containsValue(v1) || this.vertices.containsValue(v2)) {
			if (this.vertices.containsValue(v1) && this.vertices.containsValue(v2)) {
				e = new Edge(v1, v2, cost);
				this.edges.add(e);
			} else if (this.vertices.containsValue(v1)) {
				this.vertices.put((v2).getKey(), v2);
				e = new Edge(v1, v2, cost);
				this.edges.add(e);
			} else if (this.vertices.containsValue(v2)) {
				this.vertices.put((v1).getKey(), v1);
				e = new Edge(v1, v2, cost);
				this.edges.add(e);
			}
		} else {
			this.vertices.put((v1).getKey(), v1);
			this.vertices.put((v2).getKey(), v2);
			e = new Edge(v1, v2, cost);
			this.edges.add(e);
		}
		return e;
	}

	/**
	 * Adds an edge to the graph using an already constructed edge.
	 *
	 * @param edge
	 * @return
	 *
	 * @author Marciano Geijselaers
	 * @author Joshua Scheidt
	 */
	public Edge addEdge(Edge edge) {
		if (!this.vertices.containsValue(edge.getVertices()[0])) {
			this.vertices.put(edge.getVertices()[0].getKey(), edge.getVertices()[0]);
		}
		if (!this.vertices.containsValue(edge.getVertices()[1])) {
			this.vertices.put(edge.getVertices()[1].getKey(), edge.getVertices()[1]);
		}
		this.edges.add(edge);
		return edge;
	}

	/**
	 * This method sets the terminals for the existing vertices in the graph
	 *
	 * @param keys
	 *            The keys for which vertex is a terminal
	 */
	public void setTerminals(Set<Integer> keys) {
		for (Integer key : keys) {
			if (this.vertices.containsKey(key)) {
				this.vertices.get(key).setTerminal(true);
				this.terminals.put(key, this.vertices.get(key));
			} else {
				System.out.println("Terminal appointed to non-existing Vertex");
			}
		}
	}

	/**
	 * This method sets an individual Vertex to be a terminal
	 *
	 * @param key
	 *            Key of Vertex to be made terminal
	 */
	public void setTerminal(int key) {
		if (this.vertices.containsKey(key)) {
			this.vertices.get(key).setTerminal(true);
			this.terminals.put(key, this.vertices.get(key));
		} else {
			System.out.println("Terminal appointed to non-existing Vertex");
		}
	}

	/**
	 * Clone method to provide a deepclone of the current graph
	 *
	 * @return An exact clone of the original called graph
	 */
	@Override
	public UndirectedGraph clone() {
		UndirectedGraph graph = new UndirectedGraph();
		for (Edge e : this.edges) {
			graph.addEdge(e.getVertices()[0].getKey(), e.getVertices()[1].getKey(), e.getCost().get());
		}
		for (int key : this.terminals.keySet()) {
			(graph.vertices.get(key)).setTerminal(true);
		}
		graph.setTerminals(this.terminals.keySet());
		return graph;
	}

	/**
	 * This method removes an Edge from each of its connections Beware it doesn't
	 * check connectivity or create a new edge
	 *
	 * @param e
	 *            The edge to be removed
	 */
	public void removeEdge(Edge e) {
		for (Vertex v : e.getVertices()) {
			v.getEdges().remove(e);
		}
		this.edges.remove(e);
		e = null;
	}

	/**
	 * Returns HashMap of the Terminals in the graph
	 *
	 * @return The HashMap in question
	 */
	public HashMap<Integer, Vertex> getTerminals() {
		return this.terminals;
	}

	/**
	 * Removes a vertex from the graph this included removing all its edge
	 * connection and removing all of these edges from its neighbours If the removal
	 * of an edge creates a disconnected component that neighbour will also be
	 * removed
	 *
	 * @param v
	 *            The key the to be removed vertex has
	 */
	public void removeVertex(Vertex v) {
		HashSet<Edge> toBeRemoved = new HashSet<>();
		for (Edge e : v.getEdges()) {
			toBeRemoved.add(e);
			this.edges.remove(e);
		}
		Vertex neighbour;
		for (Edge e : toBeRemoved) {
			neighbour = e.getOtherSide(v);
			if (neighbour.getNeighbors().size() == 1) {
				neighbour = null;
			} else {
				neighbour.removeEdge(e);
			}
			e = null;
		}
		if (v.isTerminal()) {
			this.terminals.remove(v.getKey());
		}
		this.vertices.remove(v.getKey());
		v = null;
	}

	/**
	 * Returns the HashMap of the Vertices in the current graph
	 *
	 * @return The HashMap in question
	 */
	public HashMap<Integer, Vertex> getVertices() {
		return this.vertices;
	}

	/**
	 * Returns the HashMap of the Edges in the current graph
	 *
	 * @return The HashMap in question
	 */
	public HashSet<Edge> getEdges() {
		return this.edges;
	}

	/**
	 * Get the number of terminal Vertices in the current graph
	 *
	 * @return Non-negative integer which holds the number of terminals
	 */
	public int getNumberOfTerminals() {
		return this.terminals.size();
	}

	// The methods below are for testing and requesting certain information from the
	// graph
	public boolean[] preProcessable() {
		boolean[] pp = new boolean[] { false, false };
		Iterator it = this.vertices.keySet().iterator();
		Vertex current;
		int neighbours;
		while (it.hasNext()) {
			current = this.vertices.get((int) it.next());
			neighbours = current.getEdges().size();
			if (neighbours == 1 && !pp[0]) {
				pp[0] = true;
			} else if (neighbours == 2 && !current.isTerminal() && !pp[1]) {
				pp[1] = true;
			}
		}
		return pp;
	}

	public int[] countDegree() {
		Set keyset = this.getVertices().keySet();
		HashMap<Integer, Vertex> vertices = this.getVertices();
		Iterator it = keyset.iterator();
		int[] degrees = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		Vertex temp;
		int key, number;
		while (it.hasNext()) {
			key = (Integer) it.next();
			temp = vertices.get(key);
			number = temp.getNeighbors().size();
			if (number >= degrees.length + 1) {
				continue;
			}
			degrees[number - 1]++;
		}
		return degrees;
	}

	public Edge edgeBetweenVertices(Vertex v, Vertex u) throws GraphException {
		for (Edge e : v.getEdges()) {
			if (e.getOtherSide(v) == u) {
				return e;
			}
		}
		throw new GraphException("No edges from v contain vertex u.");
	}

	/**
	 * Returns all the partial graphs from each separate component in this graph. If
	 * the graph is connected, it will simply return the complete graph after
	 * performing a complete search.
	 *
	 * @return List of separate graphs
	 *
	 * @author Joshua Scheidt
	 */
	public ArrayList<UndirectedGraph> getDisconnectedComponents(ArrayList<Edge> bridges) {
		Set<Vertex> bridgePoints = new HashSet<>();
		for (Edge e : bridges) {
			bridgePoints.add(e.getVertices()[0]);
			bridgePoints.add(e.getVertices()[1]);
		}

		return null;
	}

	/**
	 * Adds a complete graph to this one.
	 *
	 * @param addable
	 *            the to be added graph
	 *
	 * @author Joshua Scheidt
	 */
	public void addGraph(UndirectedGraph addable) {
		for (Edge e : addable.getEdges()) {
			this.addEdge(e.getVertices()[0].getKey(), e.getVertices()[1].getKey(), e.getCost().get());
			if (e.getStack() != null)
				this.getVertices().get(e.getVertices()[0].getKey()).getConnectingEdge(this.getVertices().get(e.getVertices()[1].getKey()))
						.pushStack(e.getStack());
		}
		for (Vertex t : addable.getTerminals().values()) {
			this.setTerminal(t.getKey());
		}
		for (Vertex v : addable.getVertices().values()) {
			if (v.getSubsumed() != null) {
				this.getVertices().get(v.getKey()).pushStack(v.getSubsumed());
			}
		}
	}
}
