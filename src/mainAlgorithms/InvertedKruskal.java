package mainAlgorithms;

import graph.Edge;
import graph.UndirectedGraph;
import graph.Vertex;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Marciano
 */
public class InvertedKruskal implements SteinerTreeSolver{
    
    private UndirectedGraph g;
    private ArrayList<Edge> sorted;
    private HashSet<Integer> connected;
    private HashSet<Integer> connectedTerminals;
    private static final boolean DEBUG = true;
    
    public InvertedKruskal(UndirectedGraph g){
        this.g = g;
        this.sorted = new ArrayList<>();
        this.connected = new HashSet<>();
        this.connectedTerminals = new HashSet<>();
    }
    
    @Override
    public List<Edge> solve(UndirectedGraph G) {
        this.createSortedEdgeList();
        this.removeEdges();
        return new ArrayList<>(this.g.getEdges());
    }
    
    public void removeEdges(){
        for(int i = 0; i < this.sorted.size(); i++){
            if(this.canBeRemoved(this.sorted.get(i))){
                this.g.removeEdge(this.sorted.get(i));
            }
            this.connected.removeAll(this.connected);
            this.connectedTerminals.removeAll(this.connectedTerminals);
        }
    }
    
    public boolean canBeRemoved(Edge e){
        Vertex current = e.getVertices()[0];
        Vertex neighbour = e.getOtherSide(current);
        if(!current.isTerminal() && neighbour.isTerminal()){
            Vertex temp = neighbour;
            neighbour = current;
            current = temp;
        }
        if(current.isTerminal()){
            this.connectedTerminals.add(current.getKey());
        }
        this.connected.add(current.getKey());
        
        Stack<Vertex> toBeExplored = new Stack<>();
        for(Vertex v : current.getNeighbors()){
            if(v.equals(neighbour)){
               continue; 
            } else {
                toBeExplored.add(v);
            }
        }
        while(!toBeExplored.isEmpty()){
            current = toBeExplored.pop();
            if(current.isTerminal()){
                this.connectedTerminals.add(current.getKey());
            }
            this.connected.add(current.getKey());
            for(Vertex n : current.getNeighbors()){
                if(this.connected.contains(n.getKey())){
                    continue;
                } else {
                    toBeExplored.push(n);
                }
            }
        }
        return this.connectedTerminals.containsAll(this.g.getTerminals().keySet());
    }
    
    public void createSortedEdgeList(){
        this.sorted.addAll(this.g.getEdges());
//        this.sorted = this.insertionSort(this.sorted);
        this.sorted = this.mergeSort(this.sorted);
        if(DEBUG){
            System.out.println("Sorted size: " + this.sorted.size());
            for(int i = 0; i < this.sorted.size(); i++){
                System.out.print(this.sorted.get(i).getCost().get() + " ");
            }
            System.out.println("");
        }
    }
    
    public ArrayList<Edge> insertionSort(ArrayList<Edge> toBeSorted){
        Edge temp;
        for(int i = 0; i < toBeSorted.size(); i++){
            for(int j = 0; j < i; j++){
                if(toBeSorted.get(i).getCost().get() < toBeSorted.get(j).getCost().get()){
                    continue;
                } else {
                    temp = toBeSorted.get(i);
                    toBeSorted.remove(i);
                    toBeSorted.add(j, temp);
                }
            }
        }
        return sorted;
    }
    
    public ArrayList<Edge> merge(ArrayList<Edge> leftSide, ArrayList<Edge> rightSide){
        ArrayList<Edge> sorted = new ArrayList<>();
        int counterLeft = 0, counterRight = 0;
        for(int i = 0; i < leftSide.size()+rightSide.size(); i++){
            if(counterLeft == leftSide.size()){
                for(int j = counterRight; j < rightSide.size(); j++){
                    sorted.add(rightSide.get(j));
                }
                break;
            } else if(counterRight == rightSide.size()){
                for(int j = counterLeft; j < leftSide.size(); j++){
                    sorted.add(leftSide.get(j));
                }
                break;
            } else if(leftSide.get(counterLeft).getCost().get() > rightSide.get(counterRight).getCost().get()){
                sorted.add(leftSide.get(counterLeft));
                counterLeft++;
            } else {
                sorted.add(rightSide.get(counterRight));
                counterRight++;
            }
        }
        return sorted;
    }
    
    public ArrayList<Edge> mergeSort(ArrayList<Edge> edges){
        if(edges.size() == 1){
            return edges;
        }
        ArrayList<Edge> leftSide = new ArrayList<>(), rightSide = new ArrayList<>();
        for(int i = 0; i < edges.size(); i++){
            if(i < edges.size()/2){
                leftSide.add(edges.get(i));
            } else {
                rightSide.add(edges.get(i));
            }
        }
        leftSide = mergeSort(leftSide);
        rightSide = mergeSort(rightSide);
        return merge(leftSide, rightSide);
    }
}
