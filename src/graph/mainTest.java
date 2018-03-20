package graph;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class mainTest {

	public static void main(String[] args) {

		File[] files = readFiles(new File("data\\heuristics"));

		Integer[][][] results = new Integer[files.length][5][4]; // Per file, save all different graphs' Nodes, Terminals and Edges. The second
																	// index has to be changed depending on which comparisons we want. The first
																	// index will always be the base graph without preprocess changes.

		for (int fileIndex = 0; fileIndex < files.length; fileIndex++) {
			// Read the standard graph and set its values to the first index of the results.
			Long start, end;
			start = System.currentTimeMillis();
			UndirectedGraph graph = new UndirectedGraphReader().read(files[fileIndex]);
			end = System.currentTimeMillis();
			results[fileIndex][0][0] = graph.getVertices().size();
			results[fileIndex][0][1] = graph.getNumberOfTerminals();
			results[fileIndex][0][2] = graph.getEdges().size();
			results[fileIndex][0][3] = (int) (end - start);

			// Create a cloned graph which will use preprocessing.
			PreProcess improved = new PreProcess(graph);
			printCurrentSize(improved);

			// Prints the degrees of the vertices from the original graph up to a maximum of
			// degree 9.
			System.out.println("Original Degree Scale: ");
			int[] degrees = graph.countDegree();
			for (int i = 0; i < degrees.length; i++) {
				System.out.print(degrees[i] + ", ");
			}
			System.out.println("");

			// Leaf Node Removal
			start = System.currentTimeMillis();
			improved.removeLeafNodes();
			end = System.currentTimeMillis();
			results[fileIndex][1][0] = graph.getVertices().size();
			results[fileIndex][1][1] = graph.getNumberOfTerminals();
			results[fileIndex][1][2] = graph.getEdges().size();
			results[fileIndex][1][3] = (int) (end - start);

			// Remove non-degree terminal
			start = System.currentTimeMillis();
			improved.removeNonTerminalDegreeTwo();
			end = System.currentTimeMillis();
			results[fileIndex][2][0] = graph.getVertices().size();
			results[fileIndex][2][1] = graph.getNumberOfTerminals();
			results[fileIndex][2][2] = graph.getEdges().size();
			results[fileIndex][2][3] = (int) (end - start);

			// Iterative part here
			start = System.currentTimeMillis();
			// improved.removeNonTerminalDegreeTwo();
			end = System.currentTimeMillis();
			results[fileIndex][3][0] = graph.getVertices().size();
			results[fileIndex][3][1] = graph.getNumberOfTerminals();
			results[fileIndex][3][2] = graph.getEdges().size();
			results[fileIndex][3][3] = (int) (end - start);

			// Bridge Finding
			start = System.currentTimeMillis();
			improved.removeBridgesAndSections();
			end = System.currentTimeMillis();
			results[fileIndex][4][0] = graph.getVertices().size();
			results[fileIndex][4][1] = graph.getNumberOfTerminals();
			results[fileIndex][4][2] = graph.getEdges().size();
			results[fileIndex][4][3] = (int) (end - start);

			System.out.println("done");

			// Leaf Node Removal
			// improved.removeLeafNodes();
			//
			// // Non-Terminal Degree Two removal
			// long start = System.nanoTime();
			// improved.removeNonTerminalDegreeTwo();
			// long stop = System.nanoTime();
			// System.out.println("Time Taken: " + (stop - start) / 1000000000.0);
			//
			// printCurrentSize(improved);
			// printDegreeScale(improved);
			//
			// improved.removeLeafNodes();
			//
			// printCurrentSize(improved);
			// printDegreeScale(improved);
			//
			// improved.removeNonTerminalDegreeTwo();
		}
		System.out.println("\n\nTotal results:");
		for (Integer[][] singleFileResults : results) {
			System.out.println(Arrays.toString(singleFileResults[0]));
		}

	}

	/**
	 * Reads all files from a given directory. The directory is allowed to be both a
	 * folder or a file.
	 *
	 * @param directory
	 *            The directory for which file(s) have to be read.
	 * @return An array of found files.
	 *
	 * @author Joshua Scheidt
	 */
	private static File[] readFiles(File directory) {
		if (directory.exists()) {
			if (directory.isFile() && directory.getName().contains(".gr")) {
				return new File[] { directory };
			} else if (directory.isDirectory()) {
				File[] files = directory.listFiles();
				ArrayList<File> filesList = new ArrayList<>();
				for (int i = 0; i < files.length; i++) {
					if (files[i].isFile() && files[i].getName().contains(".gr")) {
						filesList.add(files[i]);
					}
				}
				return filesList.toArray(files);
			}
		}
		return new File[] {};
	}

	public static void printCurrentSize(PreProcess improved) {
		System.out.println("Current number of (Vertices, Terminals): (" + improved.graph.getVertices().size() + ", "
				+ improved.graph.getNumberOfTerminals() + ") Current number of Edges: " + improved.graph.getEdges().size());
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
