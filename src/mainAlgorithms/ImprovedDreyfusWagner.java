package mainAlgorithms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import graph.Edge;
import graph.PathFinding;
import graph.RandomMain;
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
import java.util.Arrays;
import java.util.Collections;

/**
 * This class implements the IDW algorithm.
 *
 * @author Pit Schneider
 */
public class ImprovedDreyfusWagner implements SteinerTreeSolver {
    /**
     * This class is used to store the edges that are part of the solution.
     * Instances of this class are basically entries of the b function in the
     * pseudo code Entries are either 1 or 2 vertex-set pair(s) In case we only
     * want to store 1 pair, we pass null for the 3rd and 4th parameter
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
                onePair = true;
            } else {
                this.v2 = v2;
                this.set2 = set2;
            }
        }

        @Override
        public String toString() {
            String s = v1.getKey() + getStringForSet(set1);
            if (v2 != null) {
                s += v2.getKey() + getStringForSet(set2);
            }
            return s;
        }
    }

    private UndirectedGraph g; // graph used for algorithm
    // String in following 3 HashMaps is of form: "1{1,2,3}" where 1 is the vertex and {1,2,3} the set used as key to the map value
    private HashMap<String, Integer> map = new HashMap<>(); // f an g function in pseudo code
    private HashMap<String, BookKeeping> bMap = new HashMap<>(); // b function in pseudo code
    private ArrayList<Edge> solutionEdges = new ArrayList<>(); // final solution of algorithm
    private ArrayList<Vertex> vertices; // all vertices of g
    private ArrayList<Vertex> terminals; // all terminals of g

    /**
     * Performs Dijkstra's path finding algorithm and returns the new edge
     * between the vertices. All println's in the code are used to monitor the
     * progress of the algorithm
     *
     * @param g The graph in which IDW has to be performed
     * @return The list of edges belonging to the minimum steiner tree
     *
     * @author Pit Schneider
     */
    @Override
    public List<Edge> solve(UndirectedGraph g) {

        this.g = g;
        this.vertices = new ArrayList<>(this.g.getVertices().values());
        this.terminals = new ArrayList<>(this.g.getTerminals().values());
        Vertex startingTerminal = this.terminals.get(0);
//        System.out.println("1 of " + (this.terminals.size()-1));	

        for (Vertex u : this.terminals) {
            HashMap<Integer, graph.PathFinding.DijkstraInfo> paths = new graph.PathFinding().DijkstraForDW(this.g, u, setDifference(this.vertices, vertexAsSet(u)));
            for (Vertex v : setDifference(this.vertices, vertexAsSet(u))) {
                map.put("f" + v.getKey() + getStringForSet(vertexAsSet(u)), paths.get(v.getKey()).dist);
                bMap.put(v.getKey() + getStringForSet(vertexAsSet(u)), new BookKeeping(u, vertexAsSet(u), null, null));
            }
        }

        ArrayList<Vertex> copyTerminals = new ArrayList<>(this.terminals);
        copyTerminals.remove(startingTerminal);
        int gMapVX, fMapVXPrime, fMapVXDiff;
        for (int m = 2; m <= this.terminals.size() - 1; m++) {
            ArrayList<ArrayList<Vertex>> subsets = getSubsets(copyTerminals, m);
            //int counter = 0;
            for (ArrayList<Vertex> X : subsets) {
//                System.out.println("\t" + ++counter + " of " + subsets.size());
                for (Vertex v : this.vertices) {
                    gMapVX = getValue("g" + v.getKey(), X);
                    for (ArrayList<Vertex> XPrime : powerSet(X)) {
                        if (XPrime.size() != X.size()) {
                            // gMapVX, fMapVXPrime, fMapVXDiff are computed only once to speed up algorithm a little
                            fMapVXPrime = getValue("f" + v.getKey(), XPrime);
                            fMapVXDiff = getValue("f" + v.getKey(), setDifference(X, XPrime));
                            // if gMapVX does not exist yet and is equal to MAX_VALUE we can avoid calculating fMapVXPrime + fMapVXDiff < gMapVX
                            if (gMapVX == Integer.MAX_VALUE || fMapVXPrime + fMapVXDiff < gMapVX) {
                                map.put("g" + v.getKey() + getStringForSet(X), fMapVXPrime + fMapVXDiff);
                                bMap.put(v.getKey() + getStringForSet(X), new BookKeeping(v, XPrime, v, setDifference(X, XPrime)));
                            }
                        }
                    }
                }

                // add new vertex s with key -1 (we are sure this is unique) to g				
                Vertex s = new Vertex(-1);
                this.g.addVertex(s);

                // track edges which will be temporarily added to g (to be able to later remove them again)
                ArrayList<Edge> newEdges = new ArrayList<>();
                Edge newEdge;
                for (Vertex v : this.vertices) {
                    // weight is 0 to start, will be changed with the next if-else statement
                    newEdge = new Edge(s, v, 0);
                    this.g.addEdge(newEdge);
                    newEdges.add(newEdge);
                    if (X.contains(v)) {
                        newEdge.setCost(getValue("f" + v.getKey(), setDifference(X, vertexAsSet(v))));
                    } else {
                        newEdge.setCost(getValue("g" + v.getKey(), X));

                    }
                }

                // update gloable variable to reflect changes
                this.vertices = new ArrayList<>(this.g.getVertices().values());
                Vertex u;
                HashMap<Integer, graph.PathFinding.DijkstraInfo> paths = new graph.PathFinding().DijkstraForDW(this.g, s, setDifference(setDifference(this.vertices, X), vertexAsSet(s)));
                for (Vertex v : setDifference(setDifference(this.vertices, X), vertexAsSet(s))) {
                    map.put("f" + v.getKey() + getStringForSet(X), paths.get(v.getKey()).dist);

                    u = paths.get(v.getKey()).parent;

                    if (u.getKey() != s.getKey()) {
                        bMap.put(v.getKey() + getStringForSet(X), new BookKeeping(u, X, null, null));
                    }
                }

                // remove added vertex s and new edges
                this.g.removeVertex(s);
                this.g.getEdges().removeAll(newEdges);

                // update gloabl variable again so it corresponds to the normal g vertices
                this.vertices = new ArrayList<>(this.g.getVertices().values());
            }
        }
        traceback(startingTerminal, setDifference(this.terminals, vertexAsSet(startingTerminal)));

        return solutionEdges;
    }

    /**
     * Performs the traceback method to generate the list of edges of the
     * minimum steiner tree. It accesses the Bookkeeping instances stored in
     * bMap. Edges are stored in testSolutionEdges. (Pseudo code also in paper)
     *
     * @param v The vertex v which has to be an arbitrary terminal vertex
     * @param set The set of vertices which is equal to all terminals without
     * vertex v
     *
     * @author Pit Schneider
     */
    private void traceback(Vertex v, ArrayList<Vertex> set) {

        Object obj = bMap.get(v.getKey() + getStringForSet(set));
        if (obj != null) {

            BookKeeping newB = (BookKeeping) (obj);
            // example: b(1,{1})	
            if (set.size() == 1 && set.contains(v)) {
                return;
            }
            // example: b(1,{2,3}) = (1,{2,3})
            if (getStringForSet(set).equals(getStringForSet(newB.set1)) && (v.getKey() == newB.v1.getKey())) {
                return;
            }

            // we know that we can add an edge to the solution
            if (getStringForSet(set).equals(getStringForSet(newB.set1)) && newB.onePair) {
                ArrayList<Edge> path = PathFinding.DijkstraSinglePath(this.g, newB.v1, v);
                for (Edge e : path) {
                    int[] s = {e.getVertices()[0].getKey(), e.getVertices()[1].getKey(), e.getCost().get()};
                    Edge edgeToAdd = g.getVertices().get(s[0]).getConnectingEdge(g.getVertices().get(s[1]));
                    solutionEdges.add(edgeToAdd);
                }
                traceback(newB.v1, setDifference(set, vertexAsSet(newB.v1)));
            } // else we make 1 or 2 recursive calls depending on how many vertex-set pairs we find in the b function
            else {
                traceback(newB.v1, newB.set1);
                if (!newB.onePair) {
                    traceback(newB.v2, newB.set2);
                }
            }
        }
    }

    /**
     * Performs the traceback method to generate the list of edges of the
     * minimum steiner tree. It accesses the Bookkeeping instances stored in
     * bMap. Edges are stored in testSolutionEdges.
     *
     * @param func The function in the pseudo code, or in Java terms the
     * HashMap<String, Integer>, used to get the value from
     * @param vertex The vertex which is paramter to the function in the pseudo
     * code
     * @param set The set of vertices which is paramter to the function in the
     * pseudo code
     * @return The integer value stored in the HashMap, MAX_VALUE in case there
     * is no entry
     *
     * @author Pit Schneider
     */
    private int getValue(String key, ArrayList<Vertex> set) {

        String keyString = key + getStringForSet(set);
        Object value = map.get(keyString);
        if (value != null) {
            return (int) (value);
        }
        return Integer.MAX_VALUE;
    }

    /**
     * Transforms a set of vertices into a String representation
     *
     * @param set The set of vertices which should be transformed into a String
     * @return The String representing the set
     *
     * @author Pit Schneider
     */
    private String getStringForSet(ArrayList<Vertex> set) {

        StringBuilder tmp = new StringBuilder();
        tmp.append("{");
        for (Vertex v : set) {
            tmp.append(v.getKey() + ",");
        }
        return tmp.toString();
    }

    /**
     * Computes a difference between two sets.
     *
     * @param set1 The first set as an ArrayList<Vertex>
     * @param set2 The second set as an ArrayList<Vertex>
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
     * @param originalSet The set used as a basis for computing the power set
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
            if (set.size() != this.terminals.size()) {
                ArrayList<Vertex> newSet = new ArrayList<Vertex>();
                newSet.add(head);
                newSet.addAll(set);
                sets.add(newSet);
            }
            if (set.isEmpty()) {
            } else {
                sets.add(set);
            }
        }
        return sets;
    }

    /**
     * Computes all the subsets of a given set which respect a desired element
     * size
     *
     * @param originalSet The set used as a basis for computing the subsets
     * @param size The size of all the computed subsets
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
        getSubsets(originalSet, size, idx + 1, current, solution);
        current.remove(x);
        getSubsets(originalSet, size, idx + 1, current, solution);
    }

    /**
     * Turns a vertex into a set containing only this vertex
     *
     * @param v The vertex that should be turned into a set
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
