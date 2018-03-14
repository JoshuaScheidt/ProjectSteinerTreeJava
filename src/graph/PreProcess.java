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
import java.util.Stack;

/**
 *
 * @author Marciano
 */
public class PreProcess {
    
    UndirectedGraph graph;
    
    public PreProcess(UndirectedGraph g){
        this.graph = g.clone();
    }
    
    public void removeNonTerminalDegreeTwo(){
        Set keys = this.graph.getVertices().keySet();
        Iterator it = keys.iterator();
        HashMap<Integer, Vertex> vertices = this.graph.getVertices();
        Vertex current, firstVertex, secondVertex, tempVertex;
        Edge firstEdge, secondEdge, tempEdge = null;
        Stack<double[]> subsumed;
        ArrayList<Integer> toBeRemoved = new ArrayList<>();
        int cost = 0;
        while(it.hasNext()){
            current = vertices.get((int)it.next());
            if(!current.isTerminal() && current.getNeighbors().size() == 2){
                System.out.println("Enters Loop");
                subsumed = new Stack<>();
                firstVertex = (Vertex) current.getNeighbors().toArray()[0];
                secondVertex = current.getOtherNeighborVertex(firstVertex);
                firstEdge = current.getConnectingEdge(firstVertex);
                secondEdge = current.getConnectingEdge(secondVertex);
                subsumed.push(new double[]{current.getKey(), firstVertex.getKey(), firstEdge.getCost().get()});
                subsumed.push(new double[]{current.getKey(), secondVertex.getKey(), secondEdge.getCost().get()});
                cost += firstEdge.getCost().get() + secondEdge.getCost().get();
                toBeRemoved.add(current.getKey());
                while(!firstVertex.isTerminal() && firstVertex.getNeighbors().size() == 2){
                    tempEdge = firstVertex.getOtherEdge(firstEdge);
                    tempVertex = tempEdge.getOtherSide(firstVertex);
                    subsumed.push(new double[]{firstVertex.getKey(), tempVertex.getKey(), tempEdge.getCost().get()});
                    toBeRemoved.add(firstVertex.getKey());
                    cost += tempEdge.getCost().get();
                    firstVertex = tempVertex;
                    firstEdge = tempEdge;
                }
                while(!secondVertex.isTerminal() && secondVertex.getNeighbors().size() == 2){
                    tempEdge = secondVertex.getOtherEdge(secondEdge);
                    tempVertex = tempEdge.getOtherSide(secondVertex);
                    subsumed.push(new double[]{secondVertex.getKey(), tempVertex.getKey(), tempEdge.getCost().get()});
                    toBeRemoved.add(secondVertex.getKey());
                    cost += tempEdge.getCost().get();
                    secondVertex = tempVertex;
                    secondEdge = tempEdge;
                }
                //YOU NEED TO STILL ADD THAT THE STACK OF SUBSUMED EDGES AND VERTICES ARE IN THE NEW EDGE, ALSO FIX CONCURRENCY ERROR!!!
                this.graph.addEdge(firstVertex, secondVertex, cost);
                for(int key : toBeRemoved){
                    System.out.println(key);
                    this.graph.removeVertex(key);
                }
                it.remove();
            }
        }
    }
    /**
     * Removes Non-Terminal leaf nodes entirely as they will never be chosen (WE ASSUME THERE WILL BE NO NON-NEGATIVE EDGES)
     * Removes Terminal leaf nodes and sets its neighbor to be a terminal to ensure connection
     */
    public void removeLeafNodes(){
        Set keys = this.graph.getVertices().keySet();
        Iterator it = keys.iterator();
        HashMap<Integer, Vertex> vertices = this.graph.getVertices();
        Vertex current, newCurrent;
        while(it.hasNext()) {
            current = vertices.get((int)it.next());
            while(!current.isTerminal() && current.getNeighbors().size() == 1){
                newCurrent = (Vertex) current.getNeighbors().toArray()[0];
                
                it.remove();
                this.graph.removeVertex(current);
                current = newCurrent;
            }
            while(current.isTerminal() && current.getNeighbors().size() == 1){
                newCurrent = (Vertex) current.getNeighbors().toArray()[0];
                newCurrent.pushSubsumed(new double[]{newCurrent.getKey(), current.getKey(), ((Edge)(current.getEdges().toArray()[0])).getCost().get()});
                this.graph.setTerminal(newCurrent.getKey());
                
                it.remove();
                this.graph.removeVertex(current);
                current = newCurrent;
            }
       }
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
    	this.iteratedValues = new int[this.graph.getVertices().size()];
    	this.lowestFoundLabels = new int[this.graph.getVertices().size()];
    	this.bridges = new ArrayList<>();
    	
    	this.preorderTraversal(this.graph.getVertices().get(1), ((Vertex)this.graph.getVertices().get(1).getNeighbors().toArray()[0]));
    	return this.bridges;
    }
}
