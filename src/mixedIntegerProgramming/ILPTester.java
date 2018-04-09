/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mixedIntegerProgramming;

import graph.Edge;
import graph.UndirectedGraph;
import graph.UndirectedGraphReader;
import java.io.File;
import java.util.ArrayList;
import mainAlgorithms.InvertedKruskal;

/**
 *
 * @author Marciano
 */
public class ILPTester {
    
    public static void main(String[] args){
        String fileName = "data\\heuristics\\instance197.gr";
        File file = new File(fileName);
        
        UndirectedGraph graph = new UndirectedGraphReader().read(file);
        long starts = System.currentTimeMillis();
        
        InvertedKruskal ik = new InvertedKruskal(graph);
        ArrayList<Edge> solution = (ArrayList) ik.solve(graph);
        printSolution(solution);

//        fileName = fileName.substring(fileName.indexOf("\\") + 1);
//        fileName = fileName.substring(fileName.indexOf("\\") + 1);
//        fileName = fileName.substring(0, fileName.indexOf("."));
//        CutILP fp = new CutILP(graph, fileName);
//        fp.initiateCutSearch();
        
        System.out.println("Took " + (System.currentTimeMillis() - starts) + " ms");

    }
    
    private static void printSolution(ArrayList<Edge> solution){
        String temp = "";
        int sum = 0;
        for(int i = 0; i < solution.size(); i++){
            temp = temp.concat(solution.get(i).getVertices()[0].getKey() + " " + solution.get(i).getVertices()[1].getKey() + "\n");
            sum += solution.get(i).getCost().get();
        }
        System.out.println("VALUE " + sum);
        System.out.println(temp);
    }
}
