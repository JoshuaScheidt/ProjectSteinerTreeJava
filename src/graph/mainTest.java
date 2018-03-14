package graph;

import java.io.File;
import java.util.ArrayList;

public class mainTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		UndirectedGraph graph = new UndirectedGraphReader().read(new File("data\\heuristics\\testTarjan.gr"));
                PreProcess improved = new PreProcess(graph);
                
                System.out.println("Original Degree Scale: ");
                int[] degrees = graph.countDegree();
                for(int i = 0; i < degrees.length; i++){
                    System.out.print(degrees[i] + ", ");
                }
                System.out.println("");
                
                System.out.println("Original number of (Vertices, Terminals): (" + improved.graph.getVertices().size() + ", " + improved.graph.getNumberOfTerminals() +  ") Original number of Edges: " + improved.graph.getEdges().size());
                
                ArrayList<Edge> bridges = improved.tarjanBridgeFinding();
                for(Edge e : bridges) System.out.println("Bridge found on vertices: "+e.getVertices()[0].getKey() + " and  "+e.getVertices()[1].getKey());
//                System.out.println("done");
//                improved.removeLeafNodes();
//                System.out.println("Leaf nodes removed. Current number of (Vertices, Terminals): (" + improved.graph.getVertices().size() + ", " + improved.graph.getNumberOfTerminals() +  ") Current number of Edges: " + improved.graph.getEdges().size());
                //improved.reduceSize();
                //System.out.println("Non-terminal degree 2 removal. Current number of Vertices: " + improved.graph.getVerticesSize() + " Current number of Edges: " + improved.graph.getEdgesSize());
                
//                System.out.println("Cloned Degree Scale: ");
//                int[] clonedDegrees = improved.graph.countDegree();
//                for(int i = 0; i < clonedDegrees.length; i++){
//                    System.out.print(clonedDegrees[i] + ", ");
//                }
//                System.out.println("");
        }

}
