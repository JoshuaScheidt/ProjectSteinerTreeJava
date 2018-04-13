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
import java.util.stream.Collectors;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;

/**
 *
 * @author Pit Schneider
 */
public class ImprovedDreyfusWagner implements SteinerTreeSolver {
	
//	class BookKeeping {
//		
//		Map.Entry<Integer,Vertex> v1;
//		Map.Entry<Integer,Vertex> v2;
//		Set<Map.Entry<Integer,Vertex>> set1;
//		Set<Map.Entry<Integer,Vertex>> set2;
//		boolean onePair = false;
//		
//		public BookKeeping(Map.Entry<Integer,Vertex> v1, Set<Map.Entry<Integer,Vertex>> set1, Map.Entry<Integer,Vertex> v2, Set<Map.Entry<Integer,Vertex>> set2) {
//			
//			this.v1 = v1;
//			this.set1 = set1;
//			if (v2 == null && set2 == null) {
//				onePair = true;
//			} else {
//				this.v2 = v2;
//				this.set2 = set2;
//			}
//		}
//	}
	
	private UndirectedGraph g;
	private int numberOfTerminals;
	private int numberOfVertices;
	private int counter;
	private HashMap<String, Integer> fMap = new HashMap<>();
	private HashMap<String, Integer> gMap = new HashMap<>();
	private HashMap<String, String> bMap = new HashMap<>();
	private HashMap<Edge, Integer> wPrimeMap = new HashMap<>();
//	List<Edge> edges = new ArrayList<>();
//	List<Integer> vertices = new ArrayList<>();
	
	// unfortunately does not work yet
	@Override
	public List<Edge> solve(UndirectedGraph g) {
		
		this.g = g;
		this.counter = 0;
		this.numberOfTerminals = g.getNumberOfTerminals();
		this.numberOfVertices = g.getVertices().entrySet().size();

		System.out.println("1 of " + (this.numberOfTerminals-1));				
		for (Map.Entry<Integer,Vertex> u : g.getTerminals().entrySet()) {
			System.out.println("\t" + ++counter + " of " + this.numberOfTerminals);
			for (Map.Entry<Integer,Vertex> v : g.getVertices().entrySet()) {
				fMap.put(v.getKey()+"{"+u.getKey()+"}", sPath(u.getValue(), v.getValue()));
//				bMap.put(v.getKey()+"{"+u.getKey()+"}", "{("+u.getKey()+",{"+u.getKey()+"})}");
//				bMap.put(v.getKey()+"{"+u.getKey()+"}", new BookKeeping(u, vertexAsSet(u), null, null));
			}	
		}
		for (int m=2; m<=this.numberOfTerminals-1; m++) {	
			System.out.println(m + " of " + (this.numberOfTerminals-1));
			Set<Set<Map.Entry<Integer,Vertex>>> subsets = getSubsets(g.getTerminals().entrySet(), m);
			counter = 0;
			for (Set<Map.Entry<Integer,Vertex>> X : subsets) {
				System.out.println("\t" + ++counter + " of " + subsets.size());
				for (Map.Entry<Integer,Vertex> v : g.getVertices().entrySet()) {
					for (Set<Map.Entry<Integer,Vertex>> XPrime : powerSet(X)) {
						if (XPrime.size() != X.size() && !XPrime.toString().equals("[]")) {
							int gMapVX = getValue(gMap, v, X);
							int fMapVXPrime = getValue(fMap, v, XPrime);
							int fMapVXDiff = getValue(fMap, v, setDifference(X, XPrime));
							if (gMapVX == Integer.MAX_VALUE || fMapVXPrime + fMapVXDiff < gMapVX) {
								gMap.put(v.getKey() + getStringForSet(X), fMapVXPrime + fMapVXDiff);
//									bMap.put(v.getKey()+getStringForSet(X), "{("+v.getKey()+","+getStringForSet(XPrime)+"),("+v.getKey()+","+getStringForSet(setDifference(X, XPrime))+")}");
//									bMap.put(v.getKey()+getStringForSet(X), new BookKeeping(v, XPrime, v, setDifference(X, XPrime)));

							}
						}
					}
				}
				
				Vertex s = new Vertex(-1);
				this.g.addVertex(s);
				
				Set<Edge> newEdges = new HashSet<>();
				for (Map.Entry<Integer,Vertex> v : this.g.getVertices().entrySet()) {
					Edge newEdge = new Edge(s, v.getValue(), 0);
					this.g.addEdge(newEdge);
					newEdges.add(newEdge);
					if (X.contains(v)) {
						newEdge.setCost(getValue(fMap, v, setDifference(X, vertexAsSet(v))));
					} else {
						newEdge.setCost(getValue(fMap, v, setDifference(X, vertexAsSet(v))));
					}
				}
				
				int counter = 0;
				
				for (Map.Entry<Integer,Vertex> v : setDifference(this.g.getVertices().entrySet(), X)) {
					fMap.put(v.getKey()+getStringForSet(X), sPath(s, v.getValue()));
					if (v.getKey() != s.getKey()) {
						// bookkeeping
					}
				}

				this.g.removeVertex(s);
				this.g.getEdges().removeAll(newEdges);
			}
			
		}
		Map.Entry<Integer,Vertex> v = g.getTerminals().entrySet().iterator().next();
		System.out.println("Weight: " + getValue(fMap, v, setDifference(g.getTerminals().entrySet(), vertexAsSet(v))));
//		traceback(v, setDifference(g.getTerminals().entrySet(), vertexAsSet(v)));
		return null;
	}
	
//	private void traceback(Map.Entry<Integer,Vertex> v, Set<Map.Entry<Integer,Vertex>> set) {
//		
//		Object obj = bMap.get(v.getKey()+getStringForSet(set));
//		System.out.println(obj);
//		
//		if (obj != null) {
//			BookKeeping newB = (BookKeeping)(obj);
//			if (newB.onePair) {
//				System.out.println(newB.v1.getKey());
//
//				Edge newEdge = newB.v1.getValue().getConnectingEdge(v.getValue());
////				this.edges.add(newEdge);
//				this.vertices.add(newB.v1.getKey());
//				this.vertices.add(v.getKey());
////				System.out.println("cost:" + newEdge.getCost().orElse(-1));
//				traceback(newB.v1, set);
//			} else {
//				traceback(newB.v1, newB.set1);
//				traceback(newB.v2, newB.set2);
//			}
//		}
//	}
	
	private int getValue(HashMap<String, Integer> func, Map.Entry<Integer,Vertex> vertex, Set<Map.Entry<Integer,Vertex>> set) {
		
		String keyString = vertex.getKey() + getStringForSet(set);
		Object value = func.get(keyString);
		if (value != null) {
			return (int)(value);
		}
		return Integer.MAX_VALUE;
	}
	
	private String getStringForSet(Set<Map.Entry<Integer,Vertex>> set) {
		if (set.isEmpty()) {
			return "{}";
		}
		String s = "{";
		for (Map.Entry<Integer,Vertex> v : set) {
			s += v.getKey() + ",";
		}
		s = s.substring(0, s.length()-1);
		s += "}";
		return s;
	}
	
	private Set<Map.Entry<Integer,Vertex>> setDifference(Set<Map.Entry<Integer,Vertex>> set1, Set<Map.Entry<Integer,Vertex>> set2) {
		
		Set<Map.Entry<Integer,Vertex>> copy = set1.stream().collect(Collectors.toSet());
		copy.removeAll(set2);
		return copy;
	}
	
	private int sPath(Vertex start, Vertex end) {
				
		graph.PathFinding pathFinding = new graph.PathFinding();
		return pathFinding.DijkstraSingleEdge(this.g, start, end).getCost().orElse(0);			
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
	
	private Set<Map.Entry<Integer,Vertex>> vertexAsSet(Map.Entry<Integer,Vertex> v) {
		
		Set<Map.Entry<Integer,Vertex>> oneTerminalSet = new HashSet<>();
		oneTerminalSet.add(v);
		return oneTerminalSet;
	}
}