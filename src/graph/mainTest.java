package graph;

import java.io.File;
import java.util.ArrayList;

public class mainTest {

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        UndirectedGraph graph = new UndirectedGraphReader().read(new File("data\\heuristics\\instance197.gr"));
        PreProcess improved = new PreProcess(graph);

        printCurrentSize(improved);
        System.out.println("Original Degree Scale: ");
        int[] degrees = graph.countDegree();
        for (int i = 0; i < degrees.length; i++) {
            System.out.print(degrees[i] + ", ");
        }
        System.out.println("");

        //Bridge Finding
//		long start = System.currentTimeMillis();
//		ArrayList<Edge> bridges = improved.tarjanBridgeFinding();
//		System.out.println("Time needed: " + (System.currentTimeMillis() - start) + " ms");
//		System.out.println("Found " + bridges.size() + " bridges.");
//		for (Edge e : bridges)
//			System.out.println("Bridge found on vertices: " + e.getVertices()[0].getKey() + " and  " + e.getVertices()[1].getKey());
        // System.out.println("done");
        //Leaf Node Removal
        improved.removeLeafNodes();

        //Non-Terminal Degree Two removal
        long start = System.nanoTime();
        improved.removeNonTerminalDegreeTwo();
        long stop = System.nanoTime();
        System.out.println("Time Taken: " + (stop - start) / 1000000000.0);

        printCurrentSize(improved);
        printDegreeScale(improved);
        
        improved.removeLeafNodes();

        printCurrentSize(improved);
        printDegreeScale(improved);
        
        improved.removeNonTerminalDegreeTwo();
        
    }

    public static void printCurrentSize(PreProcess improved) {
        System.out.println(
                "Current number of (Vertices, Terminals): ("
                + improved.graph.getVertices().size() + ", " + improved.graph.getNumberOfTerminals()
                + ") Current number of Edges: " + improved.graph.getEdges().size());
    }

    public static void printDegreeScale(PreProcess improved) {
        System.out.println("Current Degree Scale: ");
        int[] clonedDegrees = improved.graph.countDegree();
        for (int i = 0; i < clonedDegrees.length; i++) {
            System.out.print(clonedDegrees[i] + ", ");
        }
        System.out.println("");
    }

}
