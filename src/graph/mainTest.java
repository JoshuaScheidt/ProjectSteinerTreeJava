package graph;

import java.io.File;

public class mainTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//UndirectedGraph graph = new UndirectedGraphReader().read(new File("E:\\Programming\\Java\\GeneralPurposeCodeJava\\test\\structures\\undirectedGraph\\testFiles\\instance199.gr"));
		UndirectedGraph graph = new UndirectedGraphReader().read(new File("data\\heuristics\\instance197.gr"));
                PreProcess improved = new PreProcess(graph);
                
                System.out.println("Original Degree Scale: ");
                int[] degrees = graph.countDegree();
                for(int i = 0; i < degrees.length; i++){
                    System.out.print(degrees[i] + ", ");
                }
                System.out.println("");
                
                System.out.println("Cloned Degree Scale: ");
                int[] clonedDegrees = improved.countDegree();
                for(int i = 0; i < clonedDegrees.length; i++){
                    System.out.print(clonedDegrees[i] + ", ");
                }
                System.out.println("");
                
                System.out.println("Original number of Vertices: " + improved.graph.getVerticesSize() + " Current number of Edges: " + improved.graph.getEdgesSize());
                
                improved.removeUnnecessaryVertices();
                improved.reduceSize();
                
                System.out.println("Current number of Vertices: " + improved.graph.getVerticesSize() + " Current number of Edges: " + improved.graph.getEdgesSize());
        }

}
