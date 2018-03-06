/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graph;

import java.util.HashSet;
import java.util.Iterator;

/**
 *
 * @author Marciano
 */
public class Vertex extends Object {

    private int key;
    private HashSet<Edge> edges = new HashSet<>();
    private boolean isTerminal = false;
    
    /**
     * Empty constructor to be used when no other information is available
     */
    public Vertex(){}
    /**
     * Constructor which immediately sets if this current Vertex is a terminal
     * @param isTerminal Boolean value which decides if it is a terminal or not.
     */
    public Vertex(boolean isTerminal) {
    	this.isTerminal=isTerminal;
    }
    /**
     * Constructor which takes in the key for use with a HashMap
     * @param key The Integer key which holds the key to its location in a HashMap
     */
    public Vertex(int key){
        this.key = key;
    }
    /**
     * Adds an Edge
     * @param e The Edge to be added
     */
    public void addEdge(Edge e){
        this.edges.add(e);
    }
    /**
     * Returns a HashSet of Vertices which are neighbors connected through one of the edges of this object
     * @return HashSet of neighboring Vertices
     */
    public HashSet<Vertex> getNeighbors() {
        HashSet<Vertex> temp = new HashSet<>();
        Iterator it = this.edges.iterator();
        while(it.hasNext()){
            temp.add(((Edge)(it.next())).getOtherSide(this));
        }
        return temp;
    }
    /**
     * Returns HashSet of Edges that are connected to this Vertex
     * @return Edges
     */
    public HashSet<Edge> getEdges(){
        return this.edges;
    }
    /**
     * Checks if the input Vertex v is a neighbor of the current Vertex
     * @param v Vertex to be checked
     * @return True if the Vertex v is a neighbor, false if it isn't
     */
    public boolean isNeighbor(Vertex v) {
        if(this.getNeighbors().contains((Vertex) v)){
            return true;
        } else {
            return false;
        }
    }
    /**
     * Checks if the Vertex is a terminal
     * @return True if it is a terminal, false if it is not a terminal
     */
    public boolean isTerminal() {
    	return this.isTerminal;
    }
    /**
     * Sets the Vertex to be a terminal, it is not necessary to set the terminal to false
     * because the initialised value is false.
     * @param isTerminal Boolean true if terminal, false if not
     */
    public void setTerminal(boolean isTerminal) {
    	this.isTerminal = isTerminal;
    }
    /**
     * Returns the key of itself
     * @return Integer key to get its location in the HashMap
     */
    public int getKey() {
    	return this.key;
    }
}
