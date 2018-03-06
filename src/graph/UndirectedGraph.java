/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graph;

import java.awt.BorderLayout;
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
    private int numberOfTerminals = 0;

    /**
     * Empty constructor to allow for iterative additions of edges and vertices
     */
    public UndirectedGraph() {
    }

    /**
     * Constructor to create a graph from a predefined set of edges and vertices
     *
     * @param V Vertices to be added to the new graph
     * @param E Edges to be added to the new graph
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
            } else if (this.vertices.containsKey(((Vertex) n[0]).getKey())) {
                this.vertices.put(((Vertex) n[1]).getKey(), n[1]);
                this.edges.add(e);
            } else {
                this.vertices.put(((Vertex) n[0]).getKey(), n[0]);
                this.edges.add(e);
            }
        }
    }

    /**
     * This method adds a vertex to the graph it also checks if it already
     * contains this vertex if it does it will not add it
     *
     * @param N The vertex to be added
     */
    public void addVertex(Vertex N) {
        if (!(N instanceof Vertex) || this.vertices.containsValue((Vertex) N)) {
            return;
        }
        this.vertices.put(((Vertex) N).getKey(), N);
    }

    /**
     * Method for constructing edges using only keys.
     * It checks if these keys have been used and else will use the initialised Vertex
     * If neither has been initialised this will be done
     * @param key1 Key of the first Vertex
     * @param key2 Key of the second Vertex
     * @param cost Cost of the Edge between the Vertices
     */
    public void addEdge(int key1, int key2, int cost){
        if (this.vertices.containsKey(key1) || this.vertices.containsKey(key2)) {
            if (this.vertices.containsKey(key1) && this.vertices.containsKey(key2)) {
                Edge e = new Edge(this.vertices.get(key1), this.vertices.get(key2), cost);
                this.edges.add(e);
            } else if (this.vertices.containsKey(key1)) {
                this.vertices.put(key2, new Vertex(key2));
                Edge e = new Edge(this.vertices.get(key1), this.vertices.get(key2), cost);
                this.edges.add(e);
            } else if (this.vertices.containsKey(key2)) {
                this.vertices.put(key1, new Vertex(key1));
                Edge e = new Edge(this.vertices.get(key1), this.vertices.get(key2), cost);
                this.edges.add(e);
            }
        } else {
            this.vertices.put(key1, new Vertex(key1));
            this.vertices.put(key2, new Vertex(key2));
            Edge e = new Edge(this.vertices.get(key1), this.vertices.get(key2), cost);
            this.edges.add(e);
        }
    }
    /**
     * Adds edge to the graph given 2 vertices and a cost It will add vertices
     * to the graph if they weren't already in there
     *
     * @param v1 First Vertex to be added
     * @param v2 Second Vertex to be added
     * @param cost Cost of the Edge
     */
    public void addEdge(Vertex v1, Vertex v2, int cost) {
        if (this.vertices.containsValue(v1) || this.vertices.containsValue(v2)) {
            if (this.vertices.containsValue(v1) && this.vertices.containsValue(v2)) {
                Edge e = new Edge(v1, v2, cost);
                this.edges.add(e);
            } else if (this.vertices.containsValue(v1)) {
                this.vertices.put((v2).getKey(), v2);
                Edge e = new Edge(v1, v2, cost);
                this.edges.add(e);
            } else if (this.vertices.containsValue(v2)) {
                this.vertices.put((v1).getKey(), v1);
                Edge e = new Edge(v1, v2, cost);
                this.edges.add(e);
            }
        } else {
            this.vertices.put((v1).getKey(), v1);
            this.vertices.put((v2).getKey(), v2);
            Edge e = new Edge(v1, v2, cost);
            this.edges.add(e);
        }
    }

    /**
     * This method sets the terminals for the existing vertices in the graph
     *
     * @param keys The keys for which vertex is a terminal
     */
    public void setTerminals(int[] keys) {
        for (int i = 0; i < keys.length; i++) {
            if (this.vertices.containsKey(keys[i])) {
                this.vertices.get(keys[i]).setTerminal(true);
                this.terminals.put(keys[i], this.vertices.get(keys[i]));
            } else {
                System.out.println("Terminal appointed to non-existing Vertex");
            }
        }
    }

    /**
     * Clone method to provide a deepclone of the current graph
     *
     * @return An exact clone of the original called graph
     */
    public UndirectedGraph clone() {
        UndirectedGraph graph = new UndirectedGraph();
        for (Edge e : this.edges) {
            graph.addEdge(new Vertex((e.getVertices()[0]).getKey()), new Vertex((e.getVertices()[1]).getKey()), e.getCost().get());
        }
        for (int key : this.terminals.keySet()) {
            ((Vertex) graph.vertices.get(key)).setTerminal(true);
        }
        return graph;
    }

    /**
     * This method removes an Edge from each of its connections Beware it
     * doesn't check connectivity or create a new edge
     *
     * @param e The edge to be removed
     */
    public void removeEdge(Edge e) {
        for (Vertex v : e.getVertices()) {
            v.getEdges().remove(e);
        }
        e = null;
    }

    /**
     * Removes a vertex from the graph this included removing all its edge
     * connection and removing all of these edges from its neighbors
     *
     * @param v The vertex to be removed
     */
    public void removeVertex(Vertex v) {
        for (Edge e : v.getEdges()) {
            Vertex neighbor = e.getOtherSide(v);
            neighbor.getEdges().remove(e);
            this.edges.remove(e);
            e = null;
        }
        int key = v.getKey();
        if (v.isTerminal()) {
            this.numberOfTerminals--;
            this.terminals.remove(key);
        }
        this.vertices.remove(key);
        v = null;
    }

    /**
     * A method which checks if a Vertex v resides in the current graph
     *
     * @param v The Vertex to be checked
     * @return True if it contains the vertex, false if it doesn't contain it
     */
    public boolean containsVertex(Vertex v) {
        return this.vertices.containsValue(v.getKey());
    }

    /**
     * A method which checks if an Edge e resides in the current graph
     *
     * @param e The Edge to be checked
     * @return True if it contains the vertex, false if it doesn't contain it
     */
    public boolean containsEdge(Edge e) {
        return this.edges.contains(e);
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
        return this.numberOfTerminals;
    }

    /**
     * Get the number of Edges in the graph
     *
     * @return Non-negative integer which holds the number of Edges
     */
    public int getEdgesSize() {
        return this.edges.size();
    }

    /**
     * Get the number of Vertices in the graph
     *
     * @return Non-negative integer which holds the number of Vertices
     */
    public int getVerticesSize() {
        return this.vertices.size();
    }

}
