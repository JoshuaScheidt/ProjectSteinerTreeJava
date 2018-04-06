/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mixedIntegerProgramming;

import graph.Edge;
import graph.UndirectedGraph;
import graph.Vertex;
import ilog.concert.*;
import ilog.cplex.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 *
 * @author Marciano
 */
public class ILPProgram {

    private UndirectedGraph g;
    private HashMap<Integer, Edge> edges; 
    private HashSet<ArrayList<Integer>> result;
    private final boolean DEBUG = false;
    
    public ILPProgram(UndirectedGraph g){
        this.g = g;
    }
    
    public void initiateCutSearch(){
        this.createIdentifiableEdges();
        Set keys = this.g.getTerminals().keySet();
        Iterator it = keys.iterator();
        int temp;
        ArrayList sub1, sub2;
        this.result = new HashSet<>();
        while(it.hasNext()){
            temp = (int)it.next();
            sub1 = new ArrayList<>();
            sub1.add(temp);
            sub2 = new ArrayList<>();
            sub2.addAll(keys);
            sub2.remove(temp);
            this.recursiveCut(sub1, sub2, 1);
        }
        System.out.println(this.result.size());
    }
    
    public void recursiveCut(ArrayList<Integer> sub1, ArrayList<Integer> sub2, int term1){
        if(term1 >= this.g.getNumberOfTerminals()){
            return;
        } else {
            this.result.add(sub1);
            int temp;
            ArrayList<Integer> newSub1, newSub2;
            for(int i = 0; i < sub2.size(); i++){
                temp = sub2.get(i);
                newSub1 = new ArrayList<>();
                newSub1.add(temp);
                newSub2 = new ArrayList<>();
                newSub2.addAll(sub2);
                newSub2.remove(temp);
                if(this.g.getTerminals().containsKey(temp)){
                    this.recursiveCut(newSub1, newSub2, term1++);
                } else {
                    this.recursiveCut(newSub1, newSub2, term1);
                }
            }
        }
    }
    
    public void createIdentifiableEdges(){
        this.edges = new HashMap<>();
        Iterator it = this.g.getEdges().iterator();
        Edge e;
        int counter = 1;
        while(it.hasNext()){
            e = (Edge)it.next();
            this.edges.put(counter, e);
            counter++;
        }
    }
    
    public int[] activateCPLEX(String txt){
        int[] basic = null;
        try{
            IloCplex cplex = new IloCplex();
        
            cplex.importModel(txt);
            cplex.setOut(null);
            if ( cplex.solve() ) {
                if(DEBUG){
                    cplex.output().println("Solution status = " + cplex.getStatus());
                    cplex.output().println("Solution value  = " + cplex.getObjValue());
                }
                IloLPMatrix lp = (IloLPMatrix)cplex.LPMatrixIterator().next();
                
                IloNumVar[] vars = lp.getNumVars();
                double[]    vals = cplex.getValues(vars);
                int lengthBasic = this.g.getEdges().size();
                if(DEBUG){
                    System.out.println(lengthBasic);
                    System.out.println(Arrays.toString(vars));
                    System.out.println(Arrays.toString(vals));
                }
                basic = new int[lengthBasic];
                if(DEBUG){
                    System.out.println(Arrays.toString(vars));
                    System.out.println(Arrays.toString(vals));
                }
                int cnt = 0;
                for(int i = 0; i < vars.length; i++){
                    if(vals[i] == 1 && i < g.getEdges().size()){
                       basic[cnt] = i;
                       cnt++;
                    }
                }
         }
         cplex.end();
        } 
        catch(IloException e) {
            System.err.println("Concert exception '" + e + "'caught");
        }
        if(DEBUG){
            System.out.println(Arrays.toString(basic));
        }
        return basic;
    }
    
    public String flowFormulation(){
        ArrayList<String> lines = new ArrayList<>();
        lines.add("Minimize");
        String temp = "obj: ";
        String variables = "";
        Iterator<Edge> it = this.g.getEdges().iterator();
        Edge e;
        int cnt = 0;
        while(it.hasNext()){
            e = (Edge) it.next();
            if(cnt == 0){
                temp = temp.concat(e.getCost() + "x_" + ( (e.getVertices()[0])).getKey() + "" + ((e.getVertices()[1])).getKey());
                variables = variables.concat( "x_" + ((e.getVertices()[0])).getKey() + "" + ((e.getVertices()[1])).getKey() + "\n");
            } else {
                temp = temp.concat("+" + e.getCost() + "x_" + ((e.getVertices()[0])).getKey() + "" + ((e.getVertices()[1])).getKey());
                variables = variables.concat( "x_" + ( (e.getVertices()[0])).getKey() + "" + ((e.getVertices()[1])).getKey() + "\n");
            }
        }
        lines.add(temp);
        
        
        return null;
    }
}
