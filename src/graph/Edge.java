/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graph;

import java.util.Optional;
import java.util.Stack;

/**
 *
 * @author Marciano
 */
public class Edge extends Object {

	private Vertex[] connected = new Vertex[2];
	private Optional<Integer> cost;
	private Stack<int[]> subsumed;

	/**
	 * Constructor for creating an Edge given 2 Vertices and a cost
	 *
	 * @param v1
	 *            Vertex one to be connected to Vertex two
	 * @param v2
	 *            Vertex two to be connected to Vertex one
	 * @param c
	 *            Cost of taking this Edge into your Minimum Steiner Tree
	 */
	public Edge(Vertex v1, Vertex v2, int c) {
		if (!v1.isNeighbor(v2)) {
			this.connected[0] = v1;
			this.connected[1] = v2;
			v1.addEdge(this);
			v2.addEdge(this);
			this.cost = Optional.of(c);
		}
	}

	/**
	 * Constructor for creating an Edge given 2 Vertices and a cost. This one is
	 * only allowed to be used when 100% certain that possible multi connected
	 * vertices remove all but 1 connecting edges
	 *
	 * @param v1
	 *            Vertex one to be connected to Vertex two
	 * @param v2
	 *            Vertex two to be connected to Vertex one
	 * @param c
	 *            Cost of taking this Edge into your Minimum Steiner Tree
	 */
	public Edge(Vertex v1, Vertex v2, int c, boolean force) {
		if (force) {
			this.connected[0] = v1;
			this.connected[1] = v2;
			v1.addEdge(this);
			v2.addEdge(this);
			this.cost = Optional.of(c);
		}
	}

	/**
	 * Sets the cost of the current Edge
	 * 
	 * @param cost
	 *            Parameter to exchange Edge cost
	 */
	public void setCost(int cost) {
		this.cost = Optional.of(cost);
	}

	/**
	 * Pushes an entire Stack to the current Stack, it will retain the order of the
	 * input Stack which means the top item of the input Stack will also be the top
	 * item of this objects Stack
	 *
	 * @param stack
	 *            Input Stack of another Vertex
	 */
	public void pushStack(Stack<int[]> stack) {
		if (this.subsumed == null) {
			this.subsumed = new Stack<>();
		}
		for (int i = stack.size() - 1; i >= 0; i--) {
			this.pushSubsumed(stack.get(i));
		}
	}

	/**
	 * Pushes Double array to the Stack
	 *
	 * @param keys
	 *            Items to be added to Stack
	 */
	public void pushSubsumed(int[] keys) {
		if (this.subsumed == null) {
			this.subsumed = new Stack<>();
		}
		this.subsumed.push(keys);
	}

	/**
	 * Replaces the current stack with the new stack.
	 *
	 * @param stack
	 *            The new stack
	 *
	 * @author Joshua Scheidt
	 */
	public void replaceStack(Stack<int[]> stack) {
		this.subsumed = stack;
	}

	/**
	 * Returns the complete current stack
	 *
	 * @return The stack of subsumed edges
	 *
	 * @author Joshua Scheidt
	 */
	public Stack<int[]> getStack() {
		return this.subsumed;
	}

	/**
	 * Gets the other side of the Edge given one side of the Edge by a Vertex v
	 *
	 * @param v
	 *            Vertex to be checked and given its opposite
	 * @return A Vertex which is not v, if v is not in the connected array at all it
	 *         will return null
	 */
	public Vertex getOtherSide(Vertex v) {
		int index = -1;
		if (this.connected[0].equals(v) || this.connected[1].equals(v)) {
			if (this.connected[0].equals(v)) {
				return this.connected[1];
			} else {
				return this.connected[0];
			}
		} else {
			return null;
		}
	}

	/**
	 * Returns the Vertices attached to this Edge this cannot exceed 2
	 *
	 * @return Array of 2 Vertices
	 */
	public Vertex[] getVertices() {
		return this.connected;
	}

	/**
	 * The cost is an Optional Integer to check if it had been set before
	 *
	 * @return Optional Integer from which the Integer can be received and can be
	 *         checked if it had been set
	 */
	public Optional<Integer> getCost() {
		return this.cost;
	}
}
