/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graph;

import java.util.ArrayList;
import java.util.Optional;
import interfaces.Connection;
import interfaces.Node;
/**
 *
 * @author Marciano
 */
public class Edge extends Object implements Connection{

    private ArrayList<Node> connected = new ArrayList<>();
    private Optional<Integer> cost;
    
    public Edge(Vertex v1, Vertex v2, int c){
        if(!v1.isNeighbor(v2)){
            this.connected.add(v1);
            this.connected.add(v2);
            v1.addNeighbor(v2);
            v2.addNeighbor(v1);
            this.cost = Optional.of(c);
        }
    }

    @Override
    public ArrayList<Node> getNodes() {
        return this.connected;
    }

    @Override
    public Node getOtherSide(Node N) {
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

    @Override
    public Optional<Integer> getCost() {
        return this.cost;
    }
}
