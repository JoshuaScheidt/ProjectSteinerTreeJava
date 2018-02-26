/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graph;

import java.util.HashSet;
import interfaces.Connection;
import interfaces.Node;

/**
 *
 * @author Marciano
 */
public class Vertex extends Object implements Node{

    private int key;
    private HashSet<Node> neighbors = new HashSet<>();
    private boolean isTerminal = false;
    
    public Vertex(){}
    
    public Vertex(boolean isTerminal) {
    	this.isTerminal=isTerminal;
	}
    
    public Vertex(int key){
        this.key = key;
    }

    public void addNeighbor(Vertex v){
        this.neighbors.add(v);
    }
    @Override
    public HashSet<Node> getNeighbors() {
        return this.neighbors;
    }

    @Override
    public boolean isNeighbor(Node N) {
        if(this.neighbors.contains((Vertex) N)){
            return true;
        } else {
            return false;
        }
    }
    
    public boolean getTerminal() {
    	return this.isTerminal;
    }
    
    public void setTerminal(boolean isTerminal) {
    	this.isTerminal = isTerminal;
    }
    
    public int getKey() {
    	return this.key;
    }
}
