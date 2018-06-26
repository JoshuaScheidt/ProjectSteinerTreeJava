package mainAlgorithms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import graph.Edge;
import graph.PathFinding;
import graph.RandomMain;
import graph.UndirectedGraph;
import graph.Vertex;

/**
 * This class implements the IDW algorithm.
 *
 * @author Pit Schneider
 */
public class ImprovedDreyfusWagner implements SteinerTreeSolver {

	/**
	 * This class is used to store the edges that are part of the solution.
	 * Instances of this class are basically entries of the b function in the pseudo
	 * code Entries are either 1 or 2 vertex-set pair(s) In case we only want to
	 * store 1 pair, we pass null for the 3rd and 4th parameter
	 *
	 * @author Pit Schneider
	 */
	class BookKeeping {

		Vertex v1, v2;
		ArrayList<Vertex> set1, set2;
		boolean onePair = false;

		public BookKeeping(Vertex v1, ArrayList<Vertex> set1, Vertex v2, ArrayList<Vertex> set2) {

			this.v1 = v1;
			this.set1 = set1;
			if (v2 == null && set2 == null) {
				this.onePair = true;
			} else {
				this.v2 = v2;
				this.set2 = set2;
			}
		}

		@Override
		public String toString() {
			String s = this.v1.getKey() + ImprovedDreyfusWagner.this.getStringForSet(this.set1);
			if (this.v2 != null) {
				s += this.v2.getKey() + ImprovedDreyfusWagner.this.getStringForSet(this.set2);
			}
			return s;
		}
	}

	private UndirectedGraph g; // graph used for algorithm
	// String in following 3 HashMaps is of form: "1{1,2,3}" where 1 is the vertex
	// and {1,2,3} the set used as key to the map value
	private HashMap<String, Integer> fMap = new HashMap<>(); // f function in pseudo code
	private HashMap<String, Integer> gMap = new HashMap<>(); // g function in pseudo code
	private HashMap<String, BookKeeping> bMap = new HashMap<>(); // b function in pseudo code
	private ArrayList<Edge> solutionEdges = new ArrayList<>(); // final solution of algorithm
	private ArrayList<Edge> testSolutionEdges = new ArrayList<>(); // candidate for the solution
	private ArrayList<Edge> edges; // all edges of g
	private ArrayList<Vertex> vertices; // all vertices of g
	private ArrayList<Vertex> terminals; // all terminals of g

	/**
	 * Performs Dijkstra's path finding algorithm and returns the new edge between
	 * the vertices. All println's in the code are used to monitor the progress of
	 * the algorithm
	 *
	 * @param g
	 *            The graph in which IDW has to be performed
	 * @return The list of edges belonging to the minimum steiner tree
	 *
	 * @author Pit Schneider
	 */
	@Override
	public List<Edge> solve(UndirectedGraph g) {

		this.g = g;
		this.edges = new ArrayList<>(this.g.getEdges());
		this.vertices = new ArrayList<>(this.g.getVertices().values());
		this.terminals = new ArrayList<>(this.g.getTerminals().values());

		// System.out.println("1 of " + (this.terminals.size()-1));
		for (Vertex u : this.terminals) {
			if (RandomMain.killed)
				return null;
			HashMap<Integer, graph.PathFinding.DijkstraInfo> paths = PathFinding.DijkstraForDW(this.g, u,
					this.setDifference(this.vertices, this.vertexAsSet(u)));
			if (paths == null)
				return null;
			for (Vertex v : this.setDifference(this.vertices, this.vertexAsSet(u))) {
				this.fMap.put(v.getKey() + this.getStringForSet(this.vertexAsSet(u)), paths.get(v.getKey()).dist);
				this.bMap.put(v.getKey() + this.getStringForSet(this.vertexAsSet(u)), new BookKeeping(u, this.vertexAsSet(u), null, null));
			}
		}

		// int casesAvoided = 0;

		for (int m = 2; m <= this.terminals.size() - 1; m++) {
			// System.out.println(m + " of " + (this.terminals.size()-1));
			ArrayList<ArrayList<Vertex>> subsets = this.getSubsets(this.terminals, m);
			int counter = 0;
			for (ArrayList<Vertex> X : subsets) {
				// System.out.println("\t" + ++counter + " of " + subsets.size());
				for (Vertex v : this.vertices) {
					for (ArrayList<Vertex> XPrime : this.powerSet(X)) {
						if (RandomMain.killed)
							return null;
						if (XPrime.size() != X.size() && !XPrime.toString().equals("[]")) {
							// gMapVX, fMapVXPrime, fMapVXDiff are computed only once to speed up algorithm
							// a little
							int gMapVX = this.getValue(this.gMap, v.getKey(), X);
							int fMapVXPrime = this.getValue(this.fMap, v.getKey(), XPrime);
							int fMapVXDiff = this.getValue(this.fMap, v.getKey(), this.setDifference(X, XPrime));
							// if gMapVX does not exist yet and is equal to MAX_VALUE we can avoid
							// calculating fMapVXPrime + fMapVXDiff < gMapVX
							if ((gMapVX == Integer.MAX_VALUE || fMapVXPrime + fMapVXDiff < gMapVX) && !(XPrime.contains(v) && XPrime.size() > 1)) {
								// if (gMapVX == Integer.MAX_VALUE || fMapVXPrime + fMapVXDiff < gMapVX) {
								this.gMap.put(v.getKey() + this.getStringForSet(X), fMapVXPrime + fMapVXDiff);
								this.bMap.put(v.getKey() + this.getStringForSet(X), new BookKeeping(v, XPrime, v, this.setDifference(X, XPrime)));
							}
							// else {
							//// if(!(XPrime.contains(v) && XPrime.size() > 1)){
							////// System.out.println("Catches one of those cases:");
							////// System.out.println(v.getKey());
							////// System.out.println("Set: ");
							////// for(Vertex checking : XPrime){
							////// System.out.println(checking.getKey());
							////// }
							//// casesAvoided++;
							//// }
							// }
						}
					}
				}

				// add new vertex s with key -1 (we are sure this is unique) to g
				Vertex s = new Vertex(-1);
				this.g.addVertex(s);

				// track edges which will be temporarily added to g (to be able to later remove
				// them again)
				ArrayList<Edge> newEdges = new ArrayList<>();
				for (Vertex v : this.vertices) {
					// weight is 0 to start, will be changed with the next if-else statement
					Edge newEdge = new Edge(s, v, 0);
					this.g.addEdge(newEdge);
					newEdges.add(newEdge);
					if (X.contains(v)) {
						newEdge.setCost(this.getValue(this.fMap, v.getKey(), this.setDifference(X, this.vertexAsSet(v))));
					} else {
						newEdge.setCost(this.getValue(this.gMap, v.getKey(), X));

					}
				}

				// update gloable variables to reflect changes
				this.edges = new ArrayList<>(this.g.getEdges());
				this.vertices = new ArrayList<>(this.g.getVertices().values());

				HashMap<Integer, graph.PathFinding.DijkstraInfo> paths = new graph.PathFinding().DijkstraForDW(this.g, s,
						this.setDifference(this.setDifference(this.vertices, X), this.vertexAsSet(s)));
				for (Vertex v : this.setDifference(this.setDifference(this.vertices, X), this.vertexAsSet(s))) {
					if (RandomMain.killed)
						return null;
					this.fMap.put(v.getKey() + this.getStringForSet(X), paths.get(v.getKey()).dist);

					Vertex u = paths.get(v.getKey()).parent;

					if (u.getKey() != s.getKey()) {
						this.bMap.put(v.getKey() + this.getStringForSet(X), new BookKeeping(u, X, null, null));
					}
				}

				// remove added vertex s and new edges
				this.g.removeVertex(s);
				this.g.getEdges().removeAll(newEdges);

				// update gloabl variables again so they correspond to the normal g edges and
				// vertices
				this.edges = new ArrayList<>(this.g.getEdges());
				this.vertices = new ArrayList<>(this.g.getVertices().values());
			}
		}
		// System.out.println("Cases Avoided: " + casesAvoided);

		this.traceback(this.terminals.get(0), this.setDifference(this.terminals, this.vertexAsSet(this.terminals.get(0))));

		return this.solutionEdges;
	}

	/**
	 * Performs the traceback method to generate the list of edges of the minimum
	 * steiner tree. It accesses the Bookkeeping instances stored in bMap. Edges are
	 * stored in testSolutionEdges. (Pseudo code also in paper)
	 *
	 * @param v
	 *            The vertex v which has to be an arbitrary terminal vertex
	 * @param set
	 *            The set of vertices which is equal to all terminals without vertex
	 *            v
	 *
	 * @author Pit Schneider
	 */
	private void traceback(Vertex v, ArrayList<Vertex> set) {
		if (RandomMain.killed)
			return;

		Object obj = this.bMap.get(v.getKey() + this.getStringForSet(set));
		if (obj != null) {

			BookKeeping newB = (BookKeeping) (obj);
			// example: b(1,{1})
			if (set.size() == 1 && set.contains(v)) {
				return;
			}
			// example: b(1,{2,3}) = (1,{2,3})
			if (this.getStringForSet(set).equals(this.getStringForSet(newB.set1)) && (v.getKey() == newB.v1.getKey())) {
				return;
			}

			// we know that we can add an edge to the solution
			if (this.getStringForSet(set).equals(this.getStringForSet(newB.set1)) && newB.onePair) {
				ArrayList<Edge> path = PathFinding.DijkstraSinglePath(this.g, newB.v1, v);
				if (path == null)
					return;
				for (Edge e : path) {
					int[] s = { e.getVertices()[0].getKey(), e.getVertices()[1].getKey(), e.getCost().get() };
					Edge edgeToAdd = this.g.getVertices().get(s[0]).getConnectingEdge(this.g.getVertices().get(s[1]));
					this.solutionEdges.add(edgeToAdd);
				}
				this.traceback(newB.v1, this.setDifference(set, this.vertexAsSet(newB.v1)));
			}
			// else we make 1 or 2 recursive calls depending on how many vertex-set pairs we
			// find in the b function
			else {
				this.traceback(newB.v1, newB.set1);
				if (!newB.onePair) {
					this.traceback(newB.v2, newB.set2);
				}
			}
		}
	}

	/**
	 * Performs the traceback method to generate the list of edges of the minimum
	 * steiner tree. It accesses the Bookkeeping instances stored in bMap. Edges are
	 * stored in testSolutionEdges.
	 *
	 * @param func
	 *            The function in the pseudo code, or in Java terms the
	 *            HashMap<String, Integer>, used to get the value from
	 * @param vertex
	 *            The vertex which is paramter to the function in the pseudo code
	 * @param set
	 *            The set of vertices which is paramter to the function in the
	 *            pseudo code
	 * @return The integer value stored in the HashMap, MAX_VALUE in case there is
	 *         no entry
	 *
	 * @author Pit Schneider
	 */
	private int getValue(HashMap<String, Integer> func, Integer vertex, ArrayList<Vertex> set) {

		String keyString = vertex + this.getStringForSet(set);
		Object value = func.get(keyString);
		if (value != null) {
			return (int) (value);
		}
		return Integer.MAX_VALUE;
	}

	/**
	 * Transforms a set of vertices into a String representation
	 *
	 * @param set
	 *            The set of vertices which should be transformed into a String
	 * @return The String representing the set
	 *
	 * @author Pit Schneider
	 */
	private String getStringForSet(ArrayList<Vertex> set) {
		if (set.isEmpty()) {
			return "{}";
		}
		String s = "{";
		for (Vertex v : set) {
			s += v.getKey() + ",";
		}
		s = s.substring(0, s.length() - 1);
		s += "}";
		return s;
	}

	/**
	 * Computes a difference between two sets.
	 *
	 * @param set1
	 *            The first set as an ArrayList<Vertex>
	 * @param set2
	 *            The second set as an ArrayList<Vertex>
	 * @return The difference set of set1\set2
	 *
	 * @author Pit Schneider
	 */
	private ArrayList<Vertex> setDifference(ArrayList<Vertex> set1, ArrayList<Vertex> set2) {

		ArrayList<Vertex> copy = new ArrayList<>(set1);
		copy.removeAll(set2);
		return copy;
	}

	/**
	 * Computes the power set of a given set
	 *
	 * @param originalSet
	 *            The set used as a basis for computing the power set
	 * @return The power set stored as an ArrayList of ArrayLists
	 *
	 * @author Pit Schneider
	 */
	private ArrayList<ArrayList<Vertex>> powerSet(ArrayList<Vertex> originalSet) {
		ArrayList<ArrayList<Vertex>> sets = new ArrayList<ArrayList<Vertex>>();
		if (originalSet.isEmpty()) {
			sets.add(new ArrayList<Vertex>());
			return sets;
		}
		ArrayList<Vertex> list = new ArrayList<>(originalSet);
		Vertex head = list.get(0);
		ArrayList<Vertex> rest = new ArrayList<Vertex>(list.subList(1, list.size()));
		for (ArrayList<Vertex> set : this.powerSet(rest)) {
			ArrayList<Vertex> newSet = new ArrayList<Vertex>();
			newSet.add(head);
			newSet.addAll(set);
			sets.add(newSet);
			sets.add(set);
		}
		return sets;
	}

	/**
	 * Computes all the subsets of a given set which respect a desired element size
	 *
	 * @param originalSet
	 *            The set used as a basis for computing the subsets
	 * @param size
	 *            The size of all the computed subsets
	 * @return The subsets represented as an ArrayList of ArrayLists
	 *
	 * @author Pit Schneider
	 */
	private ArrayList<ArrayList<Vertex>> getSubsets(ArrayList<Vertex> originalSet, int size) {

		ArrayList<ArrayList<Vertex>> sets = new ArrayList<ArrayList<Vertex>>();
		ArrayList<Vertex> list = new ArrayList<Vertex>(originalSet);
		getSubsets(list, size, 0, new ArrayList<Vertex>(), sets);
		return sets;
	}

	// helper function of above function
	private static void getSubsets(ArrayList<Vertex> originalSet, int size, int idx, ArrayList<Vertex> current,
			ArrayList<ArrayList<Vertex>> solution) {

		if (current.size() == size) {
			solution.add(new ArrayList<Vertex>(current));
			return;
		}
		if (idx == originalSet.size()) {
			return;
		}
		Vertex x = originalSet.get(idx);
		current.add(x);
		getSubsets(originalSet, size, idx + 1, current, solution);
		current.remove(x);
		getSubsets(originalSet, size, idx + 1, current, solution);
	}

	/**
	 * Turns a vertex into a set containing only this vertex
	 *
	 * @param v
	 *            The vertex that should be turned into a set
	 * @return The set containing only this vertex
	 *
	 * @author Pit Schneider
	 */
	private ArrayList<Vertex> vertexAsSet(Vertex v) {

		ArrayList<Vertex> set = new ArrayList<>();
		set.add(v);
		return set;
	}
}
