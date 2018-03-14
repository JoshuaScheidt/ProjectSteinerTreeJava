/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import java.util.Stack;
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
    
    public void removeTerminalDegreeTwo(){
        Set keys = this.graph.getVertices().keySet();
        Iterator it = keys.iterator();
        int key;
        HashMap<Integer, Vertex> vertices = this.graph.getVertices();
        Vertex current, newCurrent;
        while(it.hasNext()){
            key = (int) it.next();
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
    		}
			else if(next != parent) {
				this.lowestFoundLabels[v.getKey()-1] = Math.min(this.lowestFoundLabels[v.getKey()-1], this.lowestFoundLabels[next.getKey()-1]);
			}
    	}
    }
    
    
//WORKING DEPTH-FIRST SEARCH
//	Stack<Vertex> stack = new Stack<>();
//	Vertex next;
//	Iterator<Vertex> it;
//	stack.push(v);
//	this.iteratedValues[v.getKey()-1] = this.count;
//	this.count++;
//	
//	while(!stack.isEmpty()) {
//		it = stack.peek().getNeighbors().iterator();
//		while((next=it.next()) != null) {
//			if(this.iteratedValues[next.getKey()-1] == 0) {
//            	this.iteratedValues[next.getKey()-1] = this.count;
//            	this.count++;
//            	stack.push(next);
//            	break;
//			}
//			else if(!it.hasNext()) {
//        		stack.pop();
//        		break;
//        	}
//		}
//	}
//	System.out.println(Arrays.toString(this.iteratedValues));   
    
    /**
     * Completes the preprocess step for Tarjan's bridge finding algorithm.
     * This method does NOT use recursions to decrease heap size.
     *
     * @param v The starting vertex
     *
     * @author Joshua Scheidt
     */
    private void preorderTraversalNoRec(Vertex v) {
    	Stack<Vertex> stack = new Stack<>();
    	Vertex next, latest;
    	Iterator<Vertex> it;
    	stack.push(v);
    	this.iteratedValues[v.getKey()-1] = this.count;
    	this.count++;
    	this.lowestFoundLabels[v.getKey()-1] = iteratedValues[v.getKey()-1];
    	
    	while(!stack.isEmpty()) {
    		it = stack.peek().getNeighbors().iterator();
    		while((next=it.next()) != null) {
    			if(this.iteratedValues[next.getKey()-1] == 0) {
                	this.iteratedValues[next.getKey()-1] = this.count;
                	this.count++;
                	this.lowestFoundLabels[v.getKey()-1] = iteratedValues[v.getKey()-1];
                	stack.push(next);
                	break;
    			}
    			else if(!it.hasNext()) {
            		stack.pop();
        			this.lowestFoundLabels[stack.peek().getKey()-1] = Math.min(this.lowestFoundLabels[stack.peek().getKey()-1], this.lowestFoundLabels[next.getKey()-1]);
        			if(this.lowestFoundLabels[next.getKey()-1] == this.iteratedValues[next.getKey()-1])
    					try {
    						this.bridges.add(this.graph.edgeBetweenVertices(v, next));
    					} catch (GraphException e) {
    						e.printStackTrace();
    					}
        			
        			break;
            	}
    			else if(next != stack.get(stack.indexOf(stack.peek())-1)) {
    				this.lowestFoundLabels[stack.peek().getKey()-1] = Math.min(this.lowestFoundLabels[stack.peek().getKey()-1], this.lowestFoundLabels[next.getKey()-1]);
    			}
    		}
    	}
    	System.out.println(Arrays.toString(this.iteratedValues));    	
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
    	
//    	this.preorderTraversal(this.graph.getVertices().get(1), ((Vertex)this.graph.getVertices().get(1).getNeighbors().toArray()[0]));
    	this.preorderTraversalNoRec(this.graph.getVertices().get(1));
    	return this.bridges;
    }
}
