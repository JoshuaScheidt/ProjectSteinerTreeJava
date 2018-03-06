package graph;

import java.io.File;

public class mainTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//UndirectedGraph graph = new UndirectedGraphReader().read(new File("E:\\Programming\\Java\\GeneralPurposeCodeJava\\test\\structures\\undirectedGraph\\testFiles\\instance199.gr"));
		UndirectedGraph graph = new UndirectedGraphReader().read(new File("C:\\Users\\Marciano\\Downloads\\heuristic\\public\\test.gr"));
                System.out.println("Number of Vertices: " + graph.getVerticesSize() + " Number of Edges: " + graph.getEdgesSize());
                PreProcess improved = new PreProcess(graph);
                System.out.println("Current Degree Scale: ");
                int[] degrees = improved.countDegree();
                for(int i = 0; i < degrees.length; i++){
                    System.out.print(degrees[i] + ", ");
                }
                improved.removeUnnecessaryVertices();
                improved.reduceSize();
                
                System.out.println("Number of Vertices: " + improved.graph.getVerticesSize() + " Number of Edges: " + improved.graph.getEdgesSize());
                System.out.println("");
        }

}
