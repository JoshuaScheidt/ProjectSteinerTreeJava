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
import java.util.Iterator;

/**
 *
 * @author Marciano
 */
public class FlowProgram {

    private UndirectedGraph g;
    private final boolean DEBUG = false;
    
    public FlowProgram(UndirectedGraph g){
        this.g = g;
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
