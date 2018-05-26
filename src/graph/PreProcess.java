/*
j * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graph;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
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
	int[] range;
	private HashMap<Integer, BitSet> checked;

	public PreProcess(UndirectedGraph g) {
		this.graph = g.clone();
		this.range = new int[] { Integer.MAX_VALUE, Integer.MIN_VALUE };
		this.checked = new HashMap<>();
		Iterator it = this.graph.getVertices().keySet().iterator();
		BitSet allFalse;
		while (it.hasNext()) {
			allFalse = new BitSet(4);
			allFalse.set(0, 4);
			allFalse.flip(0, allFalse.length() - 1);
			this.checked.put((int) it.next(), allFalse);
		}
		// for (Vertex v : this.graph.getVertices().values()) {
		// System.out.println(this.checked.get(v.getKey()).cardinality() + " " +
		// this.checked.get(v.getKey()).length());
		// System.out.println(this.checked.get(v.getKey()).get(0));
		// System.out.println(this.checked.get(v.getKey()).get(1));
		// System.out.println(this.checked.get(v.getKey()).get(2));
		// }
	}

	public void rangeCheck() {
		this.graph.getEdges().forEach((e) -> {
			if (e.getCost().get() < this.range[0]) {
				this.range[0] = e.getCost().get();
			} else if (e.getCost().get() > this.range[1]) {
				this.range[1] = e.getCost().get();
			}
		});
		System.out.println("Range: [" + this.range[0] + ", " + this.range[1] + "]");
	}

	/**
	 * The following method checks each clique of size three and sees if any sum of
	 * two edges is smaller than the third. If that is the case the third edge can
	 * be removed.
	 */
	public void cliqueEdgeRemoval() {
		HashSet<HashSet<Integer>> cliques = new HashSet<>();
		HashSet<Integer> clique;
		HashSet<Edge> toBeRemoved = new HashSet<>();
		Edge vn, vc, nc;
		// Finding all unique cliques
		for (Vertex v : this.graph.getVertices().values()) {
			if (!(this.checked.get(v.getKey()).cardinality() == this.checked.get(v.getKey()).length())) {
				for (Vertex n : v.getNeighbors()) {
					if (!(this.checked.get(n.getKey()).cardinality() == this.checked.get(n.getKey()).length())) {
						for (Vertex c : n.getNeighbors()) {
							if (!(this.checked.get(c.getKey()).cardinality() == this.checked.get(c.getKey()).length())) {
								if (c.isNeighbor(v)) {
									clique = new HashSet<>();
									clique.add(v.getKey());
									clique.add(n.getKey());
									clique.add(c.getKey());
									if (!cliques.contains(clique)) {
										cliques.add(clique);
										vn = this.graph.getVertices().get(v.getKey()).getConnectingEdge(this.graph.getVertices().get(n.getKey()));
										vc = this.graph.getVertices().get(v.getKey()).getConnectingEdge(this.graph.getVertices().get(c.getKey()));
										nc = this.graph.getVertices().get(n.getKey()).getConnectingEdge(this.graph.getVertices().get(c.getKey()));
										if ((vn.getCost().get() + vc.getCost().get()) <= nc.getCost().get()) {
											toBeRemoved.add(nc);
											System.out.println("Removes Edge from clique: [" + nc.getVertices()[0].getKey() + ", "
													+ nc.getVertices()[1].getKey() + "]");
										} else if ((vn.getCost().get() + nc.getCost().get()) <= vc.getCost().get()) {
											toBeRemoved.add(vc);
											System.out.println("Removes Edge from clique: [" + vc.getVertices()[0].getKey() + ", "
													+ vc.getVertices()[1].getKey() + "]");
										} else if ((vc.getCost().get() + nc.getCost().get()) <= vn.getCost().get()) {
											toBeRemoved.add(vn);
											System.out.println("Removes Edge from clique: [" + vn.getVertices()[0].getKey() + ", "
													+ vn.getVertices()[1].getKey() + "]");
										} else {
											System.out.println("Clique doesn't support Edge Removal");
										}
									}
								}
							}
						}
					}
				}
				this.checked.get(v.getKey()).set(2);
			}
		}
		for (Edge e : toBeRemoved) {
			this.graph.removeEdge(e);
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
		Stack<int[]> subsumed;
		HashSet<Integer> toBeRemovedVertices = new HashSet<>();
		HashSet<Edge> toBeRemovedEdges = new HashSet<>();
		ArrayList<int[]> newEdges = new ArrayList<>();
		ArrayList<Stack<int[]>> containedWithinEdge = new ArrayList<>();
		Vertex current, firstVertex, secondVertex, tempVertex;
		Edge firstEdge, secondEdge, tempEdge, temp;
		int cost, currentKey;

		while (it.hasNext()) {
			// Gets the current Vertex in the Iterator
			currentKey = (int) it.next();
			current = vertices.get(currentKey);
			// Checks if Vertex is Non-Terminal and degree 2
			if (!(this.checked.get(currentKey).cardinality() == this.checked.get(currentKey).length())) {
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
					subsumed.push(new int[] { current.getKey(), firstVertex.getKey(), firstEdge.getCost().get() });
					subsumed.push(new int[] { current.getKey(), secondVertex.getKey(), secondEdge.getCost().get() });
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
						subsumed.push(new int[] { firstVertex.getKey(), tempVertex.getKey(), tempEdge.getCost().get() });
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
						subsumed.push(new int[] { secondVertex.getKey(), tempVertex.getKey(), tempEdge.getCost().get() });
						toBeRemovedVertices.add(secondVertex.getKey());
						cost += tempEdge.getCost().get();
						secondVertex = tempVertex;
						secondEdge = tempEdge;
					}
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
						newEdges.add(new int[] { firstVertex.getKey(), secondVertex.getKey(), cost });
						containedWithinEdge.add(subsumed);
					}
				} else {
					this.checked.get(currentKey).set(1);
				}
			}
		}
		for (int i = 0; i < newEdges.size(); i++) {
			this.checked.get(newEdges.get(i)[0]).set(1, false);
			this.checked.get(newEdges.get(i)[1]).set(1, false);
			temp = this.graph.addEdge(newEdges.get(i)[0], newEdges.get(i)[1], newEdges.get(i)[2]);
			temp.pushStack(containedWithinEdge.get(i));
		}
		it = toBeRemovedVertices.iterator();
		while (it.hasNext()) {
			currentKey = (int) it.next();
			this.checked.remove(currentKey);
			this.graph.removeVertex(this.graph.getVertices().get(currentKey));
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
		Iterator it = this.graph.getVertices().keySet().iterator();
		HashMap<Integer, Vertex> vertices = this.graph.getVertices();
		HashSet<Vertex> toBeRemoved = new HashSet<>();
		Vertex current, newCurrent, temp;
		int currentKey;
		System.out.println("starting");

		while (it.hasNext()) {
			currentKey = (int) it.next();
			if (!(this.checked.get(currentKey).cardinality() == this.checked.get(currentKey).length())) {
				current = vertices.get(currentKey);
				if (!current.isTerminal() && current.getNeighbors().size() == 1) {
					toBeRemoved.add(current);
					newCurrent = (Vertex) current.getNeighbors().toArray()[0];
					while (!newCurrent.isTerminal() && newCurrent.getNeighbors().size() == 2) {
						temp = newCurrent.getOtherNeighborVertex(current);
						current = newCurrent;
						newCurrent = temp;
						toBeRemoved.add(current);
					}
					this.checked.get(newCurrent.getKey()).set(0, false);
				} else if (current.isTerminal() && current.getNeighbors().size() == 1) {
					toBeRemoved.add(current);
					newCurrent = (Vertex) current.getNeighbors().toArray()[0];
					while (newCurrent.isTerminal() && newCurrent.getNeighbors().size() == 2) {
						temp = newCurrent.getOtherNeighborVertex(current);
						current = newCurrent;
						newCurrent = temp;
						if (current.getSubsumed() != null) {
							if (current.getSubsumed().size() > 0) {
								newCurrent.pushStack(current.getSubsumed());
							}
						}
						newCurrent.pushSubsumed(
								new int[] { newCurrent.getKey(), current.getKey(), ((Edge) (current.getEdges().toArray()[0])).getCost().get() });
						this.graph.setTerminal(newCurrent.getKey());
						current = newCurrent;
						toBeRemoved.add(current);
					}
					System.out.println(newCurrent.getKey());
					for (Vertex nb : newCurrent.getNeighbors())
						System.out.print(nb.getKey() + " ");
					System.out.println();
					System.out.println(this.graph.getVertices().containsKey(newCurrent.getKey()));
					System.out.println(this.checked.get(newCurrent.getKey()));
					this.checked.get(newCurrent.getKey()).set(0, false);
				} else {
					this.checked.get(currentKey).set(0);
				}
			}
		}
		it = toBeRemoved.iterator();
		while (it.hasNext()) {
			current = (Vertex) it.next();
			System.out.println(current.getKey());
			this.checked.remove(current.getKey());
			this.graph.removeVertex(current);
		}
	}

	/**
	 * Finds and returns all articulation points in the graph.
	 *
	 * @param v0
	 *            The starting vertex
	 * @param totalVertices
	 *            The total amount of vertices in the graph
	 * @return A list of Vertices
	 *
	 * @author Joshua Scheidt
	 */
	public HashSet<Vertex> articulationPointFinding(Vertex v0, int totalVertices) {
		int count = 1;
		int[] iteratedValues = new int[totalVertices];
		int[] lowestFoundLabels = new int[totalVertices];
		HashSet<Vertex> articulationBridge = new HashSet<>();
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
							articulationBridge.add(current);
							articulationBridge.add(parent);
						} else if (parent != fake && lowestFoundLabels[current.getKey() - 1] >= iteratedValues[parent.getKey() - 1]) {
							articulationBridge.add(parent);
						}
						stack.pop();
						break;
					}
				}
			}
		}
		if (v0.getNeighbors().size() > 1) {
			int val = iteratedValues[v0.getKey() - 1];
			boolean remove = true;
			for (Vertex v : v0.getNeighbors()) {
				if (lowestFoundLabels[v.getKey() - 1] != val) {
					remove = false;
					break;
				}
			}
			if (remove)
				articulationBridge.remove(v0);
		}

		return articulationBridge;
	}

	/**
	 * Performs an analysis on the section to check which vertices, terminals,
	 * articulation points and edges lie within a section, and will afterwards call
	 * shortest path if it reduces the number of edges.
	 *
	 * @param artiPoints
	 *            The articulation points in the graph
	 *
	 * @author Joshua Scheidt
	 */
	public void analyseSections(HashSet<Vertex> artiPoints) {
		// Map to keep track of all the already visited vertices in the set.
		// These will hold only the vertices which are not articulation points.
		Map<Vertex, Boolean> hasVisited;
		// The stack used for replacement of recursion
		Stack<Vertex> stack = new Stack<>();
		// The next and parent vertices in the stack
		Vertex next, parent;
		// Temporary vertex used for moving other vertices around
		Vertex tmp;
		// The iterator of the neighbours of the stack's last element
		Iterator<Vertex> it;
		// Map in a Map which shows for every articulation point which neighbours have
		// been checked (true=checked)
		Map<Vertex, Map<Vertex, Boolean>> artiNbCheck = new HashMap<>();
		for (Vertex v : artiPoints) {
			Map<Vertex, Boolean> map = new HashMap<>();
			for (Vertex nb : v.getNeighbors()) {
				map.put(nb, false);
			}
			artiNbCheck.put(v, map);
		}

		// VerticesInSection, TerminalsInSection, ArticulationInSection and
		// EdgesInSection
		// If a terminals is an articulation as well, it will not be added to tis.
		Set<Vertex> vis, tis, ais;
		Set<Edge> eis;

		for (Vertex arti : artiPoints) {
			stack.push(arti);
			parent = arti;
			checkArti: while (artiNbCheck.get(arti).values().contains(false)) {
				hasVisited = new HashMap<>();
				vis = new HashSet<>();
				tis = new HashSet<>();
				ais = new HashSet<>();
				eis = new HashSet<>();
				ais.add(arti);

				nbCheck: for (Vertex nb : arti.getNeighbors()) {
					if (artiNbCheck.get(arti).containsKey(nb) && !artiNbCheck.get(arti).get(nb)) {

						// The neighbour of the articulationPoint is an articulation as well.
						// Do nothing
						if (artiPoints.contains(nb)) {
							artiNbCheck.get(arti).put(nb, true);
							artiNbCheck.get(nb).put(arti, true);
						} else { // Found new unvisited neighbour
							stack.push(nb);
							artiNbCheck.get(arti).put(nb, true);
							if (nb.isTerminal())
								tis.add(nb);
							else
								vis.add(nb);
							hasVisited.put(nb, true);
							eis.add(arti.getConnectingEdge(nb));
							break nbCheck;
						}
					}
				}
				if (stack.size() > 2) {
					System.out.println("Something went wrong in the neighbour checking");
					System.exit(1);
				} else if (stack.size() == 1) {
					break checkArti;
				} else { // Stack now contains arti and 1 neighbour
					while (stack.size() > 1) { // If size is 1, then only arti available
						it = stack.peek().getNeighbors().iterator();
						if (!it.hasNext()) { // Current item on the stack has no neighbours
							System.out.println("The current stack item does not have any neighbours");
							System.exit(1);
						}
						nbLoop: while (it.hasNext()) {
							next = it.next();
							if (parent == next) { // Found parent again, do nothing
								// System.out.println("Parent found");
							} else if (artiPoints.contains(next) && !artiNbCheck.get(next).get(stack.peek())) { // My next vertex is an arti point
								artiNbCheck.get(next).put(stack.peek(), true);
								ais.add(next);
								eis.add(next.getConnectingEdge(stack.peek()));
							} else if (!eis.contains(stack.peek().getConnectingEdge(next))) { // Current edge not in set yet
								eis.add(stack.peek().getConnectingEdge(next));
								if (!hasVisited.containsKey(next) || !hasVisited.get(next)) { // Neighbour is unvisited
									hasVisited.put(next, true);
									if (next.isTerminal())
										tis.add(next);
									else {
										vis.add(next);
									}
									parent = stack.peek();
									stack.push(next);
									break nbLoop;
								}
							}
							if (!it.hasNext()) {
								stack.pop(); // Iterator went through all neighbours, thus backtrack.
								tmp = stack.pop();
								parent = (stack.size() == 0 ? arti : stack.peek());
								stack.push(tmp);
								break nbLoop;
							}
						}
					}
					// We are back to articulation point, thus end of section
					if (((tis.size() * (tis.size() - 1) / 2) + (ais.size() * (ais.size() - 1) / 2) + (tis.size() * ais.size())) < eis.size()) {
						ArrayList<Vertex> v = new ArrayList<>();
						v.addAll(vis);
						ArrayList<Vertex> t = new ArrayList<>();
						t.addAll(tis);
						ArrayList<Vertex> b = new ArrayList<>();
						b.addAll(ais);
						ArrayList<Edge> e = new ArrayList<>();
						e.addAll(eis);
						this.reduceSection(v, t, b, e);
					}
				}

			}
			stack.removeAllElements();
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
	public void reduceSection(ArrayList<Vertex> vertices, ArrayList<Vertex> terminals, ArrayList<Vertex> articulations, ArrayList<Edge> edges) {
		ArrayList<Edge> toBeAddedEdges = new ArrayList<>();
		ArrayList<Vertex> ends;
		for (int i = 0; i < articulations.size(); i++) {
			ends = new ArrayList<>();
			for (int j = i + 1; j < articulations.size(); j++) {
				ends.add(articulations.get(j));
			}
			ArrayList<Edge> tmp = (ends.size() > 0 ? PathFinding.DijkstraMultiPath(this.graph, articulations.get(i), ends, edges)
					: new ArrayList<>());
			toBeAddedEdges.addAll(tmp);
		}
		// Create new edges between terminals
		for (int i = 0; i < terminals.size(); i++) {
			ends = new ArrayList<>();
			for (int j = i + 1; j < terminals.size(); j++) {
				ends.add(terminals.get(j));
			}
			ArrayList<Edge> tmp = (ends.size() > 0 ? PathFinding.DijkstraMultiPath(this.graph, terminals.get(i), ends, edges) : new ArrayList<>());
			toBeAddedEdges.addAll(tmp);
		}

		// Create new edges between the terminals and bridges
		for (int i = 0; i < articulations.size(); i++) {
			ends = new ArrayList<>();
			for (int j = 0; j < terminals.size(); j++) {
				if (articulations.get(i) == terminals.get(j))
					continue;
				ends.add(terminals.get(j));
			}
			ArrayList<Edge> tmp = (ends.size() > 0 ? PathFinding.DijkstraMultiPath(this.graph, articulations.get(i), ends, edges)
					: new ArrayList<>());
			toBeAddedEdges.addAll(tmp);
		}

		// Remove all vertices and edges which are now not needed anymore
		for (Edge e : edges) {
			if (!e.getVertices()[0].isTerminal() && !e.getVertices()[1].isTerminal() && !articulations.contains(e.getVertices()[0])
					&& !articulations.contains(e.getVertices()[1]))
				this.graph.removeEdge(e);
		}

		for (Vertex v : vertices) {
			this.graph.removeVertex(v);
		}

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
		long start = System.currentTimeMillis();
		// ArrayList<Edge> bridges =
		// this.TarjansBridgeFinding(this.graph.getVertices().get(this.graph.getVertices().keySet().toArray()[0]),
		// totalVertices);
		HashSet<Vertex> sectionPoints = this.articulationPointFinding(this.graph.getVertices().get(this.graph.getVertices().keySet().toArray()[0]),
				totalVertices);
		long middle = System.currentTimeMillis();
		this.analyseSections(sectionPoints);
		// this.analyseSections(bridges, totalVertices);
		// this.analyseSectionsPoints(sectionPoints, totalVertices);
		System.out.println("Step 1:" + (middle - start) + " ms");
		System.out.println("Step 2:" + (System.currentTimeMillis() - middle) + " ms");
	}

	/**
	 * Below method was made to 'hide' terminals which had 2 neighbours which were
	 * also terminals However currently this doesn't happen in any graph
	 */
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
			if (counter > 0) {
				System.out.println("This Terminal has " + counter + " Terminal neighbours");
			}
			if (current.getNeighbors().size() == 2 && ((Vertex) (current.getNeighbors().toArray()[0])).isTerminal()
					&& ((Vertex) (current.getNeighbors().toArray()[1])).isTerminal()) {
				System.out.println("This actually happens?");
			}
		}

	}
}
