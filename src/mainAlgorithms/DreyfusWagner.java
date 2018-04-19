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

/**
 *
 * @author Pit Schneider
 */
public class DreyfusWagner implements SteinerTreeSolver {
	
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
	private HashMap<String, Integer> fMap = new HashMap<>();
	private HashMap<String, Integer> gMap = new HashMap<>();
	private HashMap<String, String> bMap = new HashMap<>();
	private ArrayList<Edge> edges;
	private ArrayList<Vertex> vertices;
	private ArrayList<Vertex> terminals;
	
	// for the moment only prints out the weight of the steiner tree, does not yet return used edges
	@Override
	public List<Edge> solve(UndirectedGraph g) {
		
		this.g = g;
		this.edges = new ArrayList<>(this.g.getEdges());
		this.vertices = new ArrayList<>(this.g.getVertices().values());
		this.terminals = new ArrayList<>(this.g.getTerminals().values());
		
		System.out.println("1 of " + (this.terminals.size()-1));				
		for (Vertex u : this.terminals) {
			ArrayList<Integer> paths = new graph.PathFinding().DijkstraForDW(this.g, u, this.vertices);
			int index = 0;
			for (Vertex v : this.vertices) {
				fMap.put(v.getKey()+"{"+u.getKey()+"}", paths.get(index));
				index++;
			}
		}
		
		for (int m=2; m<=this.terminals.size()-1; m++) {	
			System.out.println(m + " of " + (this.terminals.size()-1));
			ArrayList<ArrayList<Vertex>> subsets = getSubsets(this.terminals, m);
			int counter = 0;
			for (ArrayList<Vertex> X : subsets) {
				System.out.println("\t" + ++counter + " of " + subsets.size());
				for (Vertex v : this.vertices) {
					for (ArrayList<Vertex> XPrime : powerSet(X)) {
						if (XPrime.size() != X.size() && !XPrime.toString().equals("[]")) {
							int gMapVX = getValue(gMap, v.getKey(), X);
							int fMapVXPrime = getValue(fMap, v.getKey(), XPrime);
							int fMapVXDiff = getValue(fMap, v.getKey(), setDifference(X, XPrime));
							if (gMapVX == Integer.MAX_VALUE || fMapVXPrime + fMapVXDiff < gMapVX) {
								gMap.put(v.getKey() + getStringForSet(X), fMapVXPrime + fMapVXDiff);
//									bMap.put(v.getKey()+getStringForSet(X), "{("+v.getKey()+","+getStringForSet(XPrime)+"),("+v.getKey()+","+getStringForSet(setDifference(X, XPrime))+")}");
//									bMap.put(v.getKey()+getStringForSet(X), new BookKeeping(v, XPrime, v, setDifference(X, XPrime)));

							}
						}
					}
				}
				int counter2 = 0;
				int percent = 0;
				for (Vertex u : X) {
					percent = checkProgress(++counter2, percent);
					ArrayList<Integer> paths = new graph.PathFinding().DijkstraForDW(this.g, u, this.vertices);
					int index = 0;
					for (Vertex v : this.vertices) {
						int fMapVX = getValue(fMap, v.getKey(), X);
						int fMapUXDiff = getValue(fMap, u.getKey(), setDifference(X, vertexAsSet(u)));
						if (fMapVX == Integer.MAX_VALUE || (paths.get(index) + fMapUXDiff < fMapVX)) {
							fMap.put(v.getKey() + getStringForSet(X), paths.get(index) + fMapUXDiff);
//								bMap.put(v.getKey()+getStringForSet(X), "{("+u.getKey()+","+getStringForSet(setDifference(X, vertexAsSet(u)))+")}");
//								bMap.put(v.getKey()+getStringForSet(X), new BookKeeping(u, setDifference(X, uSet), null, null));
						}
						index++;
					}	
				}
				for (Vertex u : setDifference(this.vertices, X)) {
					percent = checkProgress(++counter2, percent);
					ArrayList<Integer> paths = new graph.PathFinding().DijkstraForDW(this.g, u, this.vertices);
					int index = 0;
					for (Vertex v : this.vertices) {
						int fMapVX = getValue(fMap, v.getKey(), X);
						int gMapUX = getValue(gMap, u.getKey(), X);
						if (fMapVX == Integer.MAX_VALUE || (paths.get(index) + gMapUX < fMapVX)) {
							fMap.put(v.getKey() + getStringForSet(X), paths.get(index) + gMapUX);
//							bMap.put(v.getKey()+getStringForSet(X), "{("+u.getKey()+","+getStringForSet(X)+")}");
//							bMap.put(v.getKey()+getStringForSet(X), new BookKeeping(u, X, null, null));
						}
						index++;
					}
				}
			}
			
		}
		System.out.println("Weight: " + getValue(fMap, this.terminals.get(0).getKey(), setDifference(this.terminals, vertexAsSet(this.terminals.get(0)))));
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
	
	private int getValue(HashMap<String, Integer> func, Integer vertex, ArrayList<Vertex> set) {
		
		String keyString = vertex + getStringForSet(set);
		Object value = func.get(keyString);
		if (value != null) {
			return (int)(value);
		}
		return Integer.MAX_VALUE;
	}
	
	private String getStringForSet(ArrayList<Vertex> set) {
		if (set.isEmpty()) {
			return "{}";
		}
		String s = "{";
		for (Vertex v : set) {
			s += v.getKey() + ",";
		}
		s = s.substring(0, s.length()-1);
		s += "}";
		return s;
	}
	
	private ArrayList<Vertex> setDifference(ArrayList<Vertex> set1, ArrayList<Vertex> set2) {
		
		ArrayList<Vertex> copy = new ArrayList<>(set1);
		copy.removeAll(set2);
		return copy;
	}

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
	
	private ArrayList<ArrayList<Vertex>> getSubsets(ArrayList<Vertex> originalSet, int size) {
		
		ArrayList<ArrayList<Vertex>> sets = new ArrayList<ArrayList<Vertex>>();
		ArrayList<Vertex> list = new ArrayList<Vertex>(originalSet);
		getSubsets(list, size, 0, new ArrayList<Vertex>(), sets);
		return sets;
	}
	
	private static void getSubsets(ArrayList<Vertex> originalSet, int size, int idx, ArrayList<Vertex> current, ArrayList<ArrayList<Vertex>> solution) {
		
		if (current.size() == size) {
			solution.add(new ArrayList<Vertex>(current));
			return;
		}
		if (idx == originalSet.size()) {
			return;
		}
		Vertex x = originalSet.get(idx);
		current.add(x);
		getSubsets(originalSet, size, idx+1, current, solution);
		current.remove(x);
		getSubsets(originalSet, size, idx+1, current, solution);
	}
	
	private ArrayList<Vertex> vertexAsSet(Vertex v) {
		
		ArrayList<Vertex> set = new ArrayList<>();
		set.add(v);
		return set;
	}
	
	private int checkProgress(int counter, int currentPercent) {
		int percent = (int)(Math.floor((double)(counter)/(double)(this.vertices.size())/0.2)*20.0);
		if (percent > currentPercent) {
			System.out.println("\t\t " + percent + "%");
			return percent;
		} else {
			return currentPercent;
		}
	}
}