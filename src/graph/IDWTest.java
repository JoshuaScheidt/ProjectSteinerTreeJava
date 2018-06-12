package graph;

import mainAlgorithms.SteinerTreeSolver;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;


class IDWTest {
	
	public static String fileName;
	
	public static void main(String[] args) {
		
		long start = System.currentTimeMillis();
		File[] files = readFiles(new File("data\\exact\\instance001.gr")); // windows
//		File[] files = readFiles(new File("data/exact/instance001.gr")); // mac
		graph.UndirectedGraph g = new graph.UndirectedGraphReader().read(files[0]);
		
//		UndirectedGraph g = new UndirectedGraphReader().read();
		
		
		PreProcess pp = new PreProcess(g);
//		pp.removeNonTerminalDegreeTwo();
		pp.removeTerminals();
		pp.removeLeafNodes();
		
		
//		 pp.removeBridgesAndSections(g.getVertices().size());
//		 boolean[] preProcessable;
//		 do {
//		 	preProcessable = pp.graph.preProcessable();
//		 	pp.rangeCheck();
//		 	if (preProcessable[0]) {
//		 		pp.removeLeafNodes();
//		 	}
//		 	if (preProcessable[1]) {
//		 		pp.removeNonTerminalDegreeTwo();
//		 	}
//		 } while (preProcessable[0] || preProcessable[1]);

		SteinerTreeSolver solver = new mainAlgorithms.ImprovedDreyfusWagner();
		printSolution(solver.solve(pp.graph), false);
		printTimeNeeded(start);
	}
	
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
	
	private static void printTimeNeeded(long start) {
		
		long millis = System.currentTimeMillis() - start;
		System.out.println(String.format("%d:%d:%d", 
			TimeUnit.MILLISECONDS.toMinutes(millis),
			TimeUnit.MILLISECONDS.toSeconds(millis) - 
			TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)),
			(millis%1000))
		);
	}
	
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
//				Logger.getLogger(CutILP.class.getName()).log(Level.SEVERE, null, ex);
			}
		} else {
			System.out.println("VALUE " + sum);
//			System.out.println(temp);
		}
	}
}