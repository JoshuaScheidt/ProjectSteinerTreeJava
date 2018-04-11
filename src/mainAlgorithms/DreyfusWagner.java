package mainAlgorithms;

import graph.Edge;
import graph.UndirectedGraph;
import graph.Vertex;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;

/**
 *
 * @author Pit Schneider
 */
public class DreyfusWagner implements SteinerTreeSolver {
	
	private UndirectedGraph g;
	private graph.PathFinding pathFinding;
	private Set<Map.Entry<Integer,Vertex>> vertices;
	private Set<Map.Entry<Integer,Vertex>> terminals;
	private int numberOfTerminals;
	private HashMap<String, Double> fMap = new HashMap<>();
	private HashMap<String, Double> gMap = new HashMap<>();
	
	// for the moment only prints out the weight of the steiner tree, does not yet return used edges
	@Override
	public List<Edge> solve(UndirectedGraph g) {
		
		this.g = g;
		this.pathFinding = new graph.PathFinding();
		this.vertices = g.getVertices().entrySet();
		this.terminals = g.getTerminals().entrySet();
		this.numberOfTerminals = this.terminals.size();

		for (Map.Entry<Integer,Vertex> v : this.vertices) {
			for (Map.Entry<Integer,Vertex> u : this.terminals) {
				this.fMap.put(v.getKey() + " {"+u.getKey()+"}", sPath(u, v));
				// still need to store used edges here
			}	
		}
		System.out.println("1 of " + (this.numberOfTerminals-1));				
		for (int m=2; m<=this.numberOfTerminals-1; m++) {	
			System.out.println(m + " of " + (this.numberOfTerminals-1));
			Set<Set<Map.Entry<Integer,Vertex>>> subsets = getSubsets(this.terminals, m);
			int counter = 0;
			for (Set<Map.Entry<Integer,Vertex>> X : subsets) {
				System.out.println("\t" + ++counter + " of " + subsets.size());
				for (Map.Entry<Integer,Vertex> v : this.vertices) {
					for (Set<Map.Entry<Integer,Vertex>> XPrime : powerSet(X)) {
						if (XPrime.size() != X.size() && !XPrime.toString().equals("[]")) {
							if (getValue(fMap, v, XPrime) + getValue(fMap, v, setDifference(X, XPrime)) < getValue(gMap, v, X)) {
								gMap.put(v.getKey() + " " + getStringForSet(X), getValue(fMap, v, XPrime) + getValue(fMap, v, setDifference(X, XPrime)));
								// still need to store used edges here
							}
						}
					}
				}
				for (Map.Entry<Integer,Vertex> v : this.vertices) {
					for (Map.Entry<Integer,Vertex> u : X) {
						Set<Map.Entry<Integer,Vertex>> uSet = new HashSet<>();
						uSet.add(u);
						if (sPath(v, u) + getValue(fMap, u, setDifference(X, uSet)) < getValue(fMap, v, X)) {
							fMap.put(v.getKey() + " " + getStringForSet(X), sPath(v, u) + getValue(fMap, u, setDifference(X, uSet)));
							// still need to store used edges here
						}
					}
					for (Map.Entry<Integer,Vertex> u : setDifference(this.vertices, X)) {
						if (sPath(u, v) + getValue(gMap, u, X) < getValue(fMap, v, X)) {
							fMap.put(v.getKey() + " " + getStringForSet(X), sPath(v, u) + getValue(gMap, u, X));
							// still need to store used edges here
						}
					}	
				}
			}
			
		}
		
		Set<Map.Entry<Integer,Vertex>> oneTerminalSet = new HashSet<>();
		Map.Entry<Integer,Vertex> v = this.terminals.iterator().next();
		oneTerminalSet.add(v);
		System.out.println("Weight: " + getValue(fMap, v, setDifference(this.terminals, oneTerminalSet)));
		return null;
	}
	
//	private List<Edge> traceback(Vertex v, Set<Map.Entry<Integer,Vertex>> X) {
//		
//	}
	
	private Double getValue(HashMap<String, Double> func, Map.Entry<Integer,Vertex> vertex, Set<Map.Entry<Integer,Vertex>> set) {
		
		String keyString = vertex.getKey() + " " + getStringForSet(set);
		Double value = func.get(keyString);
		if (value != null) {
			return value;
		}
		return Double.MAX_VALUE;
	}
	
	private String getStringForSet(Set<Map.Entry<Integer,Vertex>> set) {
		if (set.isEmpty()) {
			return "{}";
		}
		String s = "{";
		for (Map.Entry<Integer,Vertex> v : set) {
			s += v.getKey() + ", ";
		}
		s = s.substring(0, s.length()-2);
		s += "}";
		return s;
	}
	
	private Set<Map.Entry<Integer,Vertex>> setDifference(Set<Map.Entry<Integer,Vertex>> set1, Set<Map.Entry<Integer,Vertex>> set2) {
		Set<Map.Entry<Integer,Vertex>> copy = new HashSet<>();
		for (Map.Entry<Integer,Vertex> vertice : set1) {
			copy.add(vertice);
		}
		copy.removeAll(set2);
		return copy;
	}
	
	private double sPath(Map.Entry<Integer,Vertex> start, Map.Entry<Integer,Vertex> end) {
		
		return pathFinding.DijkstraSingleEdge(this.g, start.getValue(), end.getValue()).getCost().orElse(-1);			
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
	private <T> Set<Set<T>> powerSet(Set<T> originalSet) {
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
	
	private <T> Set<Set<T>> getSubsets(Set<T> originalSet, int size) {
		
		Set<Set<T>> sets = new HashSet<Set<T>>();
		List<T> list = new ArrayList<T>(originalSet);
		getSubsets(list, size, 0, new HashSet<T>(), sets);
		return sets;
	}
	
	private static <T> void getSubsets(List<T> originalSet, int size, int idx, Set<T> current, Set<Set<T>> solution) {
		
		if (current.size() == size) {
			solution.add(new HashSet<>(current));
			return;
		}
		if (idx == originalSet.size()) {
			return;
		}
		T x = originalSet.get(idx);
		current.add(x);
		getSubsets(originalSet, size, idx+1, current, solution);
		current.remove(x);
		getSubsets(originalSet, size, idx+1, current, solution);
	}
}