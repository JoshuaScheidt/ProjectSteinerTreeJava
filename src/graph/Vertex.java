/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graph;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Stack;

/**
 *
 * @author Marciano
 */
public class Vertex extends Object {

    private int key;
    private HashSet<Edge> edges = new HashSet<>();
    private Stack<double[]> subsumed;
    private boolean isTerminal = false;

    /**
     * Empty constructor to be used when no other information is available
     */
    public Vertex() {
    }

    /**
     * Constructor which immediately sets if this current Vertex is a terminal
     *
     * @param isTerminal Boolean value which decides if it is a terminal or not.
     */
    public Vertex(boolean isTerminal) {
        this.isTerminal = isTerminal;
    }

    /**
     * Constructor which takes in the key for use with a HashMap
     *
     * @param key The Integer key which holds the key to its location in a
     * HashMap
     */
    public Vertex(int key) {
        this.key = key;
    }

    /**
     * Adds an Edge
     *
     * @param e The Edge to be added
     */
    public void addEdge(Edge e) {
        this.edges.add(e);
    }

    /**
     * Pushes an entire Stack to the current Stack, it will retain the order of
     * the input Stack which means the top item of the input Stack will also be
     * the top item of this objects Stack
     *
     * @param stack Input Stack of another Vertex
     */
    public void pushStack(Stack<double[]> stack) {
        if (this.subsumed.isEmpty()) {
            this.subsumed = new Stack<>();
        }
        for (int i = stack.size() - 1; i >= 0; i--) {
            this.pushSubsumed(stack.get(i));
        }
    }

    /**
     * Pushes Double array to the Stack
     *
     * @param keys Items to be added to Stack
     */
    public void pushSubsumed(double[] keys) {
        if (this.subsumed == null) {
            this.subsumed = new Stack<>();
        }
        this.subsumed.push(keys);
    }

    /**
     * Looks at the top item without removing
     *
     * @return Integer array of the top item
     */
    public double[] peekSubsumed() {
        return this.subsumed.peek();
    }

    /**
     * Removes the top item from the Stack
     *
     * @return Integer array of the top item
     */
    public double[] popSubsumed() {
        return this.subsumed.pop();
    }

    /**
     * Checks if the input Vertex v is a neighbor of the current Vertex
     *
     * @param v Vertex to be checked
     * @return True if the Vertex v is a neighbor, false if it isn't
     */
    public boolean isNeighbor(Vertex v) {
        if (this.getNeighbors().contains((Vertex) v)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Should return the edge that connects this Vertex to Vertex v Returns null
     * when v is not a neighbor of this object
     *
     * @param v Vertex to be compared to the edges in the current Vertex' Edges
     * set
     * @return Edge which should be the connection between this Vertex and v
     */
    public Edge getConnectingEdge(Vertex v) {
        if (this.getNeighbors().contains(v)) {
            for (Edge e : this.edges) {
                if (e.getVertices()[0].getKey() == v.getKey()) {
                    return e;
                } else if (e.getVertices()[1].getKey() == v.getKey()) {
                    return e;
                }
            }
        }
        return null;
    }

    /**
     * Specialized method for use with degree 2 vertices This returns the
     * neighbor which is not v, return null if v is not a neighbor
     *
     * @param v Vertex v is the neighbor that should not be selected
     * @return The neighbor that is not v
     */
    public Vertex getOtherNeighborVertex(Vertex v) {
        HashSet<Vertex> neigh = this.getNeighbors();
        if (neigh.contains(v) && this.edges.size() == 2) {
            if(((Vertex)(neigh.toArray()[0])).getKey() == v.getKey()){
                return (Vertex)(neigh.toArray()[0]);
            } else {
                return (Vertex)(neigh.toArray()[1]);
            }
        }
        return null;
    }

    /**
     * This is a specialised method which assumes a Vertex has degree 2
     *
     * @param e Edge e should be checked against the other Edges and return not
     * itself
     * @return The other Edge will be returned
     */
    public Edge getOtherEdge(Edge e) {
        if (this.edges.size() == 2 && this.edges.contains(e)) {
            for (Edge n : this.edges) {
                if (!n.equals(e)) {
                    return n;
                }
            }
        }
        return null;
    }

    /**
     * Returns a HashSet of Vertices which are neighbors connected through one
     * of the edges of this object
     *
     * @return HashSet of neighboring Vertices
     */
    public HashSet<Vertex> getNeighbors() {
        HashSet<Vertex> temp = new HashSet<>();
        Iterator it = this.edges.iterator();
        while (it.hasNext()) {
            temp.add(((Edge) (it.next())).getOtherSide(this));
        }
        return temp;
    }

    /**
     * Returns HashSet of Edges that are connected to this Vertex
     *
     * @return Edges
     */
    public HashSet<Edge> getEdges() {
        return this.edges;
    }

    /**
     * Checks if the Vertex is a terminal
     *
     * @return True if it is a terminal, false if it is not a terminal
     */
    public boolean isTerminal() {
        return this.isTerminal;
    }

    /**
     * Sets the Vertex to be a terminal, it is not necessary to set the terminal
     * to false because the initialised value is false.
     *
     * @param isTerminal Boolean true if terminal, false if not
     */
    public void setTerminal(boolean isTerminal) {
        this.isTerminal = isTerminal;
    }

    /**
     * Returns the key of itself
     *
     * @return Integer key to get its location in the HashMap
     */
    public int getKey() {
        return this.key;
    }

    /**
     * Returns the stack of subsumed Vertices which holds Integer arrays of size
     * 2 index 0 and 1 hold the keys of the subsumed edge and vertex, index 2
     * holds the cost of the edge between the vertices
     *
     * @return Stack of subsumed
     */
    public Stack getSubsumed() {
        return this.subsumed;
    }
}
