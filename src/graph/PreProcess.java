/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graph;

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
    
    
    public UndirectedGraph reduceSize(){
        Set keys = this.graph.getVertices().keySet();
        Iterator it = keys.iterator();
        Vertex temp;
        while(it.hasNext()){
            temp = (Vertex) it.next();
            if(temp.getNeighbors().size() == 2){
                    
            }
        }
        return null;
    }
    
    public void removeUnnecessaryVertices(){
        Set keys = this.graph.getVertices().keySet();
        Iterator it = keys.iterator();
        int key;
        HashMap<Integer, Vertex> vertices = this.graph.getVertices();
        while(it.hasNext()) {
            key = (Integer)it.next();
            Vertex current = vertices.get(key);
            while(!current.isTerminal() && current.getNeighbors().size() <= 1){
                Vertex newCurrent = (Vertex) current.getNeighbors().toArray()[0];
                this.graph.removeVertex(current);
                current = newCurrent;
            }
        }
    }
}
