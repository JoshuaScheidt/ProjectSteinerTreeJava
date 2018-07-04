/*
 * All of the given code may be used on free will when referenced to the source.
 * Initial version created at 16:34:20
 */
package mainAlgorithms;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import graph.Edge;
import graph.UndirectedGraph;
import graph.Vertex;

/**
 * The Mobius Dynamics algorithm for solving Steiner Tree problems.
 *
 * @author Joshua Scheidt
 */
public class MobiusDynamics implements SteinerTreeSolver {

	final double EPSILON = 0.05;
	final double DELTA = 12 * this.EPSILON * Math.log(1 / 3 / this.EPSILON);
	double q;

	/*
	 * (non-Javadoc)
	 * 
	 * @see mainAlgorithms.SteinerTreeSolver#solve(graph.UndirectedGraph)
	 */
	@Override
	public List<Edge> solve(UndirectedGraph G) {
		Map<Integer, Vertex> Z = G.getTerminals();
		this.q = Math.floor(this.EPSILON * Z.size()) + 1;

		Set<Vertex> completeSet = new HashSet<>();
		for (Vertex val : Z.values())
			completeSet.add(val);

		Set<Set<Vertex>> Zsubsets = this.powerSet(completeSet);
		System.out.println(Zsubsets.size());
		for (Set<Vertex> Zprime : Zsubsets) {
			// System.out.println(Zprime.size());
		}

		return null;
	}

	/**
	 * Returns the powerset of the original set.
	 *
	 * @param originalSet
	 *            The original set retrieving a list of vertices
	 * @return The powerset containing all combination between the vertices
	 *
	 * @author Joshua Scheidt
	 */
	public <T> Set<Set<T>> powerSet(Set<T> originalSet) {
		Set<Set<T>> sets = new HashSet<Set<T>>();
		if (originalSet.isEmpty()) {
			sets.add(new HashSet<T>());
			return sets;
		}
		List<T> list = new ArrayList<T>(originalSet);
		T head = list.get(0);
		Set<T> rest = new HashSet<T>(list.subList(1, list.size()));
		for (Set<T> set : this.powerSet(rest)) {
			Set<T> newSet = new HashSet<T>();
			newSet.add(head);
			newSet.addAll(set);
			sets.add(newSet);
			sets.add(set);
		}
		return sets;
	}

}
