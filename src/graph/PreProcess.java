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
    
    /**
     * This method looks more complicated than what it actually does. It removes all 
     * Non-terminals with degree 2. It iteratively checks its neighbors until it finds
     * a Terminal or a Vertex with degree higher than 2.
     * It has to keep track of subsumed vertices per New Edge.
     * And it has to keep track of all Vertices to be removed and Edges to be created.
     * This cannot happen concurrently as the Iterator doesn't allow it, if we do do this
     * it could cause checks on newly created edges which is unnecessary.
     */
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
            //Gets the current Vertex in the Iterator
            current = vertices.get((int)it.next());
            //Checks if Vertex is Non-Terminal and degree 2
            if(!current.isTerminal() && current.getNeighbors().size() == 2){
                System.out.println("Enters Loop");
                //Creates a stack to be used for all vertices that will be subsumed by the to be created Edge
                subsumed = new Stack<>();
                //Creating first steps left and right of current to iteratively find a terminal or degree greater than 2
                firstVertex = (Vertex) current.getNeighbors().toArray()[0];
                secondVertex = current.getOtherNeighborVertex(firstVertex);
                firstEdge = current.getConnectingEdge(firstVertex);
                secondEdge = current.getConnectingEdge(secondVertex);
                //Pushes the original two removable Edges in the form of their two keys and their respective costs
                subsumed.push(new double[]{current.getKey(), firstVertex.getKey(), firstEdge.getCost().get()});
                subsumed.push(new double[]{current.getKey(), secondVertex.getKey(), secondEdge.getCost().get()});
                //The total cost of the new Edge is the sum of the removed Edges
                cost += firstEdge.getCost().get() + secondEdge.getCost().get();
                //Keeps a list of the Vertices to be removed, Removal method will also remove all connected
                //Edges so no need to store the Edge objects
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
                if(current.getSubsumed().size() > 0){
                    newCurrent.pushStack(current.getSubsumed());
                }
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
