/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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

	public void removeNonTerminalDegreeTwo() {
		Set keys = this.graph.getVertices().keySet();
		Iterator it = keys.iterator();
		HashMap<Integer, Vertex> vertices = this.graph.getVertices();
		Vertex current, firstVertex, secondVertex, tempVertex;
		Edge firstEdge, secondEdge, tempEdge = null;
		Stack<double[]> subsumed;
		ArrayList<Integer> toBeRemoved = new ArrayList<>();
		int cost = 0;
		while (it.hasNext()) {
			current = vertices.get((int) it.next());
			if (!current.isTerminal() && current.getNeighbors().size() == 2) {
				System.out.println("Enters Loop");
				subsumed = new Stack<>();
				firstVertex = (Vertex) current.getNeighbors().toArray()[0];
				secondVertex = (Vertex) current.getNeighbors().toArray()[1];
				firstEdge = current.getAdjoinedEdge(firstVertex);
				secondEdge = current.getAdjoinedEdge(secondVertex);
				subsumed.push(new double[] { current.getKey(), firstVertex.getKey(), firstEdge.getCost().get() });
				subsumed.push(new double[] { current.getKey(), secondVertex.getKey(), secondEdge.getCost().get() });
				cost += firstEdge.getCost().get() + secondEdge.getCost().get();
				toBeRemoved.add(current.getKey());
				while (!firstVertex.isTerminal() && firstVertex.getNeighbors().size() == 2) {
					tempVertex = firstVertex.getOtherEdge(firstEdge).getOtherSide(firstVertex);
					System.out.println("tempEdge: " + tempEdge);
					System.out.println(tempVertex.getOtherEdge(firstEdge));
					tempEdge = tempVertex.getOtherEdge(firstEdge);
					System.out.println("tempEdge: " + tempEdge);
					subsumed.push(new double[] { firstVertex.getKey(), tempVertex.getKey(), tempEdge.getCost().get() });
					toBeRemoved.add(firstVertex.getKey());
					firstVertex = tempVertex;
					firstEdge = tempEdge;
				}
				while (!secondVertex.isTerminal() && secondVertex.getNeighbors().size() == 2) {
					tempVertex = secondVertex.getOtherEdge(secondEdge).getOtherSide(secondVertex);
					tempEdge = tempVertex.getOtherEdge(secondEdge);
					subsumed.push(new double[] { secondVertex.getKey(), tempVertex.getKey(), tempEdge.getCost().get() });
					toBeRemoved.add(secondVertex.getKey());
					secondVertex = tempVertex;
					secondEdge = tempEdge;
				}
				this.graph.addEdge(firstVertex, secondVertex, cost);

				for (int key : toBeRemoved) {
					this.graph.removeVertex(key);
				}
			}
		}
	}

	/**
	 * Removes Non-Terminal leaf nodes entirely as they will never be chosen (WE
	 * ASSUME THERE WILL BE NO NON-NEGATIVE EDGES) Removes Terminal leaf nodes and
	 * sets its neighbor to be a terminal to ensure connection
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
				this.graph.removeVertex(current);
				current = newCurrent;
			}
			while (current.isTerminal() && current.getNeighbors().size() == 1) {
				newCurrent = (Vertex) current.getNeighbors().toArray()[0];
				newCurrent.pushSubsumed(
						new double[] { newCurrent.getKey(), current.getKey(), ((Edge) (current.getEdges().toArray()[0])).getCost().get() });
				this.graph.setTerminal(newCurrent.getKey());

				it.remove();
				this.graph.removeVertex(current);
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
		System.out.println(Arrays.toString(iteratedValues));
	}

	/**
	 * Completes the preprocess step for Tarjan's bridge finding algorithm. This
	 * method does NOT use recursions to decrease heap size.
	 *
	 * @param v
	 *            The starting vertex
	 *
	 * @author Joshua Scheidt
	 */
	private ArrayList<Edge> preorderTraversal(Vertex v0) {
		int count = 1;
		int[] iteratedValues = new int[this.graph.getVertices().size()];
		int[] lowestFoundLabels = new int[this.graph.getVertices().size()];
		ArrayList<Edge> bridges = new ArrayList<>();
		count = 1;
		iteratedValues = new int[this.graph.getVertices().size()];
		lowestFoundLabels = new int[this.graph.getVertices().size()];
		bridges = new ArrayList<>();
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
					if (next != parent)
						lowestFoundLabels[current.getKey() - 1] = Math.min(lowestFoundLabels[current.getKey() - 1],
								lowestFoundLabels[next.getKey() - 1]); // Set current lowest to go to lowest neighbor
					if (!it.hasNext()) {
						if (lowestFoundLabels[current.getKey() - 1] == iteratedValues[current.getKey() - 1] && parent != fake)
							try {
								// System.out.println("New edge:" + current.getKey() + " " + parent.getKey());
								bridges.add(this.graph.edgeBetweenVertices(current, parent));
							} catch (GraphException e) {
								e.printStackTrace();
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
	 * Performs Tarjan's bridge finding algorithm.
	 *
	 * @return All the bridges in the graph.
	 *
	 * @author Joshua Scheidt
	 */
	public ArrayList<Edge> tarjanBridgeFinding() {

		// this.preorderTraversal(this.graph.getVertices().get(1),
		// ((Vertex)this.graph.getVertices().get(1).getNeighbors().toArray()[0]));
		return this.preorderTraversal(this.graph.getVertices().get(1));
	}
}
