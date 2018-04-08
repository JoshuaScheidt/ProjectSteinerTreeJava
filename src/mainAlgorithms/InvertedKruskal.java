package mainAlgorithms;

import graph.Edge;
import graph.UndirectedGraph;
import java.util.ArrayList;
import java.util.HashMap;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Marciano
 */
public class InvertedKruskal {
    
    private UndirectedGraph g;
    private ArrayList<Edge> sorted;
    private HashMap<Integer, Boolean> connected;
    
    public InvertedKruskal(UndirectedGraph g){
        this.g = g;
        this.sorted = new ArrayList<>();
        this.connected = new HashMap<>();
    }
    
    public void start(){
        this.createSortedEdgeList();
    }
    
    public void createSortedEdgeList(){
        this.sorted.addAll(this.g.getEdges());
        
        System.out.println(this.sorted.size());
        
//        this.sorted = this.mergeSort(sorted);
        this.sorted = this.insertionSort(sorted);
        
        System.out.println(this.sorted.size());
        
        System.out.print("[");
        for(int i = 0; i < this.sorted.size(); i++){
            System.out.print(this.sorted.get(i).getCost().get() + ", ");
        }
        System.out.println("]");
    }
    
    public ArrayList<Edge> insertionSort(ArrayList<Edge> x){
        ArrayList<Edge> sorted = new ArrayList<>();
        sorted.add(x.get(0));
        for(int i = 1; i < x.size(); i++){
            for(int j = 0; j < sorted.size(); j++){
                if(x.get(i).getCost().get() < sorted.get(j).getCost().get()){
                    if(j == sorted.size() - 1){
                        sorted.add(x.get(i));
                        break;
                    }
                } else {
                    sorted.add(j, x.get(i));
                    break;
                }
            }   
        }
        return sorted;
    }
    public ArrayList<Edge> mergeSort(ArrayList<Edge> x){
        if(x.size() == 1){
            return x;
        } else {
            ArrayList<Edge> a = new ArrayList<>(), b = new ArrayList<>();
            for(int i = 0; i < x.size(); i++){
                if(i < x.size()/2){
                    a.add(x.get(i));
                } else {
                    b.add(x.get(i));
                }
            }
            a = mergeSort(a);
            b = mergeSort(b);
            ArrayList<Edge> sorted = new ArrayList<>();
            outerloop: for(int i = 0; i < a.size(); i++){
                for(int j = 0; j < b.size(); j++){
                    if(a.get(i).getCost().get() > b.get(j).getCost().get()){
                        sorted.add(a.get(i));
                        if(i == a.size()-1){
                            for(int k = j; k < b.size(); k++){
                                sorted.add(b.get(k));
                            }
                        }
                        break;
                    } else {
                        sorted.add(b.get(j));
                        if(j == b.size()-1){
                            for(int k = i; k < a.size(); k++){
                                sorted.add(a.get(k));
                            }
                            break outerloop;
                        }
                    }
                }
            }
            return sorted;
        }
    }
}
