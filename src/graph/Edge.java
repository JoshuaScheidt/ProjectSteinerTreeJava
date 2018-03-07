/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graph;

import java.util.ArrayList;
import java.util.Optional;
import java.util.Stack;
/**
 *
 * @author Marciano
 */
public class Edge extends Object {

    private Vertex[] connected = new Vertex[2];
    private Optional<Integer> cost;
    
    /**
     * Constructor for creating an Edge given 2 Vertices and a cost
     * @param v1 Vertex one to be connected to Vertex two
     * @param v2 Vertex two to be connected to Vertex one
     * @param c Cost of taking this Edge into your Minimum Steiner Tree
     */
    public Edge(Vertex v1, Vertex v2, int c){
        if(!v1.isNeighbor(v2)){
            this.connected[0] = v1;
            this.connected[1] = v2;
            v1.addEdge(this);
            v2.addEdge(this);
            this.cost = Optional.of(c);
        }
    }
    /**
     * Returns the Vertices attached to this Edge this cannot exceed 2
     * @return Array of 2 Vertices
     */
    public Vertex[] getVertices() {
        return this.connected;
    }
    /**
     * Gets the other side of the Edge given one side of the Edge by a Vertex v
     * @param v Vertex to be checked and given its opposite
     * @return A Vertex which is not v, if v is not in the connected array at all it will return null
     */
    public Vertex getOtherSide(Vertex v) {
        int index = -1;
        if(this.connected[0].equals(v) || this.connected[1].equals(v)){
            if(this.connected[0].equals(v)){
                return this.connected[1];
            } else {
                return this.connected[0];
            }
        } else {
            return null;
        }
    }
    /**
     * The cost is an Optional Integer to check if it had been set before 
     * @return Optional Integer from which the Integer can be received and can be checked if it had been set
     */
    public Optional<Integer> getCost() {
        return this.cost;
    }
}
