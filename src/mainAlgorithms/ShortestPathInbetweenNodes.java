package mainAlgorithms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import graph.Edge;
import graph.EdgeFake;
import graph.PathFinding;
import graph.UndirectedGraph;
import graph.Vertex;

public class ShortestPathInbetweenNodes implements SteinerTreeSolver {
	public Consumer<ArrayList<Edge>> sendBest;

	public List<Edge> bestEdges;

	public ShortestPathInbetweenNodes(Consumer<ArrayList<Edge>> object) {
		this.sendBest = object;
	}

	@Override
	public List<Edge> solve(UndirectedGraph G) {

		ArrayList<EdgeFake> bestResult = new ArrayList<>();
		int bestScore = Integer.MAX_VALUE;

		boolean stopFinding = false;
		Iterator<Integer> termKeys = G.getTerminals().keySet().iterator();
		long startTime = System.currentTimeMillis();
		int counter = 0;
		while (!stopFinding && termKeys.hasNext()) {
			counter++;
			ArrayList<Vertex> source = new ArrayList<>();
			// Initialize for using recursive behaviour
			Integer startingTerminal = termKeys.next();
			source.add(G.getTerminals().get(startingTerminal));
			ArrayList<Integer[]> searches = new ArrayList<>();

			for (Vertex nb : source.get(0).getNeighbors()) {
				if (searches.size() > 0) {
					binaryInsertion(searches,
							new Integer[] { source.get(0).getKey(), nb.getKey(), source.get(0).getConnectingEdge(nb).getCost().get() });
				} else
					searches.add(new Integer[] { source.get(0).getKey(), nb.getKey(), source.get(0).getConnectingEdge(nb).getCost().get() });
			}

			HashMap<Integer, ArrayList<Integer>> visited = new HashMap<>();
			visited.put(source.get(0).getKey(), new ArrayList<>());
			visited.get(source.get(0).getKey()).add(source.get(0).getKey());

			ArrayList<EdgeFake> result = new ArrayList<>();

			HashSet<Integer> inSet = new HashSet<>();
			inSet.add(source.get(0).getKey());

			ArrayList<EdgeFake> edges = this.dijkstraAllInbetween(G, source, searches, visited, result, 1, inSet);

			int currentScore = 0;
			for (EdgeFake e : edges) {
				currentScore += e.getCost();
			}
			// System.out.println("score: " + currentScore + " with terminal: " +
			// startingTerminal);
			if (currentScore < bestScore) {
				bestScore = currentScore;
				bestResult = edges;
				ArrayList<Edge> correspondingEdges = new ArrayList<>();
				for (EdgeFake e : bestResult) {
					if (e.getStack() != null) {
						for (int[] s : e.getStack()) {
							correspondingEdges.add(G.getVertices().get(s[0]).getConnectingEdge(G.getVertices().get(s[1])));
						}
					} else
						correspondingEdges.add(e.getVertices()[0].getConnectingEdge(e.getVertices()[1]));
				}
				this.sendBest.accept(correspondingEdges);
				this.bestEdges = correspondingEdges;
			}

			long avgTimeToComplete = (System.currentTimeMillis() - startTime) / counter;
			if ((5 * 60 * 1000) - (System.currentTimeMillis() - startTime) > (avgTimeToComplete * 2)) {
				continue;
			} else
				stopFinding = true;
		}

		return this.bestEdges;
	}

	public ArrayList<EdgeFake> dijkstraAllInbetween(UndirectedGraph graph, ArrayList<Vertex> source, ArrayList<Integer[]> searches,
			HashMap<Integer, ArrayList<Integer>> visited, ArrayList<EdgeFake> result, int numTerminals, HashSet<Integer> inSet) {
		Integer[] current;
		while (numTerminals < graph.getNumberOfTerminals()) {
			// System.out.println("source size: " + source.size());
			// System.out.println("Searches size: " + searches.size());
			// System.out.println("num terminals " + numTerminals);
			// for (Integer[] i : searches)
			// System.out.println(Arrays.toString(i));
			if (searches.size() == 0)
				System.out.println("ERROR");

			current = searches.remove(0);
			// System.out.println("inset: " + Arrays.toString(inSet.toArray()));
			while (inSet.contains(current[0]) && inSet.contains(current[1])) {// && !foundShortest.contains(this.current[1])) {
				current = searches.remove(0);
			}
			// foundShortest.add(this.current[1]);
			// System.out.println("Current: " + Arrays.toString(this.current));
			visited.get(current[0]).add(current[1]);
			if (graph.getVertices().get(current[1]).isTerminal()) {
				// System.out.println("Found terminal");
				ArrayList<Vertex> end = new ArrayList<>();
				end.add(graph.getVertices().get(current[1]));
				ArrayList<EdgeFake> path = PathFinding.DijkstraMultiPathFakeEdgesMultiSolution(graph, graph.getVertices().get(current[0]), end, null);
				// System.out.println("Path size " + path.size());
				if (path.size() == 1) {
					numTerminals++;
					result.addAll(path);
					ArrayList<Vertex> pathVertices = new ArrayList<>();
					EdgeFake e = path.get(0);
					if (e.getStack() != null)
						for (int[] i : e.getStack()) {
							// System.out.println("Edge: " + Arrays.toString(i));
							for (int j = 0; j < searches.size(); j++) {
								if (searches.get(j)[0] == current[0] && ((searches.get(j)[1] == i[0]) || (searches.get(j)[1] == i[1]))) {
									searches.remove(j);
									j--;
								}
							}
							if (!pathVertices.contains(graph.getVertices().get(i[0]))) {
								pathVertices.add(graph.getVertices().get(i[0]));
							}
							if (!pathVertices.contains(graph.getVertices().get(i[1]))) {
								pathVertices.add(graph.getVertices().get(i[1]));
							}
						}
					else {
						for (int j = 0; j < searches.size(); j++) {
							if (searches.get(j)[0] == current[0]
									&& ((searches.get(j)[1] == e.getVertices()[0].getKey()) || (searches.get(j)[1] == e.getVertices()[1].getKey()))) {
								searches.remove(j);
								j--;
							}
						}
						pathVertices.add(e.getVertices()[0]);
						pathVertices.add(e.getVertices()[1]);
					}
					// System.out.print("Path: ");
					for (Vertex v : pathVertices) {
						if (v != graph.getVertices().get(current[0])) {
							inSet.add(v.getKey());
							for (Vertex nb : v.getNeighbors()) {
								if (!pathVertices.contains(nb) && !inSet.contains(nb.getKey())) {
									binaryInsertion(searches, new Integer[] { v.getKey(), nb.getKey(), v.getConnectingEdge(nb).getCost().get() });
								}
							}
							source.add(v);
							visited.put(v.getKey(), new ArrayList<>());
							visited.get(v.getKey()).add(v.getKey());
						}
					}
				} else {
					ArrayList<ArrayList<EdgeFake>> allResults = new ArrayList<>();
					HashSet<Integer> inAPath = new HashSet<>();
					for (EdgeFake e : path)
						for (int[] i : e.getStack()) {
							inAPath.add(i[0]);
							inAPath.add(i[1]);
						}
					for (EdgeFake e : path) {
						// for (int[] i : e.getStack())
						// System.out.println("Here e: " + i[0] + " " + i[1] + " " + i[2]);
						// System.out.println();
						int tmpNumTerminals = numTerminals + 1;
						ArrayList<EdgeFake> tmpResult = (ArrayList<EdgeFake>) result.clone();
						tmpResult.add(e);
						ArrayList<Integer[]> tmpSearches = (ArrayList<Integer[]>) searches.clone();
						HashSet<Integer> tmpInSet = (HashSet<Integer>) inSet.clone();
						ArrayList<Vertex> tmpSource = (ArrayList<Vertex>) source.clone();
						HashMap<Integer, ArrayList<Integer>> tmpVisited = (HashMap<Integer, ArrayList<Integer>>) visited.clone();
						ArrayList<Vertex> pathVertices = new ArrayList<>();
						if (e.getStack() != null)
							for (int[] i : e.getStack()) {
								// System.out.println("Edge: " + Arrays.toString(i));
								for (int j = 0; j < tmpSearches.size(); j++) {
									if (tmpSearches.get(j)[0] == current[0] && ((tmpSearches.get(j)[1] == i[0]) || (tmpSearches.get(j)[1] == i[1]))) {
										tmpSearches.remove(j);
										j--;
									}
								}
								if (!pathVertices.contains(graph.getVertices().get(i[0]))) {
									pathVertices.add(graph.getVertices().get(i[0]));
								}
								if (!pathVertices.contains(graph.getVertices().get(i[1]))) {
									pathVertices.add(graph.getVertices().get(i[1]));
								}
							}
						else {
							for (int j = 0; j < tmpSearches.size(); j++) {
								if (tmpSearches.get(j)[0] == current[0] && ((tmpSearches.get(j)[1] == e.getVertices()[0].getKey())
										|| (tmpSearches.get(j)[1] == e.getVertices()[1].getKey()))) {
									tmpSearches.remove(j);
									j--;
								}
							}
							pathVertices.add(e.getVertices()[0]);
							pathVertices.add(e.getVertices()[1]);
						}
						// System.out.print("Path: ");
						for (Vertex v : pathVertices) {
							if (v != graph.getVertices().get(current[0])) {
								tmpInSet.add(v.getKey());
								for (Vertex nb : v.getNeighbors()) {
									if (!tmpInSet.contains(nb.getKey()) && !inAPath.contains(nb.getKey())) {
										binaryInsertion(tmpSearches,
												new Integer[] { v.getKey(), nb.getKey(), v.getConnectingEdge(nb).getCost().get() });
									}
								}
								tmpSource.add(v);
								tmpVisited.put(v.getKey(), new ArrayList<>());
								tmpVisited.get(v.getKey()).add(v.getKey());
							}
						}
						// System.out.println("InfoBlock start");
						// System.out.print("Source: ");
						// for (Vertex v : tmpSource)
						// System.out.print(v.getKey() + " ");
						// System.out.println("\nSearches: ");
						// for (Integer[] i : tmpSearches)
						// System.out.println(i[0] + " " + i[1] + " " + i[2]);
						// System.out.println("numTerminals:" + tmpNumTerminals);
						// System.out.println("InfoBlock end");
						allResults.add(this.dijkstraAllInbetween(graph, tmpSource, tmpSearches, tmpVisited, tmpResult, tmpNumTerminals, tmpInSet));
					}
					ArrayList<EdgeFake> bestTree = null;
					int bestScore = Integer.MAX_VALUE;
					// System.out.println("Calculating the score:");
					for (ArrayList<EdgeFake> res : allResults) {
						int curScore = 0;
						for (EdgeFake e : res) {
							curScore += e.getCost();
							// System.out.println("\n" + e.getVertices()[0].getKey() + " " +
							// e.getVertices()[1].getKey() + " " + e.getCost());
							// for (int[] i : e.getStack())
							// System.out.println(i[0] + " " + i[1] + " " + i[2]);
						}
						// System.out.println();
						if (curScore < bestScore) {
							bestTree = res;
							bestScore = curScore;
						}
					}
					// System.out.println("found best tree");
					return bestTree;
				}
				// System.out.println();
			} else {
				// System.out.println("in here");
				for (Vertex nb : graph.getVertices().get(current[1]).getNeighbors()) {
					if (!visited.get(current[0]).contains(nb.getKey()) && !inSet.contains(nb.getKey())) {
						boolean add = true;
						for (int i = 0; i < searches.size(); i++)
							if (searches.get(i)[0] == current[0] && searches.get(i)[1] == nb.getKey()) {
								if (searches.get(i)[2] <= current[2] + graph.getVertices().get(current[1]).getConnectingEdge(nb).getCost().get())
									add = false;
								else {
									searches.remove(i);
									i--;
								}
							}
						if (add)
							binaryInsertion(searches, new Integer[] { current[0], nb.getKey(),
									current[2] + graph.getVertices().get(current[1]).getConnectingEdge(nb).getCost().get() });
					}
				}
			}
		}
		// System.out.println(numTerminals + " " + graph.getNumberOfTerminals());
		// System.out.println("returning: " + result.size());
		// for (EdgeFake e : result)
		// for (int[] i : e.getStack())
		// System.out.println(i[0] + " " + i[1] + " " + i[2]);
		return result;
	}

	public static void binaryInsertion(ArrayList<Integer[]> searches, Integer[] insert) {
		//////////////////////////////////// INSERTION////////////////////////
		for (int i = 0; i < searches.size(); i++) {
			if (insert[2] <= searches.get(i)[2]) {
				searches.add(i, insert);
				return;
			}
		}
		searches.add(insert);

		////////////////////////// BINARY MY TRY///////////////////////
		// int upper = searches.size() - 1;
		// int lower = 0;
		// if (searches.size() == 0) {
		// searches.add(insert);
		// return;
		// }
		// while (true) {
		// if (upper == lower) {
		// if (searches.get(upper)[2] > insert[2])
		// searches.add(upper, insert);
		// else
		// searches.add(upper + 1, insert);
		// return;
		// } else {
		// int index = (int) Math.ceil(lower + ((upper - lower) / 2.0));
		// if (searches.get(index)[2] > insert[2]) {
		// if (upper > index)
		// upper = index;
		// else {
		// if (searches.get(lower)[2] > insert[2])
		// searches.add(lower, insert);
		// else
		// searches.add(index, insert);
		// return;
		// }
		// } else
		// lower = index;
		// }
		// }

		////////////////////////// BINARY ALGORITHM///////////////////
		// int end = searches.size() - 1;
		// int start = 0;
		// int m;
		// if (searches.size() == 0) {
		// searches.add(insert);
		// return;
		// }
		// while (true) {
		// m = (int) Math.ceil(start + ((end - start) / 2.0));
		// System.out.println(Arrays.toString(insert));
		// if (insert[2] > searches.get(end)[2]) {
		// System.out.println("if 1");
		// searches.add(insert);
		// return;
		// } else if (insert[2] < searches.get(start)[2]) {
		// System.out.println("if 2");
		// searches.add(0, insert);
		// return;
		// } else if (start >= end) {
		// System.out.println("if 3");
		// searches.add(start, insert);
		// return;
		// } else if (insert[2] < searches.get(m)[2]) {
		// System.out.println("if 4");
		// end = m - 1;
		// } else if (insert[2] > searches.get(m)[2]) {
		// System.out.println("if 5");
		// start = m + 1;
		// } else {
		// System.out.println("if 6");
		// searches.add(m, insert);
		// return;
		// }
		// }
	}

	// /**
	// * Performs Dijkstra's path finding algorithm and returns the new edges
	// between
	// * the vertices.
	// *
	// * @param G
	// * The graph in which Dijkstra has to be performed
	// * @param start
	// * The starting vertex
	// * @param end
	// * The endpoint vertices as an array
	// * @param edges
	// * The allowed edges to traverse over
	// * @return The new edges with the lowest weights
	// *
	// * @author Joshua Scheidt
	// */
	// public static ArrayList<EdgeFake>
	// DijkstraShortestPathHeuristic(UndirectedGraph G, ArrayList<Vertex> start,
	// HashMap<Vertex, HashMap<Vertex, DijkstraInfo>> fullInfo, HashMap<Vertex,
	// ArrayList<Vertex>> availableSearches,
	// HashMap<Vertex, Integer> lowestCosts, HashMap<Vertex, ArrayList<Integer>>
	// alreadyVisited) {
	// // System.out.println(start.size());
	// Vertex foundFrom = null, foundTerminal = null;
	// while (true) {
	// // System.out.println(Q.size());
	// int smallestDist = Integer.MAX_VALUE;
	// Vertex begin = null;
	// Vertex chosen = null;
	// Entry<Vertex, Integer> min = Collections.min(lowestCosts.entrySet(),
	// Comparator.comparing(Entry::getValue));
	//
	// // for (Vertex v : start)
	// for (Vertex i : availableSearches.get(min.getKey())) {
	// // System.out.println(fullInfo.get(v).get(i).dist);
	// if (fullInfo.get(min.getKey()).get(i).dist < smallestDist) {
	// begin = min.getKey();
	// chosen = i;
	// smallestDist = fullInfo.get(min.getKey()).get(i).dist;
	// // System.out.println("in here");
	// }
	// }
	// // System.out.println(chosen.getKey());
	//
	// if (chosen == null)
	// System.out.println("ERROR: No shortest distance vertex found with distance <
	// INTEGER.MAX_VALUE");
	// if (chosen.isTerminal() && !start.contains(chosen)) {
	// foundFrom = begin;
	// foundTerminal = chosen;
	// break;
	// }
	//
	// availableSearches.get(begin).remove(chosen);
	// alreadyVisited.get(begin).add(chosen.getKey());
	//
	// int distToCur = fullInfo.get(begin).get(chosen).dist;
	// int totDistToNb = 0;
	// for (Vertex nb : chosen.getNeighbors()) {
	// if (G.getEdges().contains(chosen.getConnectingEdge(nb)) &&
	// !alreadyVisited.get(begin).contains(nb.getKey())) {
	// totDistToNb = distToCur + chosen.getConnectingEdge(nb).getCost().get();
	// DijkstraInfo nbInfo = fullInfo.get(begin).get(nb);
	// // System.out.println("before:" + nbInfo.dist);
	// if (nbInfo == null)
	// System.out.println(nb.getKey() + " ???");
	// if (totDistToNb < nbInfo.dist) {
	// nbInfo.dist = totDistToNb;
	// nbInfo.parent = chosen;
	// if (!availableSearches.get(begin).contains(nb)) {
	// availableSearches.get(begin).add(nb);
	// }
	// }
	// // System.out.println("after: " + nbInfo.dist);
	// }
	// }
	// int lowest = Integer.MAX_VALUE;
	// for (Vertex v : availableSearches.get(begin)) {
	// if (lowest > fullInfo.get(begin).get(v).dist)
	// lowest = fullInfo.get(begin).get(v).dist;
	// }
	// lowestCosts.put(begin, lowest);
	// }
	//
	// ArrayList<EdgeFake> result = new ArrayList<>();
	// ArrayList<Vertex> path = new ArrayList<>();
	// Vertex current = foundTerminal;
	// while (fullInfo.get(foundFrom).get(current).parent != null) {
	// path.add(current);
	// current = fullInfo.get(foundFrom).get(current).parent;
	// }
	// path.add(current);
	// if (foundFrom.isNeighbor(foundTerminal))
	// result.add(new EdgeFake(foundFrom, foundTerminal,
	// foundFrom.getConnectingEdge(foundTerminal).getCost().get(),
	// foundFrom.getConnectingEdge(foundTerminal).getStack()));
	// else {
	// Stack<int[]> pathStack = new Stack<>();
	// for (int i = 0; i < path.size() - 1; i++) {
	// pathStack.push(new int[] { path.get(i).getKey(), path.get(i + 1).getKey(),
	// G.getVertices().get(path.get(i).getKey()).getConnectingEdge(G.getVertices().get(path.get(i
	// + 1).getKey())).getCost().get() });
	// }
	// EdgeFake newEdge = new EdgeFake(foundFrom, foundTerminal,
	// fullInfo.get(foundFrom).get(foundTerminal).dist, pathStack);
	// result.add(newEdge);
	// }
	//
	// return result;
	// }

}
