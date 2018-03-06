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

    private ArrayList<Vertex> connected = new ArrayList<>();
    private Optional<Integer> cost;
    
    
    public Edge(Vertex v1, Vertex v2, int c){
        if(!v1.isNeighbor(v2)){
            this.connected.add(v1);
            this.connected.add(v2);
            v1.addNeighbor(this);
            v2.addNeighbor(this);
            this.cost = Optional.of(c);
        }
    }
    
    public ArrayList<Vertex> getNodes() {
        return this.connected;
    }
    
    public Vertex getOtherSide(Vertex N) {
        int index = -1;
        if(this.connected.get(0).equals(N) || this.connected.get(1).equals(N)){
            if(this.connected.get(0).equals(N)){
                return this.connected.get(1);
            } else {
                return this.connected.get(0);
            }
        } else {
            return null;
        }
    }
    
    public Optional<Integer> getCost() {
        return this.cost;
    }
}
