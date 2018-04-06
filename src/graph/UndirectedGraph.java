/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

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
	public Edge addEdge(int key1, int key2, double cost) {
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
	 * Removes a vertex from the graph this included removing all its edge
	 * connection and removing all of these edges from its neighbors
	 *
	 * @param v
	 *            The vertex to be removed
	 */
	public void removeVertex(Vertex v) {
		for (Edge e : v.getEdges()) {
			this.edges.remove(e);
		}
		int key = v.getKey();
		if (v.isTerminal()) {
			this.terminals.keySet().remove(key);
		}
		this.vertices.keySet().remove(key);
		v = null;
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
	 * connection and removing all of these edges from its neighbours
	 *
	 * @param key
	 *            The key the to be removed vertex has
	 */
	public void removeVertex(int key) {
		Vertex v = this.getVertices().get(key);
		HashSet<Edge> toBeRemoved = new HashSet<>();
		for (Edge e : v.getEdges()) {
			toBeRemoved.add(e);
			this.edges.remove(e);
		}
		for (Edge e : toBeRemoved) {
                        e.getOtherSide(v).removeEdge(e);
			v.removeEdge(e);
			e = null;
		}
		if (v.isTerminal()) {
			this.terminals.keySet().remove(key);
		}
		this.vertices.remove(key);
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
        public boolean[] preProcessable(){
            boolean[] pp = new boolean[]{false, false};
            Iterator it = this.vertices.keySet().iterator();
            Vertex current;
            int neighbours;
            while(it.hasNext()){
                current = this.vertices.get((int)it.next());
                neighbours = current.getEdges().size();
                if(neighbours == 1 && !pp[0]){
                    pp[0] = true;
                } else if(neighbours == 2 && !current.isTerminal() && !pp[1]){
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
			if (e.getOtherSide(v) == u)
				return e;
		}
		throw new GraphException("No edges from v contain vertex u.");
	}
}
