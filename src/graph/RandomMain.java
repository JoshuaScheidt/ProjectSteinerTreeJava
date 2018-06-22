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
		testSectioning();
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
		File[] files = readFiles(new File("data\\test\\testArticulationNew2.gr"));
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

}
