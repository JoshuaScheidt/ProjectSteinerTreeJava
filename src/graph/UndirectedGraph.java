/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import interfaces.Connection;
import interfaces.Graph;
import interfaces.Node;
import java.util.Iterator;
import java.util.Set;

/**
 *
 * @author Marciano
 */
public class UndirectedGraph implements Graph{

    private HashMap<Integer, Node> vertices = new HashMap<>();
    private HashSet<Connection> edges = new HashSet<>();
    private HashMap<Integer, Node> terminals = new HashMap<>();
    private int numberOfTerminals = 0;
    
    public UndirectedGraph(){}
    
    public UndirectedGraph(ArrayList<Vertex> V, ArrayList<Edge> E){
        V.forEach((v) -> {
            this.addNode(v);
        });
        ArrayList<Node> n;
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
    @Override
    public void addNode(Node N) {
        if(!(N instanceof Vertex) || this.vertices.containsValue((Vertex) N)){
            return;
        }
        this.vertices.put(((Vertex)N).getKey(), N);
    }
    
    @Override
    public void addEdge(Node N1, Node N2, int cost) {
    	if(this.vertices.containsValue(N1) || this.vertices.containsValue(N2)) {
    		if(this.vertices.containsValue(N1) && this.vertices.containsValue(N2)) {
    			Edge e = new Edge((Vertex)N1, (Vertex)N2, cost);
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

    public void setTerminals(){
        Set keys = this.vertices.keySet();
        Iterator it = keys.iterator();
        int key;
        while(it.hasNext()) {
            key = (Integer)it.next();
            if(((Vertex)this.vertices.get(key)).isTerminal()){
                this.terminals.put(key, this.vertices.get(key));
                this.numberOfTerminals++;
            }
        }
    }
    
    public void removeUnnecessaryVertices(){
        Set keys = this.vertices.keySet();
        Iterator it = keys.iterator();
        int key;
        while(it.hasNext()) {
            key = (Integer)it.next();
            if(this.vertices.get(key).getNeighbors().size() <= 1 && !((Vertex)this.vertices.get(key)).isTerminal()){
                this.vertices.remove(key);
            }
        }
    }
    @Override
    public void removeNode(Node N) {
        this.vertices.remove(((Vertex) N).getKey());
    }

    @Override
    public boolean containsNode(Node N) {
        return this.vertices.containsValue(N);
    }

    @Override
    public boolean containsEdge(Connection E) {
        return this.edges.contains(E);
    }

    @Override
    public HashMap<Integer, Node> getNode() {
        return this.vertices;
    }

    @Override
    public HashSet<Connection> getEdges() {
        return this.edges;
    }

    public int getNumberOfTerminals(){
        return this.numberOfTerminals;
    }
    @Override
    public int getEdgesSize() {
	return this.edges.size();
    }

    @Override
    public int getNodesSize() {
	return this.vertices.size();
    }
    
}
