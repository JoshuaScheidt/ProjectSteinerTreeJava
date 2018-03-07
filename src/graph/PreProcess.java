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
    //Doesn't work yet!!!
    public void reduceSize(){
        Set keys = this.graph.getVertices().keySet();
        Iterator it = keys.iterator();
        HashMap<Integer, Vertex> vertices = this.graph.getVertices();
        Vertex temp, n1, n2, prevn1, prevn2;
        ArrayList<Vertex> newNeighbors = new ArrayList<>();
        int combinedCost = 0;
        while(it.hasNext()){
            temp = vertices.get((Integer)it.next());
            if(temp.getNeighbors().size() == 2 && !temp.isTerminal()){
                n1 = ((Vertex) (temp.getNeighbors().toArray()[0]));
                n2 = ((Vertex) (temp.getNeighbors().toArray()[1]));
                while((n1.getEdges().size() == 2 && !n1.isTerminal())){
                    prevn1 = n1;
                    
                }
                while((n2.getEdges().size() == 2 && !n2.isTerminal())){
                    
                }
                for(Edge e : temp.getEdges()){
                    combinedCost += e.getCost().get();
                    newNeighbors.add(e.getOtherSide(temp));
                }
                it.remove();
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
            while(current.isTerminal() && current.getNeighbors().size() == 1){
                newCurrent = (Vertex) current.getNeighbors().toArray()[0];
                newCurrent.setTerminal(true);
                newCurrent.pushStack(current.getSubsumed());
                newCurrent.pushSubsumed(new int[]{newCurrent.getKey(), current.getKey(), ((Edge) current.getEdges().toArray()[0]).getCost().get()});
                it.remove();
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
    
    
    /** The current count for the pre-order traversal */
    private int count;
    /** The array containing the count per Vertex for the pre-order traversal */
    private int[] iteratedValues;
    /** The lowest found count per Vertex */
    private int[] lowestFoundLabels;
    /** The found bridges */
    private ArrayList<Edge> bridges;
    
    /**
     * Completes the preprocess step for Tarjan's bridge finding algorithm.
     *
     * @param v The current Vertex
     * @param parent The parent from the current Vertex (current from previous iteration)
     *
     * @author Joshua Scheidt
     */
    private void preorderTraversal(Vertex v, Vertex parent) {
    	this.iteratedValues[v.getKey()-1] = this.count;
    	this.count++;
    	this.lowestFoundLabels[v.getKey()-1] = iteratedValues[v.getKey()-1];
    	
    	for(Vertex next : v.getNeighbors()) {
    		if(this.iteratedValues[next.getKey()-1] == 0) {
    			this.preorderTraversal(next, v);
    			
    			this.lowestFoundLabels[v.getKey()-1] = Math.min(this.lowestFoundLabels[v.getKey()-1], this.lowestFoundLabels[next.getKey()-1]);
    			if(this.lowestFoundLabels[next.getKey()-1] == this.iteratedValues[next.getKey()-1])
					try {
						this.bridges.add(this.graph.edgeBetweenVertices(v, next));
					} catch (GraphException e) {
						e.printStackTrace();
					}
    			else if(next != parent) {
    				this.lowestFoundLabels[v.getKey()-1] = Math.min(this.lowestFoundLabels[v.getKey()-1], this.lowestFoundLabels[next.getKey()-1]);
    			}
    		}
    	}
    }
    
    /**
     * Performs Tarjan's bridge finding algorithm.
     *
     * @return All the bridges in the graph.
     *
     * @author Joshua Scheidt
     */
    public ArrayList<Edge> tarjanBridgeFinding() {
    	this.count = 1;
    	this.iteratedValues = new int[this.graph.getVerticesSize()];
    	this.lowestFoundLabels = new int[this.graph.getVerticesSize()];
    	this.bridges = new ArrayList<>();
    	
    	this.preorderTraversal(this.graph.getVertices().get(1), ((Vertex)this.graph.getVertices().get(1).getNeighbors().toArray()[0]));
    	return this.bridges;
    }
}
