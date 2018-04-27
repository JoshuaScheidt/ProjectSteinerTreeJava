package mainAlgorithms;

import graph.Edge;
import graph.UndirectedGraph;
import graph.Vertex;
import graph.EdgeFake;
import graph.PathFinding;

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
	
	// used to store edges part of of the solution
	class BookKeeping {
		
		Vertex v1, v2;
		ArrayList<Vertex> set1, set2;
		boolean onePair = false;
		
		public BookKeeping(Vertex v1, ArrayList<Vertex> set1, Vertex v2, ArrayList<Vertex> set2) {
			
			this.v1 = v1;
			this.set1 = set1;
			if (v2 == null && set2 == null) {
				onePair = true;
			} else {
				this.v2 = v2;
				this.set2 = set2;
			}
		}
	}
	
	private UndirectedGraph g;
	private HashMap<String, Integer> fMap = new HashMap<>();
	private HashMap<String, Integer> gMap = new HashMap<>();
	private HashMap<String, BookKeeping> bMap = new HashMap<>();
	private ArrayList<Edge> solutionEdges = new ArrayList<>();
	private ArrayList<Edge> edges;
	private ArrayList<Vertex> vertices;
	private ArrayList<Vertex> terminals;
	
	// seems to work now, but printed weight differs from returned list of edges combined weight sometimes (not sure why...)
	// more confident in printed weight
	@Override
	public List<Edge> solve(UndirectedGraph g) {
		
		this.g = g;
		this.edges = new ArrayList<>(this.g.getEdges());
		this.vertices = new ArrayList<>(this.g.getVertices().values());
		this.terminals = new ArrayList<>(this.g.getTerminals().values());
		
		System.out.println("1 of " + (this.terminals.size()-1));				
		for (Vertex u : this.terminals) {
			HashMap<Integer,graph.PathFinding.DijkstraInfo> paths = new graph.PathFinding().DijkstraForDW(this.g, u, setDifference(this.vertices, vertexAsSet(u)));
			for (Vertex v : setDifference(this.vertices, vertexAsSet(u))) {
				fMap.put(v.getKey()+"{"+u.getKey()+"}", paths.get(v.getKey()).dist);
				bMap.put(v.getKey()+"{"+u.getKey()+"}", new BookKeeping(u, vertexAsSet(u), null, null));
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
								bMap.put(v.getKey() + getStringForSet(X), new BookKeeping(v, XPrime, v, setDifference(X, XPrime)));
							}
						}
					}
				}
								
				Vertex s = new Vertex(-1);
				this.g.addVertex(s);
				
				ArrayList<Edge> newEdges = new ArrayList<>();
				for (Vertex v : this.vertices) {
					Edge newEdge = new Edge(s, v, 0);
					this.g.addEdge(newEdge);
					newEdges.add(newEdge);
					if (X.contains(v)) {
						newEdge.setCost(getValue(fMap, v.getKey(), setDifference(X, vertexAsSet(v))));
					} else {
						newEdge.setCost(getValue(gMap, v.getKey(), setDifference(X, vertexAsSet(v))));
					}
				}
				
				this.edges = new ArrayList<>(this.g.getEdges());
				this.vertices = new ArrayList<>(this.g.getVertices().values());
												
				HashMap<Integer,graph.PathFinding.DijkstraInfo> paths = new graph.PathFinding().DijkstraForDW(this.g, s, setDifference(setDifference(this.vertices, X), vertexAsSet(s)));	
				for (Vertex v : setDifference(setDifference(this.vertices, X), vertexAsSet(s))) {
					fMap.put(v.getKey()+getStringForSet(X), paths.get(v.getKey()).dist);
					Vertex u = paths.get(v.getKey()).parent;
					
					if (u.getKey() != s.getKey()) {
						bMap.put(v.getKey() + getStringForSet(X), new BookKeeping(u, X, null, null));
					}
				}

				this.g.removeVertex(s);
				this.g.getEdges().removeAll(newEdges);
				
				this.edges = new ArrayList<>(this.g.getEdges());
				this.vertices = new ArrayList<>(this.g.getVertices().values());
			}
		}
		System.out.println("Weight: " + getValue(fMap, this.terminals.get(0).getKey(), setDifference(this.terminals, vertexAsSet(this.terminals.get(0)))));
		traceback(this.terminals.get(0), setDifference(this.terminals, vertexAsSet(this.terminals.get(0))));
		return solutionEdges;
	}
	
	private void traceback(Vertex v, ArrayList<Vertex> set) {
		
		Object obj = bMap.get(v.getKey()+getStringForSet(set));
		
		if (obj != null) {
			
			BookKeeping newB = (BookKeeping)(obj);	
			if (set.size() == 1 && set.contains(v)) {
				return;
			}
			if (getStringForSet(set).equals(getStringForSet(newB.set1)) && (v.getKey() == newB.v1.getKey())) {
				return;
			}
			
			if (getStringForSet(set).equals(getStringForSet(newB.set1))) {
				EdgeFake edge = (new PathFinding().DijkstraMultiPathFakeEdges(this.g, newB.v1, vertexAsSet(v), this.edges)).get(0);
				if (edge.getStack() == null) {
					Edge edgeToAdd = newB.v1.getConnectingEdge(v);
					solutionEdges.add(edgeToAdd);
				} else {
					for (int i=0; i<edge.getStack().size(); i++) {
						int[] s = edge.getStack().get(i);
						Edge edgeToAdd = g.getVertices().get(s[0]).getConnectingEdge(g.getVertices().get(s[1]));
						solutionEdges.add(edgeToAdd);
					}
				}
				traceback(newB.v1, set);
			} else {
				traceback(newB.v1, newB.set1);
				if (!newB.onePair) {
					traceback(newB.v2, newB.set2);
				}
			}
		}
	}
	
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
}