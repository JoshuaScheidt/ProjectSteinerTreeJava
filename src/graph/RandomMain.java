package graph;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import mainAlgorithms.ShortestPathHeuristicV2;
import mainAlgorithms.SteinerTreeSolver;

public class RandomMain {

	public static void main(String[] args) {
		shortestPathHeuristicV2();
	}

	public static void shortestPathHeuristicV2() {
		File[] files = readFiles(new File("data\\heuristics\\instance001.gr"));
		for (int i = 0; i < files.length; i++) {
			SteinerTreeSolver solver = new ShortestPathHeuristicV2();
			List<Edge> result = solver.solve(new UndirectedGraphReader().read(files[i]));
		}
	}

	public void writeArticulationPointsToFile() {
		File[] files = readFiles(new File("data\\heuristics"));
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
			PrintWriter writer = new PrintWriter("E:\\Programming\\Java\\ProjectSteinerTreeJava\\res\\artiPoints.txt", "UTF-8");
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

}
