/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mixedIntegerProgramming;

import graph.UndirectedGraph;
import graph.UndirectedGraphReader;
import java.io.File;

/**
 *
 * @author Marciano
 */
public class ILPTester {
    
    public static void main(String[] args){
        String fileName = "data\\heuristics\\instance039.gr";
        File file = new File(fileName);
        fileName = fileName.substring(fileName.indexOf("\\") + 1);
        fileName = fileName.substring(fileName.indexOf("\\") + 1);
        fileName = fileName.substring(0, fileName.indexOf("."));
        UndirectedGraph graph = new UndirectedGraphReader().read(file);
        ILPProgram fp = new ILPProgram(graph, fileName);
        fp.initiateCutSearch();
    }
}
