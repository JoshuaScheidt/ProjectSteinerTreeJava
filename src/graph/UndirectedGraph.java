/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 *
 * @author Marciano
 */
public class UndirectedGraph{

    private HashMap<Integer, Vertex> vertices = new HashMap<>();
    private HashSet<Edge> edges = new HashSet<>();
    private HashMap<Integer, Vertex> terminals = new HashMap<>();
    private int numberOfTerminals = 0;
    
    public UndirectedGraph(){}
    
    public UndirectedGraph(ArrayList<Vertex> V, ArrayList<Edge> E){
        V.forEach((v) -> {
            this.addVertex(v);
        });
        ArrayList<Vertex> n;
        for(Edge e : E){
            n = e.getNodes();
            if(this.vertices.containsKey(((Vertex) n.get(0)).getKey()) && this.vertices.containsKey(((Vertex) n.get(1)).getKey())){
                this.edges.add(e);
            } else if(this.vertices.containsKey(((Vertex) n.get(0)).getKey())){
                this.vertices.put(((Vertex) n.get(1)).getKey(), n.get(1));
                this.edges.add(e);
            } else {
                this.vertices.put(((Vertex) n.get(0)).getKey(), n.get(0));
                this.edges.add(e);
            }
        }
    }
    
    public void addVertex(Vertex N) {
        if(!(N instanceof Vertex) || this.vertices.containsValue((Vertex) N)){
            return;
        }
        this.vertices.put(((Vertex)N).getKey(), N);
    }
    
    
    public void addEdge(Vertex N1, Vertex N2, int cost) {
    	if(this.vertices.containsValue(N1) || this.vertices.containsValue(N2)) {
    		if(this.vertices.containsValue(N1) && this.vertices.containsValue(N2)) {
    			Edge e = new Edge(N1, N2, cost);
    			this.edges.add(e);
    		}
    		else if(this.vertices.containsValue(N1)) {
    			Edge e = new Edge((Vertex)N1, (Vertex)N2, cost);
    			this.edges.add(e);
    			this.vertices.put(((Vertex)N2).getKey(), (Vertex)N2);
    		}
    		else if(this.vertices.containsValue(N2)) {
    			Edge e = new Edge((Vertex)N1, (Vertex)N2, cost);
    			this.edges.add(e);
    			this.vertices.put(((Vertex)N1).getKey(), (Vertex)N1);
    		}
    	}
    	else if(N1 instanceof Vertex && N2 instanceof Vertex) {
			this.vertices.put(((Vertex)N1).getKey(), (Vertex)N1);
			this.vertices.put(((Vertex)N2).getKey(), (Vertex)N2);
			Edge e = new Edge((Vertex)N1, (Vertex)N2, cost);
			this.edges.add(e);
        } else {
        	System.out.println("ERROR");
        }
        
    }

    public void setTerminals(int[] keys){
        for(int i = 0; i < keys.length; i++){
            this.vertices.get(keys[i]).setTerminal(true);
            this.terminals.put(keys[i], this.vertices.get(keys[i]));
        }
    }
    
    public UndirectedGraph clone(){
        UndirectedGraph graph = new UndirectedGraph();
        for(Edge e : this.edges) {
            graph.addEdge(new Vertex(((Vertex)(e.getNodes().get(0))).getKey()), new Vertex(((Vertex)(e.getNodes().get(1))).getKey()), e.getCost().get());
        }
        for(int key : this.terminals.keySet()) {
            ((Vertex)graph.vertices.get(key)).setTerminal(true);
        }
        return graph;
    }
    
    public void removeVertex(Vertex v) {
        for(Edge e : v.getEdges()){
            Vertex neighbor = e.getOtherSide(v);
            neighbor.getEdges().remove(e);
            this.edges.remove(e);
            e = null;
        }
        int key = v.getKey();
        if(v.isTerminal()){
            this.numberOfTerminals--;
            this.terminals.remove(key);
        }
        this.vertices.remove(key);
        v = null;
    }
    
    public boolean containsVertex(Vertex v) {
        return this.vertices.containsValue(v);
    }
    
    public boolean containsEdge(Edge e) {
        return this.edges.contains(e);
    }
    
    public HashMap<Integer, Vertex> getVertices() {
        return this.vertices;
    }
    
    public HashSet<Edge> getEdges() {
        return this.edges;
    }

    public int getNumberOfTerminals(){
        return this.numberOfTerminals;
    }
    
    public int getEdgesSize() {
	return this.edges.size();
    }
    
    public int getVerticesSize() {
	return this.vertices.size();
    }
    
}
