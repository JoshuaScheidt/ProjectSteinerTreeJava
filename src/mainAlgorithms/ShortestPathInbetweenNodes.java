package mainAlgorithms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import graph.Edge;
import graph.EdgeFake;
import graph.PathFinding;
import graph.UndirectedGraph;
import graph.Vertex;

public class ShortestPathInbetweenNodes implements SteinerTreeSolver {

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
			Integer startingTerminal = termKeys.next();
			source.add(G.getTerminals().get(startingTerminal));
			ArrayList<EdgeFake> edges = this.dijkstraAllInbetween(G, source);
			// ///////////////////////////////////////
			// UndirectedGraph tmp = G.clone();
			// ArrayList<EdgeFake> edges = this.dijkstraAllInbetween(tmp, source);
			// ArrayList<Edge> remainder = new ArrayList<>();
			// int currentScore = 0;
			// for (EdgeFake e : edges) {
			// currentScore += e.getCost();
			// }
			// System.out.println("score: " + currentScore + " with terminal: " +
			// startingTerminal);
			// if (currentScore < bestScore) {
			// bestScore = currentScore;
			// bestResult = edges;
			// }
			//
			// for (EdgeFake e : bestResult) {
			// if (e.getStack() != null) {
			// for (int[] s : e.getStack()) {
			// remainder.add(tmp.getVertices().get(s[0]).getConnectingEdge(tmp.getVertices().get(s[1])));
			// }
			// } else
			// remainder.add(e.getVertices()[0].getConnectingEdge(e.getVertices()[1]));
			// }
			// System.out.println("Graph with terminal:" + startingTerminal);
			// for (Edge e : remainder)
			// System.out.println(e.getVertices()[0].getKey() + " " +
			// e.getVertices()[1].getKey() + e.getCost().get());
			// tmp.checkConnectivity(remainder);
			// ////////////////////////////////////
			int currentScore = 0;
			for (EdgeFake e : edges) {
				currentScore += e.getCost();
			}
			// System.out.println("score: " + currentScore + " with terminal: " +
			// startingTerminal);
			if (currentScore < bestScore) {
				bestScore = currentScore;
				bestResult = edges;
			}

			long avgTimeToComplete = (System.currentTimeMillis() - startTime) / counter;
			if ((30 * 60 * 1000) - (System.currentTimeMillis() - startTime) > (avgTimeToComplete * 2)) {
				continue;
			} else
				stopFinding = true;
		}

		ArrayList<Edge> tbrEdges = new ArrayList<>();
		for (Edge e : G.getEdges())
			tbrEdges.add(e);

		for (EdgeFake e : bestResult) {
			if (e.getStack() != null) {
				for (int[] s : e.getStack()) {
					tbrEdges.remove(G.getVertices().get(s[0]).getConnectingEdge(G.getVertices().get(s[1])));
				}
			} else
				tbrEdges.remove(e.getVertices()[0].getConnectingEdge(e.getVertices()[1]));
		}
		for (Edge e : tbrEdges)
			G.removeEdge(e);
		ArrayList<Vertex> tbrVertices = new ArrayList<>();
		for (Vertex v : G.getVertices().values()) {
			if (v.getEdges().size() == 0)
				tbrVertices.add(v);
		}
		for (Vertex v : tbrVertices) {
			G.removeVertex(v);
		}
		ArrayList<Edge> result = new ArrayList<>();
		for (Edge e : G.getEdges())
			result.add(e);

		return result;
	}

	private Integer[] current;

	public ArrayList<EdgeFake> dijkstraAllInbetween(UndirectedGraph graph, ArrayList<Vertex> source) {
		ArrayList<Integer[]> searches = new ArrayList<>();
		// ArrayList<Integer> foundShortest = new ArrayList<>();
		HashMap<Integer, ArrayList<Integer>> visited = new HashMap<>();
		for (Vertex nb : source.get(0).getNeighbors()) {
			if (searches.size() > 0) {
				binaryInsertion(searches, new Integer[] { source.get(0).getKey(), nb.getKey(), source.get(0).getConnectingEdge(nb).getCost().get() });
			} else
				searches.add(new Integer[] { source.get(0).getKey(), nb.getKey(), source.get(0).getConnectingEdge(nb).getCost().get() });
		}
		// for (Integer[] i : searches)
		// System.out.println(Arrays.toString(i));
		visited.put(source.get(0).getKey(), new ArrayList<>());
		visited.get(source.get(0).getKey()).add(source.get(0).getKey());
		// foundShortest.add(source.get(0).getKey());
		ArrayList<EdgeFake> result = new ArrayList<>();
		int numTerminals = 1;
		HashSet<Integer> inSet = new HashSet<>();
		inSet.add(source.get(0).getKey());

		while (numTerminals < graph.getNumberOfTerminals()) {
			// System.out.println("source size: " + source.size());
			// System.out.println("Searches size: " + searches.size());
			// for (Integer[] i : searches)
			// System.out.println(Arrays.toString(i));
			if (searches.size() == 0)
				System.out.println("ERROR");

			this.current = searches.remove(0);
			// System.out.println("inset: " + Arrays.toString(inSet.toArray()));
			while (inSet.contains(this.current[0]) && inSet.contains(this.current[1])) {// && !foundShortest.contains(this.current[1])) {
				this.current = searches.remove(0);
			}
			// foundShortest.add(this.current[1]);
			// System.out.println("Current: " + Arrays.toString(this.current));
			visited.get(this.current[0]).add(this.current[1]);
			if (graph.getVertices().get(this.current[1]).isTerminal()) {
				// System.out.println("Found terminal");
				ArrayList<Vertex> end = new ArrayList<>();
				end.add(graph.getVertices().get(this.current[1]));
				ArrayList<EdgeFake> path = PathFinding.DijkstraMultiPathFakeEdges(graph, graph.getVertices().get(this.current[0]), end, null);
				numTerminals++;
				// System.out.println(path.size());
				result.addAll(path);
				ArrayList<Vertex> pathVertices = new ArrayList<>();
				EdgeFake e = path.get(0);
				if (e.getStack() != null)
					for (int[] i : e.getStack()) {
						// System.out.println("Edge: " + Arrays.toString(i));
						for (int j = 0; j < searches.size(); j++) {
							if (searches.get(j)[0] == this.current[0] && ((searches.get(j)[1] == i[0]) || (searches.get(j)[1] == i[1]))) {
								searches.remove(j);
								j--;
							}
						}
						// searches.forEach((array) -> {
						// if (array[0] == this.current[0] && ((array[1] == i[0]) || (array[1] ==
						// i[1]))) {
						// searches.remove(array);
						// }
						// });
						if (!pathVertices.contains(graph.getVertices().get(i[0]))) {
							pathVertices.add(graph.getVertices().get(i[0]));
						}
						if (!pathVertices.contains(graph.getVertices().get(i[1]))) {
							pathVertices.add(graph.getVertices().get(i[1]));
						}
					}
				else {
					for (int j = 0; j < searches.size(); j++) {
						if (searches.get(j)[0] == this.current[0]
								&& ((searches.get(j)[1] == e.getVertices()[0].getKey()) || (searches.get(j)[1] == e.getVertices()[1].getKey()))) {
							searches.remove(j);
							j--;
						}
					}
					// searches.forEach((array) -> {
					// if (array[0] == this.current[0] && ((array[1] == e.getVertices()[0].getKey())
					// || (array[1] == e.getVertices()[1].getKey()))) {
					// searches.remove(array);
					// }
					// });
					pathVertices.add(e.getVertices()[0]);
					pathVertices.add(e.getVertices()[1]);
				}
				// System.out.print("Path: ");
				for (Vertex v : pathVertices) {
					if (v != graph.getVertices().get(this.current[0])) {
						inSet.add(v.getKey());
						for (Vertex nb : v.getNeighbors()) {
							if (v.getKey() == 33) {
								// System.out.println("Key 33 NB " + nb.getKey());
								// System.out.println(!pathVertices.contains(nb));
								// System.out.println(!inSet.contains(nb.getKey()));
							}
							if (!pathVertices.contains(nb) && !inSet.contains(nb.getKey())) {
								binaryInsertion(searches, new Integer[] { v.getKey(), nb.getKey(), v.getConnectingEdge(nb).getCost().get() });
							}
						}
						source.add(v);
						visited.put(v.getKey(), new ArrayList<>());
						visited.get(v.getKey()).add(v.getKey());
					}
				}
				// System.out.println();
			} else {
				// System.out.println("in here");
				for (Vertex nb : graph.getVertices().get(this.current[1]).getNeighbors()) {
					if (!visited.get(this.current[0]).contains(nb.getKey()) && !inSet.contains(nb.getKey())) {
						boolean add = true;
						for (int i = 0; i < searches.size(); i++)
							if (searches.get(i)[0] == this.current[0] && searches.get(i)[1] == nb.getKey()) {
								if (searches.get(i)[2] <= this.current[2]
										+ graph.getVertices().get(this.current[1]).getConnectingEdge(nb).getCost().get())
									add = false;
								else {
									searches.remove(i);
									i--;
								}
							}
						if (add)
							binaryInsertion(searches, new Integer[] { this.current[0], nb.getKey(),
									this.current[2] + graph.getVertices().get(this.current[1]).getConnectingEdge(nb).getCost().get() });
					}
				}
			}
		}
		// System.out.println(numTerminals + " " + graph.getNumberOfTerminals());

		return result;
	}

	public static void binaryInsertion(ArrayList<Integer[]> searches, Integer[] insert) {
		for (int i = 0; i < searches.size(); i++) {
			if (insert[2] <= searches.get(i)[2]) {
				searches.add(i, insert);
				return;
			}
		}
		searches.add(insert);

		// System.out.println("Binary");
		// for (Integer[] i : searches)
		// System.out.println(Arrays.toString(i));
		// System.out.println("new");
		// int upper = searches.size() - 1;
		// int lower = 0;
		// if (searches.size() == 0) {
		// searches.add(insert);
		// return;
		// }
		// while (true) {
		// System.out.println("Upper:" + upper + " Lower:" + lower);
		// if (upper == lower) {
		// if (searches.get(upper)[2] > insert[2])
		// searches.add(upper, insert);
		// else
		// searches.add(upper + 1, insert);
		//
		// for (Integer[] i : searches)
		// System.out.println(Arrays.toString(i));
		// return;
		// } else {
		// int index = (int) Math.ceil(lower + ((upper - lower) / 2.0));
		// System.out.println("index " + index);
		// // System.out.println(index);
		// if (searches.get(index)[2] > insert[2]) {
		// if (upper > index)
		// upper = index;
		// else {
		// searches.add(index, insert);
		// for (Integer[] i : searches)
		// System.out.println(Arrays.toString(i));
		// return;
		// }
		// } else
		// lower = index;
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
