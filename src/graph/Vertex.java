/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graph;

import java.util.HashSet;
import interfaces.Connection;
import interfaces.Node;
import java.util.Iterator;

/**
 *
 * @author Marciano
 */
public class Vertex extends Object implements Node{

    private int key;
    private HashSet<Connection> neighbors = new HashSet<>();
    private boolean isTerminal = false;
    
    public Vertex(){}
    
    public Vertex(boolean isTerminal) {
    	this.isTerminal=isTerminal;
	}
    
    public Vertex(int key){
        this.key = key;
    }

    public void addNeighbor(Connection c){
        this.neighbors.add(c);
    }
    @Override
    public HashSet<Node> getNeighbors() {
        HashSet<Node> temp = new HashSet<>();
        Iterator it = this.neighbors.iterator();
        while(it.hasNext()){
            temp.add((Node) it.next());
        }
        return temp;
    }

    @Override
    public boolean isNeighbor(Node N) {
        if(this.getNeighbors().contains((Vertex) N)){
            return true;
        } else {
            return false;
        }
    }
    
    public boolean isTerminal() {
    	return this.isTerminal;
    }
    
    public void setTerminal(boolean isTerminal) {
    	this.isTerminal = isTerminal;
    }
    
    public int getKey() {
    	return this.key;
    }
}
