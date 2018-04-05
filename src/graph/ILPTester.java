/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graph;

import java.io.File;
import mixedIntegerProgramming.ILPProgram;

/**
 *
 * @author Marciano
 */
public class ILPTester {
    
    public static void main(String[] args){
        File file = new File("data\\test\\testILP.gr");
        UndirectedGraph graph = new UndirectedGraphReader().read(file);
        ILPProgram fp = new ILPProgram(graph);
        fp.initiateCutSearch();
    }
}
