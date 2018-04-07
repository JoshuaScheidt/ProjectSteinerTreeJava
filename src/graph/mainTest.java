package graph;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class mainTest {

	public static void main(String[] args) {

		File[] files = readFiles(new File("data\\test\\testAnalysis.gr"));

		UndirectedGraph graphTest = new UndirectedGraphReader().read(files[0]);
		// System.out.println(PathFinding.DijkstraSingleEdge(graphTest,
		// graphTest.getVertices().get(13), graphTest.getVertices().get(6)));
		PreProcess processed = new PreProcess(graphTest);
		long starts = System.currentTimeMillis();
		processed.removeBridgesAndSections(graphTest.getVertices().size());
		System.out.println("Took " + (System.currentTimeMillis() - starts) + " ms");
		for (int v : processed.graph.getVertices().keySet())
			System.out.print(v + " ");
		System.out.println();
		System.out.println("Total vertices: " + processed.graph.getVertices().size());
		System.out.println("Total edges:" + processed.graph.getEdges().size());
		// for (Edge e : processed.graph.getEdges()) {
		// System.out.println(e.getVertices()[0].getKey() + " " +
		// e.getVertices()[1].getKey() + " " + e.getCost().get());
		// }
		System.exit(1);

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
			results[fileIndex][1][0] = improved.graph.getVertices().size();
			results[fileIndex][1][1] = improved.graph.getNumberOfTerminals();
			results[fileIndex][1][2] = improved.graph.getEdges().size();
			results[fileIndex][1][3] = (int) (end - start);

			// Remove non-degree terminal
			start = System.currentTimeMillis();
			improved.removeNonTerminalDegreeTwo();
			end = System.currentTimeMillis();
			results[fileIndex][2][0] = improved.graph.getVertices().size();
			results[fileIndex][2][1] = improved.graph.getNumberOfTerminals();
			results[fileIndex][2][2] = improved.graph.getEdges().size();
			results[fileIndex][2][3] = (int) (end - start);

			// Iterative part here
			start = System.currentTimeMillis();
			boolean[] keepPreProcessing = improved.graph.preProcessable();
			while (keepPreProcessing[0] || keepPreProcessing[1]) {
				if (keepPreProcessing[0]) {
					improved.removeLeafNodes();
				}
				if (keepPreProcessing[1]) {
					improved.removeNonTerminalDegreeTwo();
				}
				keepPreProcessing = improved.graph.preProcessable();
			}
			end = System.currentTimeMillis();
			results[fileIndex][3][0] = improved.graph.getVertices().size();
			results[fileIndex][3][1] = improved.graph.getNumberOfTerminals();
			results[fileIndex][3][2] = improved.graph.getEdges().size();
			results[fileIndex][3][3] = (int) (end - start);

			// Bridge Finding
			start = System.currentTimeMillis();
			improved.removeBridgesAndSections(graph.getVertices().size());
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
		double[] percentileReductionVertices = new double[results.length];
		double[] percentileReductionEdges = new double[results.length];
		double[] percentileReductionTerminals = new double[results.length];
		double[] averageTimeTaken = new double[results.length];
		int counter = 0;
		for (Integer[][] singleFileResults : results) {
			System.out.println(Arrays.toString(singleFileResults[0]));
			percentileReductionVertices[counter] = ((double) (singleFileResults[0][0] - singleFileResults[3][0])) / (double) singleFileResults[0][0];
			percentileReductionTerminals[counter] = ((double) (singleFileResults[0][1] - singleFileResults[3][1])) / (double) singleFileResults[0][1];
			percentileReductionEdges[counter] = ((double) (singleFileResults[0][2] - singleFileResults[3][2])) / (double) singleFileResults[0][2];
			averageTimeTaken[counter] = singleFileResults[1][3] + singleFileResults[2][3] + singleFileResults[3][3];
			counter++;
		}
		System.out.println(Arrays.toString(percentileReductionVertices));
		System.out.println(Arrays.toString(percentileReductionEdges));
		System.out.println(Arrays.toString(percentileReductionTerminals));
		System.out.println(Arrays.toString(averageTimeTaken));

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
