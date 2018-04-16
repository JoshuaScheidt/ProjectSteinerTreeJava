/*
 * All of the given code may be used on free will when referenced to the source.
 * Initial version created at 16:15:35
 */
package graph;

import java.util.Stack;

/**
 * Special edge class for the case where you do not want to add edge to the
 * graph yet.
 * 
 * @author Joshua Scheidt
 */
public class EdgeFake {

	Vertex v1;
	Vertex v2;
	int cost;
	Stack<int[]> stack;

	public EdgeFake(Vertex v1, Vertex v2, int cost, Stack<int[]> stack) {
		this.v1 = v1;
		this.v2 = v2;
		this.cost = cost;
		this.stack = stack;
	}

	public Vertex getOtherVertex(Vertex v) {
		if (v.equals(this.v1))
			return this.v2;
		else
			return this.v1;
	}

	public Vertex[] getVertices() {
		return new Vertex[] { this.v1, this.v2 };
	}

	public int getCost() {
		return this.cost;
	}

	public Stack<int[]> getStack() {
		return this.stack;
	}
}
