/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Stack;

/**
 * Performs preprocessing on a given graph.
 *
 * @author Marciano Geijselaers
 * @author Joshua Scheidt
 */
public class PreProcess {

	UndirectedGraph graph;

	public PreProcess(UndirectedGraph g) {
		this.graph = g.clone();
	}

	public void removeTerminals() {
		HashMap<Integer, Vertex> vertices = this.graph.getVertices();
		Set keys = vertices.keySet();
		Iterator it = keys.iterator();
		Vertex current;
		while (it.hasNext()) {
			current = vertices.get((int) it.next());
			int counter = 0;
			Iterator neighbours = current.getNeighbors().iterator();
			while (neighbours.hasNext()) {
				if (((Vertex) neighbours.next()).isTerminal() && current.isTerminal()) {
					counter++;
				}
			}
			if (counter > 0)
				System.out.println("This Terminal has " + counter + " Terminal neighbours");
			if (current.getNeighbors().size() == 2 && ((Vertex) (current.getNeighbors().toArray()[0])).isTerminal()
					&& ((Vertex) (current.getNeighbors().toArray()[1])).isTerminal()) {
				System.out.println("This actually happens?");
			}
		}

	}

	/**
	 * This method looks more complicated than what it actually does. It removes all
	 * Non-terminals with degree 2. It iteratively checks its neighbours until it
	 * finds a Terminal or a Vertex with degree higher than 2. It has to keep track
	 * of subsumed vertices per New Edge. And it has to keep track of all Vertices
	 * to be removed and Edges to be created. This cannot happen concurrently as the
	 * Iterator doesn't allow it, if we do do this it could cause checks on newly
	 * created edges which is unnecessary.
	 */
	public void removeNonTerminalDegreeTwo() {
		Set keys = this.graph.getVertices().keySet();
		Iterator it = keys.iterator();
		HashMap<Integer, Vertex> vertices = this.graph.getVertices();
		Stack<double[]> subsumed;
		HashSet<Integer> toBeRemovedVertices = new HashSet<>();
		HashSet<Edge> toBeRemovedEdges = new HashSet<>();
		ArrayList<double[]> newEdges = new ArrayList<>();
		ArrayList<Stack<double[]>> containedWithinEdge = new ArrayList<>();
		Vertex current, firstVertex, secondVertex, tempVertex;
		Edge firstEdge, secondEdge, tempEdge, temp;
		double cost;
		while (it.hasNext()) {
			// Gets the current Vertex in the Iterator
			current = vertices.get((int) it.next());
			// Checks if Vertex is Non-Terminal and degree 2
			if (!current.isTerminal() && current.getNeighbors().size() == 2 && !toBeRemovedVertices.contains(current.getKey())) {
				// Creates a stack to be used for all vertices that will be subsumed by the to
				// be created Edge
				subsumed = new Stack<>();
				cost = 0;
				// Creating first steps left and right of current to iteratively find a terminal
				// or degree greater than 2
				firstVertex = (Vertex) current.getNeighbors().toArray()[0];
				secondVertex = current.getOtherNeighborVertex(firstVertex);
				firstEdge = current.getConnectingEdge(firstVertex);
				secondEdge = current.getConnectingEdge(secondVertex);
				// Pushes the original two removable Edges in the form of their two keys and
				// their respective costs
				subsumed.push(new double[] { current.getKey(), firstVertex.getKey(), firstEdge.getCost().get() });
				subsumed.push(new double[] { current.getKey(), secondVertex.getKey(), secondEdge.getCost().get() });
				// The total cost of the new Edge is the sum of the removed Edges
				cost += firstEdge.getCost().get() + secondEdge.getCost().get();
				// Keeps a list of the Vertices to be removed, Removal method will also remove
				// all connected
				// Edges so no need to store the Edge objects
				toBeRemovedVertices.add(current.getKey());
				while (!firstVertex.isTerminal() && firstVertex.getNeighbors().size() == 2) {
					// Tries the first side of the original Vertex until it finds a Vertex that
					// doesn't hold to the criteria of this method
					tempEdge = firstVertex.getOtherEdge(firstEdge);
					tempVertex = tempEdge.getOtherSide(firstVertex);
					subsumed.push(new double[] { firstVertex.getKey(), tempVertex.getKey(), tempEdge.getCost().get() });
					toBeRemovedVertices.add(firstVertex.getKey());
					cost += tempEdge.getCost().get();
					firstVertex = tempVertex;
					firstEdge = tempEdge;
				}
				while (!secondVertex.isTerminal() && secondVertex.getNeighbors().size() == 2) {
					// Tries the second side of the original Vertex until it finds a Vertex that
					// doesn't hold to the criteria of this method
					tempEdge = secondVertex.getOtherEdge(secondEdge);
					tempVertex = tempEdge.getOtherSide(secondVertex);
					subsumed.push(new double[] { secondVertex.getKey(), tempVertex.getKey(), tempEdge.getCost().get() });
					toBeRemovedVertices.add(secondVertex.getKey());
					cost += tempEdge.getCost().get();
					secondVertex = tempVertex;
					secondEdge = tempEdge;
				}
				// YOU NEED TO THINK ABOUT THIS MORE THERE COULD BE MULTIPLE NEW EDGES ADDED
				// THAT CREATE THE SAME PATH
				// COST SHOULD BE COMPARED TO SEE IF WE NEED TO REMOVE AN ORIGINAL EDGE OR NOT
				// ADD ANOTHER EDGE AT ALL
				boolean edgeExists = false;
				if (firstVertex.isNeighbor(secondVertex)) {
					if (cost > firstVertex.getConnectingEdge(secondVertex).getCost().get()) {
						// Do Nothing the vertices can all be removed because there exists a shorter
						// path between the two endpoints
					} else {
						temp = firstVertex.getConnectingEdge(secondVertex);
						temp.setCost(cost);
						temp.pushStack(subsumed);
					}
					edgeExists = true;
				} else {
					for (int i = 0; i < newEdges.size(); i++) {
						if (newEdges.get(i)[0] == firstVertex.getKey() && newEdges.get(i)[1] == secondVertex.getKey()) {
							if (newEdges.get(i)[2] > cost) {
								newEdges.get(i)[2] = cost;
							}
							edgeExists = true;
							break;
						} else if (newEdges.get(i)[0] == firstVertex.getKey() && newEdges.get(i)[1] == secondVertex.getKey()) {
							if (newEdges.get(i)[2] > cost) {
								newEdges.get(i)[2] = cost;
							}
							edgeExists = true;
							break;
						}
					}
				}
				if (!edgeExists) {
					newEdges.add(new double[] { firstVertex.getKey(), secondVertex.getKey(), cost });
					containedWithinEdge.add(subsumed);
				}
			}
		}
		for (int i = 0; i < newEdges.size(); i++) {
			temp = this.graph.addEdge((int) newEdges.get(i)[0], (int) newEdges.get(i)[1], newEdges.get(i)[2]);
			temp.pushStack(containedWithinEdge.get(i));
		}
		it = toBeRemovedVertices.iterator();
		while (it.hasNext()) {
			this.graph.removeVertex((int) it.next());
			it.remove();
		}
		it = toBeRemovedEdges.iterator();
		while (it.hasNext()) {
			this.graph.removeEdge((Edge) it.next());
		}
	}

	/**
	 * Removes Non-Terminal leaf nodes entirely as they will never be chosen (WE
	 * ASSUME THERE WILL BE NO NON-NEGATIVE EDGES) Removes Terminal leaf nodes and
	 * sets its neighbour to be a terminal to ensure connection
	 */
	public void removeLeafNodes() {
		Set keys = this.graph.getVertices().keySet();
		Iterator it = keys.iterator();
		HashMap<Integer, Vertex> vertices = this.graph.getVertices();
		Vertex current, newCurrent;
		while (it.hasNext()) {
			current = vertices.get((int) it.next());
			while (!current.isTerminal() && current.getNeighbors().size() == 1) {
				newCurrent = (Vertex) current.getNeighbors().toArray()[0];

				it.remove();
				this.graph.removeVertex(current.getKey());
				current = newCurrent;
			}
			while (current.isTerminal() && current.getNeighbors().size() == 1) {
				newCurrent = (Vertex) current.getNeighbors().toArray()[0];
				if (current.getSubsumed() != null) {
					if (current.getSubsumed().size() > 0) {
						newCurrent.pushStack(current.getSubsumed());
					}
				}
				newCurrent.pushSubsumed(
						new double[] { newCurrent.getKey(), current.getKey(), ((Edge) (current.getEdges().toArray()[0])).getCost().get() });
				this.graph.setTerminal(newCurrent.getKey());

				it.remove();
				this.graph.removeVertex(current.getKey());
				current = newCurrent;
			}
		}
	}

	// /**
	// * Completes the preprocess step for Tarjan's bridge finding algorithm.
	// *
	// * @param v
	// * The current Vertex
	// * @param parent
	// * The parent from the current Vertex (current from previous
	// * iteration)
	// *
	// * @author Joshua Scheidt
	// * @deprecated
	// */
	// @Deprecated
	// private void preorderTraversal(Vertex v, Vertex parent) {
	// this.iteratedValues[v.getKey() - 1] = this.count;
	// this.count++;
	// this.lowestFoundLabels[v.getKey() - 1] = this.iteratedValues[v.getKey() - 1];
	//
	// for (Vertex next : v.getNeighbors()) {
	// if (this.iteratedValues[next.getKey() - 1] == 0) {
	// this.preorderTraversal(next, v);
	//
	// this.lowestFoundLabels[v.getKey() - 1] =
	// Math.min(this.lowestFoundLabels[v.getKey() - 1],
	// this.lowestFoundLabels[next.getKey() - 1]);
	// if (this.lowestFoundLabels[next.getKey() - 1] ==
	// this.iteratedValues[next.getKey() - 1])
	// try {
	// this.bridges.add(this.graph.edgeBetweenVertices(v, next));
	// } catch (GraphException e) {
	// e.printStackTrace();
	// }
	// } else if (next != parent) {
	// this.lowestFoundLabels[v.getKey() - 1] =
	// Math.min(this.lowestFoundLabels[v.getKey() - 1],
	// this.lowestFoundLabels[next.getKey() - 1]);
	// }
	// }
	// }
	/**
	 * Depth-first search through the graph starting from the inputted vertex.
	 *
	 * @param v
	 *            The starting vertex
	 *
	 * @author Joshua Scheidt
	 */
	private void dfs(Vertex v) {
		int count = 1;
		int[] iteratedValues = new int[this.graph.getVertices().size()];
		int[] lowestFoundLabels = new int[this.graph.getVertices().size()];
		ArrayList<Edge> bridges = new ArrayList<>();
		Stack<Vertex> stack = new Stack<>();
		Vertex next;
		Iterator<Vertex> it;
		stack.push(v);
		iteratedValues[v.getKey() - 1] = count;
		count++;

		while (!stack.isEmpty()) {
			it = stack.peek().getNeighbors().iterator();
			while ((next = it.next()) != null) {
				if (iteratedValues[next.getKey() - 1] == 0) {
					iteratedValues[next.getKey() - 1] = count;
					count++;
					stack.push(next);
					break;
				} else if (!it.hasNext()) {
					stack.pop();
					break;
				}
			}
		}
		// System.out.println(Arrays.toString(iteratedValues));
	}

	/**
	 * Performs Tarjan's bridge finding algorithm. This method does NOT use
	 * recursions to decrease heap size.
	 *
	 * @param v
	 *            The starting vertex
	 *
	 * @author Joshua Scheidt
	 */
	private ArrayList<Edge> TarjansBridgeFinding(Vertex v0, int totalVertices) {
		int count = 1;
		int[] iteratedValues = new int[totalVertices];
		int[] lowestFoundLabels = new int[totalVertices];
		ArrayList<Edge> bridges = new ArrayList<>();
		Stack<Vertex> stack = new Stack<>();
		Vertex fake = new Vertex(0);
		stack.push(fake);
		stack.push(v0);
		iteratedValues[v0.getKey() - 1] = count;
		lowestFoundLabels[v0.getKey() - 1] = count;
		count++;
		Vertex current, parent, next;
		Iterator<Vertex> it;
		boolean backtracking = false;

		while (stack.size() > 1) {
			current = stack.pop();
			parent = stack.peek();
			stack.push(current);
			it = current.getNeighbors().iterator();
			backtracking = true;
			for (Vertex neighbor : current.getNeighbors()) {
				if (iteratedValues[neighbor.getKey() - 1] == 0) { // If any neighbor is unexplored, don't backtrack.
					backtracking = backtracking && false;
				}
			}
			if (!backtracking) { // We still have unexplored neighbors
				while ((next = it.next()) != null) {
					if (iteratedValues[next.getKey() - 1] == 0) { // Find the unexplored neighbor
						iteratedValues[next.getKey() - 1] = count;
						lowestFoundLabels[next.getKey() - 1] = count;
						count++;
						stack.push(next);
						break;
					}
					if (!it.hasNext()) { // Should never get it here, would mean there is something wrong with unexplored
						// neighbors check
						System.out.println("Still got in here");
						break;
					}
				}
			} else { // All neighbors explored
				while ((next = it.next()) != null) {
					if (next != parent) {
						lowestFoundLabels[current.getKey() - 1] = Math.min(lowestFoundLabels[current.getKey() - 1],
								lowestFoundLabels[next.getKey() - 1]); // Set current lowest to go to lowest neighbor
					}
					if (!it.hasNext()) {
						if (lowestFoundLabels[current.getKey() - 1] == iteratedValues[current.getKey() - 1] && parent != fake) {
							try {
								// System.out.println("New edge:" + current.getKey() + " " + parent.getKey());
								bridges.add(this.graph.edgeBetweenVertices(current, parent));
							} catch (GraphException e) {
								e.printStackTrace();
							}
						}
						stack.pop();
						break;
					}
				}
			}

		}
		return bridges;
	}

	/**
	 * Analyses the two neighboring sections of a bridge to check for the
	 * possibility of removal of the sections. For now, it will only do something to
	 * the graph if one of the following cases holds:
	 * <ul>
	 * <li>The section is a leaf-section without terminals, then remove the
	 * section+bridge</li>
	 * <li>The section is a leaf-section with 1 terminal, then make a shortest path
	 * from the bridge to the terminal, remove everything else</li>
	 * <li>The section is connected to multiple bridges without terminals, then make
	 * shortest paths between bridges if the number of edges would be lower than the
	 * new number of edges</li>
	 * <li>The section is connected to multiple bridges with terminals, then make
	 * shortest paths between bridges and terminals if the number of edges would be
	 * lower than the new number of edges</li>
	 * </ul>
	 *
	 * @param bridges
	 *            All the found bridges in the graph.
	 * @return The number of terminals
	 *
	 * @author Joshua Scheidt
	 */
	private void analyseSections(ArrayList<Edge> bridges, int totalVertices) {
		UndirectedGraph bridgeCutted = this.graph.clone();
		for (Edge bridge : bridges) {
			bridgeCutted.removeEdge(bridgeCutted.getVertices().get(bridge.getVertices()[0].getKey())
					.getConnectingEdge(bridgeCutted.getVertices().get(bridge.getVertices()[1].getKey())));
		}

		int nrBridges = 0;
		boolean[] hasVisited;
		HashSet<Integer> bridgesOrTerms; // Save all the found bridges and terminals from current section
		Stack<Vertex> stack = new Stack<>();
		Vertex next;
		Iterator<Vertex> it;
		ArrayList<Integer> checkedVertices = new ArrayList<>(); // If multiple bridges are in same section, don't check section multiple times

		ArrayList<Integer> allBridgeEndpoints = new ArrayList<>();
		for (Edge bridge : bridges) {
			for (Vertex endPoint : bridge.getVertices()) {
				allBridgeEndpoints.add(endPoint.getKey());
			}
		}

		Set<Vertex> verticesInSection = new HashSet<>();
		Set<Vertex> terminalsInSection = new HashSet<>();
		Set<Vertex> bridgeEndpointsInSection = new HashSet<>();
		Set<Edge> edgesInSection = new HashSet<>();

		for (Edge bridge : bridges) {
			for (Vertex endPoint : bridge.getVertices()) { // run 2 times
				if (!checkedVertices.contains(endPoint.getKey())) {
					checkedVertices.add(endPoint.getKey());
					nrBridges = 1;
					verticesInSection = new HashSet<>();
					terminalsInSection = new HashSet<>();
					bridgeEndpointsInSection = new HashSet<>();
					edgesInSection = new HashSet<>();
					bridgeEndpointsInSection.add(endPoint);

					for (Vertex other : endPoint.getNeighbors())
						if (!allBridgeEndpoints.contains(other.getKey())) {
							edgesInSection.add(endPoint.getConnectingEdge(other));
							if (!other.isTerminal())
								verticesInSection.add(other);
							else
								terminalsInSection.add(other);
						}
					hasVisited = new boolean[totalVertices];
					bridgesOrTerms = new HashSet<>();
					bridgesOrTerms.add(endPoint.getKey());

					stack.push(endPoint);
					hasVisited[endPoint.getKey() - 1] = true;
					while (!stack.isEmpty()) {
						it = stack.peek().getNeighbors().iterator();
						if (!it.hasNext()) {
							stack.pop();
							continue;
						}
						while ((next = it.next()) != null) {
							if (allBridgeEndpoints.contains(stack.peek().getKey()) && allBridgeEndpoints.contains(next.getKey())
									&& bridges.contains(stack.peek().getConnectingEdge(next))) {
								if (it.hasNext())
									continue;
								else {
									stack.pop();
									break;
								}
							} else if (!hasVisited[next.getKey() - 1]) {
								hasVisited[next.getKey() - 1] = true;
								if (!allBridgeEndpoints.contains(next.getKey()) && !next.isTerminal())
									verticesInSection.add(next);
								for (Vertex nb : next.getNeighbors()) {
									if (!allBridgeEndpoints.contains(next.getKey()) || !allBridgeEndpoints.contains(nb.getKey())
											|| !bridges.contains(next.getConnectingEdge(nb)))
										edgesInSection.add(next.getConnectingEdge(nb));
								}

								if (next.isTerminal() || allBridgeEndpoints.contains(next.getKey())) {
									if (next.isTerminal())
										terminalsInSection.add(next);
									if (allBridgeEndpoints.contains(next.getKey())) {
										nrBridges++;
										bridgeEndpointsInSection.add(next);
										checkedVertices.add(next.getKey());
									}
									bridgesOrTerms.add(next.getKey());
								}
								stack.push(next);
								break;
							} else if (!it.hasNext()) {
								stack.pop();
								break;
							}
							// System.out.println(it.hasNext());
						}
					}
					// System.out.println("Done");

					// System.out.println("EndPoint " + endPoint.getKey());
					// System.out.println("bridges " + nrBridges);
					// System.out.println("terminals " + terminalsInSection.size());
					// System.out.println("Found vertices:");
					// for (Vertex i : verticesInSection)
					// System.out.print(i.getKey() + " ");
					// System.out.println("\nFound terminals:");
					// for (Vertex i : terminalsInSection)
					// System.out.print(i.getKey() + " ");
					// System.out.println("\nFound bridge endpoints:");
					// for (Vertex i : bridgeEndpointsInSection)
					// System.out.print(i.getKey() + " ");
					// System.out.println("\nFound edges:");
					// for (Edge i : edgesInSection)
					// System.out.print(i.getVertices()[0].getKey() + "-" +
					// i.getVertices()[1].getKey() + " ");
					// System.out.println();

					if (((terminalsInSection.size() * (terminalsInSection.size() - 1)) / 2 + (nrBridges) * (bridgesOrTerms.size() - nrBridges)
							+ (nrBridges * (nrBridges - 1) / 2)) <= edgesInSection.size()) {
						ArrayList<Vertex> v = new ArrayList<>();
						v.addAll(verticesInSection);
						ArrayList<Vertex> t = new ArrayList<>();
						t.addAll(terminalsInSection);
						ArrayList<Vertex> b = new ArrayList<>();
						b.addAll(bridgeEndpointsInSection);
						ArrayList<Edge> e = new ArrayList<>();
						e.addAll(edgesInSection);
						this.reduceSection(bridgeCutted, v, t, b, e);
					}
					// Else leave as is, probably shorter to perform normal algorithm than to change
				}
			}
		}
	}

	/**
	 * Reduces the size of a section after using an analysis on what can be removed
	 * and what has to be connected. The assumption is made that, when this method
	 * is called, it will always be to reduce the size of the graph.
	 *
	 * @param vertices
	 *            The set of vertices that can be removed.
	 * @param terminals
	 *            The set of terminals in the section.
	 * @param bridgeEndpoints
	 *            The set of bridge endpoints in the section.
	 * @param edges
	 *            The set of edges that can be removed.
	 *
	 * @author Joshua Scheidt
	 */
	public void reduceSection(UndirectedGraph cutted, ArrayList<Vertex> vertices, ArrayList<Vertex> terminals, ArrayList<Vertex> bridgeEndpoints,
			ArrayList<Edge> edges) {
		// Create new edges between bridges
		ArrayList<Edge> toBeAddedEdges = new ArrayList<>();
		ArrayList<Vertex> ends;
		for (int i = 0; i < bridgeEndpoints.size(); i++) {
			ends = new ArrayList<>();
			for (int j = i + 1; j < bridgeEndpoints.size(); j++) {
				ends.add(bridgeEndpoints.get(j));
			}
			ArrayList<Edge> tmp = (ends.size() > 0 ? PathFinding.DijkstraMultiPath(cutted, bridgeEndpoints.get(i), ends) : new ArrayList<>());
			toBeAddedEdges.addAll(tmp);
		}
		// Create new edges between terminals
		for (int i = 0; i < terminals.size(); i++) {
			ends = new ArrayList<>();
			for (int j = i + 1; j < terminals.size(); j++) {
				ends.add(terminals.get(j));
			}
			ArrayList<Edge> tmp = (ends.size() > 0 ? PathFinding.DijkstraMultiPath(cutted, terminals.get(i), ends) : new ArrayList<>());
			toBeAddedEdges.addAll(tmp);
		}

		// Create new edges between the terminals and bridges
		for (int i = 0; i < bridgeEndpoints.size(); i++) {
			ends = new ArrayList<>();
			for (int j = 0; j < terminals.size(); j++) {
				if (bridgeEndpoints.get(i) == terminals.get(j))
					continue;
				ends.add(terminals.get(j));
			}
			ArrayList<Edge> tmp = (ends.size() > 0 ? PathFinding.DijkstraMultiPath(cutted, bridgeEndpoints.get(i), ends) : new ArrayList<>());
			toBeAddedEdges.addAll(tmp);
		}

		// Remove all vertices and edges which are now not needed anymore
		for (Edge e : edges)
			this.graph.removeEdge(e);

		for (Vertex v : vertices)
			this.graph.removeVertex(v.getKey());

		for (Edge e : toBeAddedEdges)
			this.graph.addEdge(e);

		// if (toBeAddedEdges.size() == 0 && bridgeEndpoints.size() == 1) {
		// System.out.println("removing");
		// this.graph.removeVertex(bridgeEndpoints.get(0).getKey());
		// }
	}

	/**
	 * Removes possible sections without terminals and/or sections containing only
	 * single terminals.
	 *
	 * @return All the bridges in the graph.
	 *
	 * @author Joshua Scheidt
	 */
	public void removeBridgesAndSections(int totalVertices) {
		ArrayList<Edge> bridges = this.TarjansBridgeFinding(this.graph.getVertices().get(this.graph.getVertices().keySet().toArray()[0]),
				totalVertices);
		this.analyseSections(bridges, totalVertices);
	}
}
