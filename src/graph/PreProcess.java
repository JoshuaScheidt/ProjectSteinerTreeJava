/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graph;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.HashMap;

/**
 *
 * @author Marciano
 */
public class PreProcess {
    
    UndirectedGraph graph;
    
    public PreProcess(UndirectedGraph g){
        this.graph = g.clone();
    }
    
    public void reduceSize(){
        Set keys = this.graph.getVertices().keySet();
        Iterator it = keys.iterator();
        HashMap<Integer, Vertex> vertices = this.graph.getVertices();
        Vertex temp;
        ArrayList<Vertex> newNeighbors = new ArrayList<>();
        int combinedCost = 0;
        while(it.hasNext()){
            temp = vertices.get(it.next());
            if(temp.getNeighbors().size() == 2 && !temp.isTerminal()){
                for(Edge e : temp.getEdges()){
                    combinedCost += e.getCost().get();
                    newNeighbors.add(e.getOtherSide(temp));
                }
                this.graph.removeVertex(temp);
                this.graph.addEdge(newNeighbors.get(0), newNeighbors.get(1), combinedCost);
                newNeighbors.clear();
            }
        }
    }
    
    public void removeUnnecessaryVertices(){
        Set keys = this.graph.getVertices().keySet();
        Iterator it = keys.iterator();
        int key;
        HashMap<Integer, Vertex> vertices = this.graph.getVertices();
        Vertex current, newCurrent;
        while(it.hasNext()) {
            key = (Integer)it.next();
            current = vertices.get(key);
            while(!current.isTerminal() && current.getNeighbors().size() == 1){
                newCurrent = (Vertex) current.getNeighbors().toArray()[0];
                it.remove();
                this.graph.removeVertex(current);
                current = newCurrent;
            }
        }
    }
    
    //The methods below are for testing and requesting certain information from the graph
    public int[] countDegree(){
        Set keyset = this.graph.getVertices().keySet();
        HashMap<Integer, Vertex> vertices = this.graph.getVertices();
        Iterator it = keyset.iterator();
        int[] degrees = {0,0, 0,0, 0,0, 0,0, 0,0};
        Vertex temp;
        int key, number;
        while(it.hasNext()){
            key = (Integer) it.next();
            temp = vertices.get(key);
            number = temp.getNeighbors().size();
            if(number >= degrees.length){
                continue;
            }
            degrees[number]++;
        }
        return degrees;
    }
}
