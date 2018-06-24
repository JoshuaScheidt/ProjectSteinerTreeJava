package graph;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import mainAlgorithms.ShortestPathHeuristicV2;
import mainAlgorithms.SteinerTreeSolver;

public class RandomMain {

	public static String fileName;
	public static List<Edge> currentBest;
	public static boolean written = false;

	public static void main(String[] args) throws InterruptedException {
		// Scanner in = new Scanner(System.in);
		// final CountDownLatch exit_now = new CountDownLatch(1);
		// double worker = 0.0;
		// int n;
		//
		// SignalHandler termHandler = new SignalHandler() {
		// @Override
		// public void handle(Signal sig) {
		// System.out.println("Terminating");
		// exit_now.countDown();
		// }
		// };
		// Signal.handle(new Signal("TERM"), termHandler);
		//
		// n = in.nextInt();
		// for (int i = 0; i < n && exit_now.getCount() == 1; i++) {
		// worker += Math.sqrt(i);
		// }
		// System.out.print((int) (worker / n));

		// shortestPathHeuristicV2();
		// System.out.println("\n");

		// long start = System.currentTimeMillis();
		// shortestPathHeuristicV2();
		// long middle = System.currentTimeMillis();
		// shortestPathHeuristicV2FullPreprocess();
		// long end = System.currentTimeMillis();
		// System.out.println("Without preprocess took " + (middle - start) + " ms");
		// System.out.println("With preprocess took " + (end - middle) + " ms");

		// writeArticulationPointsToFile();
		// doAnalysisLeafDegree2();
		try {
			doAnalysisSectioning();
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// testSectioning();
	}

	public static void shortestPathHeuristicV2() {
		File[] files = readFiles(new File("data\\exact\\instance020.gr"));
		for (int i = 0; i < files.length; i++) {
			System.out.println(files[i].getParent() + "\\" + files[i].getName());
			SteinerTreeSolver solver = new ShortestPathHeuristicV2();

			UndirectedGraph graph = new UndirectedGraphReader().read(files[i]);

			List<Edge> result = solver.solve(graph);
			System.out.println("Value without preprocess: ");
			printSolution(result, false);
		}
	}

	public static void shortestPathHeuristicV2FullPreprocess() {
		File[] files = readFiles(new File("data\\exact\\instance020.gr"));
		for (int i = 0; i < files.length; i++) {
			System.out.println(files[i].getParent() + "\\" + files[i].getName());
			SteinerTreeSolver solver = new ShortestPathHeuristicV2();

			UndirectedGraph graph = new UndirectedGraphReader().read(files[i]);
			PreProcess processed = new PreProcess(graph);
			boolean[] preProcessable;
			do {
				preProcessable = processed.graph.preProcessable();
				// pp.rangeCheck();
				if (preProcessable[0]) {
					processed.removeLeafNodes();
				}
				if (preProcessable[1]) {
					processed.removeNonTerminalDegreeTwo();
				}
			} while (preProcessable[0] || preProcessable[1]);

			// Sectioning part
			ArrayList<UndirectedGraph> subGraphs = processed.createSeparateSections(
					processed.graph.getVertices().get(processed.graph.getVertices().keySet().iterator().next()), graph.getVertices().size());
			List<Edge> solution = new ArrayList<>();
			for (UndirectedGraph sub : subGraphs) {
				solution.addAll(solver.solve(sub));
			}
			// End of sectioning part

			System.out.println("Value with full preprocessing: ");
			printSolution(solution, false);
		}
	}

	public static void shortestPathHeuristicV2wPP() {
		File[] files = readFiles(new File("data\\exact\\instance030.gr"));
		for (int i = 0; i < files.length; i++) {
			System.out.println(files[i].getParent() + "\\" + files[i].getName());
			SteinerTreeSolver solver = new ShortestPathHeuristicV2();

			UndirectedGraph graph = new UndirectedGraphReader().read(files[i]);
			PreProcess processed = new PreProcess(graph);
			boolean[] preProcessable;
			do {
				preProcessable = processed.graph.preProcessable();
				// pp.rangeCheck();
				if (preProcessable[0]) {
					processed.removeLeafNodes();
				}
				if (preProcessable[1]) {
					processed.removeNonTerminalDegreeTwo();
				}
			} while (preProcessable[0] || preProcessable[1]);

			List<Edge> result = solver.solve(processed.graph);
			int res = 0;
			for (Edge e : result)
				res += e.getCost().get();
			System.out.println("Value with 2 preprocess: " + res);
		}
	}

	public static void testSectioning() {
		File[] files = readFiles(new File("data\\test\\testArticulationNew.gr"));
		for (int i = 0; i < files.length; i++) {
			System.out.println(files[i].getParent() + "\\" + files[i].getName());
			SteinerTreeSolver solver = new ShortestPathHeuristicV2();

			UndirectedGraph graph = new UndirectedGraphReader().read(files[i]);
			PreProcess processed = new PreProcess(graph);
			long start = System.currentTimeMillis();
			ArrayList<UndirectedGraph> subgraphs = processed
					.createSeparateSections(graph.getVertices().get(graph.getVertices().keySet().toArray()[0]), graph.getVertices().size());
			System.out.println("Sectioning took: " + (System.currentTimeMillis() - start) + "ms");
			// System.out.println("\n\n\n");
			for (UndirectedGraph g : subgraphs) {
				System.out.println("New section");
				for (Edge e : g.getEdges()) {
					System.out.println(e.getVertices()[0].getKey() + " " + e.getVertices()[1].getKey() + " " + e.getCost().get());
				}
				for (Vertex t : g.getTerminals().values())
					System.out.println("Terminal:" + t.getKey());
			}
			System.out.println(subgraphs.size());

			List<Edge> result = new ArrayList<>();
			for (UndirectedGraph gr : subgraphs) {
				List<Edge> subRes = solver.solve(gr);
				if (subRes != null)
					result.addAll(subRes);
			}
			int res = 0;
			for (Edge e : result)
				res += e.getCost().get();
			System.out.println("result = " + res);
		}
	}

	public static void writeArticulationPointsToFile() {
		File[] files = readFiles(new File("data\\exact"));
		int[][] info = new int[files.length][5];
		for (int i = 0; i < files.length; i++) {
			System.out.println(i);
			UndirectedGraph graph = new UndirectedGraphReader().read(files[i]);
			info[i][0] = graph.getVertices().size();
			info[i][1] = graph.getEdges().size();
			info[i][2] = graph.getNumberOfTerminals();
			PreProcess ppGraph = new PreProcess(graph);
			long start = System.currentTimeMillis();
			info[i][3] = ppGraph
					.articulationPointFinding(graph.getVertices().get(graph.getVertices().keySet().toArray()[0]), graph.getVertices().size()).size();
			info[i][4] = (int) (System.currentTimeMillis() - start);
		}
		System.out.println("\n\n Writing Total results:");
		try {
			PrintWriter writer = new PrintWriter("E:\\Programming\\Java\\ProjectSteinerTreeJava\\res\\artiPointsExact.txt", "UTF-8");
			// tms = time in ms
			writer.println("FileIndex, #Vertices, #Edges, #Terminals, #ArtiPoints, Time(ms)");
			String cur = "";
			for (int i = 0; i < info.length; i++) {
				cur = Integer.toString(i + 1);
				for (int j = 0; j < info[i].length; j++) {
					cur += ", " + Integer.toString(info[i][j]);
				}
				writer.println(cur);
			}
			writer.close();
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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

	/**
	 * Prints solution to standard out. Checks each edge and vertex to see if it
	 * contains other hidden edges and or vertices that need to be included
	 *
	 * @param solution
	 *            Solution including all the edges in the solution
	 */
	private static void printSolution(List<Edge> solution, boolean toFile) {
		String temp = "";
		int sum = 0;
		int[] subsumed;
		for (int i = 0; i < solution.size(); i++) {
			if (!(solution.get(i).getVertices()[0].getSubsumed() == null)) {
				while (!solution.get(i).getVertices()[0].getSubsumed().isEmpty()) {
					subsumed = solution.get(i).getVertices()[0].getSubsumed().pop();
					temp = temp.concat(subsumed[0] + " " + subsumed[1] + "\n");
					sum += subsumed[2];
				}
			}
			if (!(solution.get(i).getVertices()[1].getSubsumed() == null)) {
				while (!solution.get(i).getVertices()[1].getSubsumed().isEmpty()) {
					subsumed = solution.get(i).getVertices()[1].getSubsumed().pop();
					temp = temp.concat(subsumed[0] + " " + subsumed[1] + "\n");
					sum += subsumed[2];
				}
			}
			if (!(solution.get(i).getStack() == null)) {
				while (!solution.get(i).getStack().isEmpty()) {
					subsumed = solution.get(i).getStack().pop();
					temp = temp.concat(subsumed[0] + " " + subsumed[1] + "\n");
					sum += subsumed[2];
				}
				continue;
			}
			temp = temp.concat(solution.get(i).getVertices()[0].getKey() + " " + solution.get(i).getVertices()[1].getKey() + "\n");
			sum += solution.get(i).getCost().get();
		}
		if (toFile) {
			Path file = Paths.get(fileName.substring(0, fileName.length() - 3) + ".txt");
			ArrayList<String> output = new ArrayList<>();
			output.add("VALUE" + sum);
			output.add(temp);
			try {
				Files.write(file, output, Charset.forName("UTF-8"));
			} catch (IOException ex) {
			}
		} else {
			System.out.println("VALUE " + sum);
			System.out.println(temp);
		}
	}

	/**
	 * Perform analysis
	 *
	 * @param files
	 *
	 * @author Marciano Geijselaers
	 * @author Joshua Scheidt
	 */
	private static void doAnalysisLeafDegree2() {
		File[] files = readFiles(new File("data\\heuristics"));
		Integer[][][] results = new Integer[files.length][4][4]; // Per file, save all different graphs' Nodes, Terminals and Edges. The second
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

			// // Create a cloned graph which will use preprocessing.
			// PreProcess improved = new PreProcess(graph);
			// printCurrentSize(improved);

			// Prints the degrees of the vertices from the original graph up to a maximum of
			// degree 9.
			// System.out.println("Original Degree Scale: ");
			// int[] degrees = graph.countDegree();
			// for (int i = 0; i < degrees.length; i++) {
			// System.out.print(degrees[i] + ", ");
			// }
			// System.out.println("");

			// Leaf Node Removal
			start = System.currentTimeMillis();
			improved.removeLeafNodes();
			end = System.currentTimeMillis();
			results[fileIndex][1][0] = improved.graph.getVertices().size();
			results[fileIndex][1][1] = improved.graph.getNumberOfTerminals();
			results[fileIndex][1][2] = improved.graph.getEdges().size();
			results[fileIndex][1][3] = (int) (end - start);

			// Remove non-degree terminal
			improved = new PreProcess(graph);
			start = System.currentTimeMillis();
			improved.removeNonTerminalDegreeTwo();
			end = System.currentTimeMillis();
			results[fileIndex][2][0] = improved.graph.getVertices().size();
			results[fileIndex][2][1] = improved.graph.getNumberOfTerminals();
			results[fileIndex][2][2] = improved.graph.getEdges().size();
			results[fileIndex][2][3] = (int) (end - start);

			// Iterative part here
			improved = new PreProcess(graph);
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
			// start = System.currentTimeMillis();
			// // improved.removeBridgesAndSections(graph.getVertices().size());
			// end = System.currentTimeMillis();
			// results[fileIndex][4][0] = graph.getVertices().size();
			// results[fileIndex][4][1] = graph.getNumberOfTerminals();
			// results[fileIndex][4][2] = graph.getEdges().size();
			// results[fileIndex][4][3] = (int) (end - start);

			System.out.println("done with index " + fileIndex);

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
		System.out.println("\n\n Writing Total results:");
		try {
			PrintWriter writer = new PrintWriter("E:\\Programming\\Java\\ProjectSteinerTreeJava\\res\\preprocessingStatsHeuristics.txt", "UTF-8");
			// tms = time in ms
			writer.println(files[0].getParentFile().getPath());
			writer.println(
					"FileIndex, V(original), T(original), E(original), tms(original), V(leaf), T(leaf), E(leaf), tms(leaf), V(deg2), T(deg2), E(deg2), tms(deg2), V(both), T(both), E(both), tms(both)");
			String cur = "";
			for (int i = 0; i < results.length; i++) {
				cur = Integer.toString(i + 1);
				for (int j = 0; j < results[i].length; j++) {
					for (int k = 0; k < results[i][j].length; k++) {
						cur += ", " + Integer.toString(results[i][j][k]);
					}
				}
				writer.println(cur);
			}
			writer.close();
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Perform analysis
	 *
	 * @param files
	 *
	 * @author Marciano Geijselaers
	 * @author Joshua Scheidt
	 * @throws UnsupportedEncodingException
	 * @throws FileNotFoundException
	 */
	private static void doAnalysisSectioning() throws FileNotFoundException, UnsupportedEncodingException {
		File[] files = readFiles(new File("data\\exact"));
		// PrintWriter writer = new
		// PrintWriter("E:\\Programming\\Java\\ProjectSteinerTreeJava\\res\\sectioningStatsExact.txt",
		// "UTF-8");
		// writer.println(files[0].getParentFile().getPath());
		// writer.println("FileIndex, V(combined), T(combined), E(combined),
		// tms(sectioning), empty, V(i), T(i), E(i), empty, repeat");
		// Integer[][][] results = new Integer[files.length][4][4]; // Per file, save
		// all different graphs' Nodes, Terminals and Edges. The second
		// index has to be changed depending on which comparisons we want. The first
		// index will always be the base graph without preprocess changes.

		for (int fileIndex = 0; fileIndex < files.length; fileIndex++) {
			// Read the standard graph and set its values to the first index of the results.
			Long start, end;
			UndirectedGraph graph = new UndirectedGraphReader().read(files[fileIndex]);

			// Create a cloned graph which will use preprocessing.
			PreProcess improved = new PreProcess(graph);
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

			start = System.currentTimeMillis();

			ArrayList<UndirectedGraph> subGraphs = improved.createSeparateSections(
					improved.graph.getVertices().get(improved.graph.getVertices().keySet().iterator().next()), graph.getVertices().size());
			end = System.currentTimeMillis();
			System.out.println(subGraphs.size());

			String tmp = fileIndex + "," + improved.graph.getVertices().size() + "," + improved.graph.getNumberOfTerminals() + ","
					+ improved.graph.getEdges().size() + "," + (end - start);
			for (UndirectedGraph sub : subGraphs) {
				tmp += ", ," + sub.getVertices().size() + "," + sub.getNumberOfTerminals() + "," + sub.getEdges().size();
			}
			// writer.println(tmp);
			// Bridge Finding
			// start = System.currentTimeMillis();
			// // improved.removeBridgesAndSections(graph.getVertices().size());
			// end = System.currentTimeMillis();
			// results[fileIndex][4][0] = graph.getVertices().size();
			// results[fileIndex][4][1] = graph.getNumberOfTerminals();
			// results[fileIndex][4][2] = graph.getEdges().size();
			// results[fileIndex][4][3] = (int) (end - start);

			System.out.println("done with index " + fileIndex);

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
		// writer.close();

	}

}
