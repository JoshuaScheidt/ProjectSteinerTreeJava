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
    
    public Vertex(){}
    
    public Vertex(boolean isTerminal) {
    	this.isTerminal=isTerminal;
	}
    
    public Vertex(int key){
        this.key = key;
    }

    public void addNeighbor(Edge c){
        this.edges.add(c);
    }
    
    public HashSet<Vertex> getNeighbors() {
        HashSet<Vertex> temp = new HashSet<>();
        Iterator it = this.edges.iterator();
        while(it.hasNext()){
            temp.add(((Edge)(it.next())).getOtherSide(this));
        }
        return temp;
    }
    public HashSet<Edge> getEdges(){
        return this.edges;
    }

    public boolean isNeighbor(Vertex N) {
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
