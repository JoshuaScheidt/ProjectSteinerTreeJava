/*
 * All of the given code may be used on free will when referenced to the source.
 * Initial version created at 12:32:34
 */
package graph;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * (Class description)
 *
 * 
 * @author Marciano Geijselaers
 * @author Joshua Scheidt
 */
public class UndirectedGraphReader {
    
	public UndirectedGraph read(File f) {
		int numNodes = -1;
		int numEdges = -1;
		int numTerminals = -1;
		int terminalCounter = 0;
		String section = null;
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader(f));
			String currentLine;
			UndirectedGraph G = new UndirectedGraph();
                        HashSet<Integer> terminals = new HashSet<>();
			while((currentLine = reader.readLine()) != null) {
				if(currentLine.contains("SECTION")) {
					section = currentLine.split(" ")[1];
				}
				else if(section.contains("Graph")) {
					if(currentLine.contains("E ")) {
						String[] lineParts = currentLine.split(" ");
						G.addEdge(Integer.parseInt(lineParts[1]), Integer.parseInt(lineParts[2]), Integer.parseInt(lineParts[3]));
					}
					else if(currentLine.contains("Nodes")) {
						numNodes = Integer.parseInt(currentLine.split(" ")[1]);
					}
					else if(currentLine.contains("Edges")) {
						numEdges = Integer.parseInt(currentLine.split(" ")[1]);
					}
				}
				else if(section.contains("Terminals")) {
					if(currentLine.contains("T ")) {
                                            terminals.add(Integer.parseInt(currentLine.split(" ")[1]));
                                            terminalCounter++;
					}
					if(currentLine.contains("Terminals")) {
						numTerminals = Integer.parseInt(currentLine.split(" ")[1]);
					}
				}
				else if(currentLine.contains("END")) {
					section = null;
				}
			}
                        G.setTerminals(terminals);
			if(G.getEdges().size() != numEdges) {
				System.out.println("Number of edges wrong");
				System.out.println("Number of read edges:" + numEdges);
				System.out.println("Number of actual edges:" + G.getEdges().size());
			}
			if(G.getVertices().size() != numNodes) {
				System.out.println("Number of nodes wrong");
				System.out.println("Number of read nodes:" + numNodes);
				System.out.println("Number of actual nodes:" + G.getVertices().size());
			}
			if(terminalCounter != numTerminals) {
				System.out.println("Number of terminals wrong");
				System.out.println("Number of read terminals:" + numTerminals);
				System.out.println("Number of actual terminals:" + terminalCounter);
			}
			System.out.println("Parsed correctly");
			return G;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}
		return null;
	}

}
