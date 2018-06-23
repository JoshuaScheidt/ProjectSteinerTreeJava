/*
j * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
    private HashMap<Integer, BitSet> checked;

    public PreProcess(UndirectedGraph g) {
        this.graph = g.clone();
        this.checked = new HashMap<>();
        Iterator it = this.graph.getVertices().keySet().iterator();
        BitSet allFalse;
        while (it.hasNext()) {
            allFalse = new BitSet(4);
            allFalse.set(0, 4);
            allFalse.flip(0, allFalse.length() - 1);
            this.checked.put((int) it.next(), allFalse);
        }
        // for (Vertex v : this.graph.getVertices().values()) {
        // System.out.println(this.checked.get(v.getKey()).cardinality() + " " +
        // this.checked.get(v.getKey()).length());
        // System.out.println(this.checked.get(v.getKey()).get(0));
        // System.out.println(this.checked.get(v.getKey()).get(1));
        // System.out.println(this.checked.get(v.getKey()).get(2));
        // }
    }

    /**
     * The following method checks each clique of size three and sees if any sum
     * of two edges is smaller than the third. If that is the case the third
     * edge can be removed.
     */
    public void cliqueEdgeRemoval() {
        HashSet<HashSet<Integer>> cliques = new HashSet<>();
        HashSet<Integer> clique;
        HashSet<Edge> toBeRemoved = new HashSet<>();
        Edge vn, vc, nc;
        // Finding all unique cliques
        for (Vertex v : this.graph.getVertices().values()) {
            if (!(this.checked.get(v.getKey()).cardinality() == this.checked.get(v.getKey()).length())) {
                for (Vertex n : v.getNeighbors()) {
                    if (!(this.checked.get(n.getKey()).cardinality() == this.checked.get(n.getKey()).length())) {
                        for (Vertex c : n.getNeighbors()) {
                            if (!(this.checked.get(c.getKey()).cardinality() == this.checked.get(c.getKey()).length())) {
                                if (c.isNeighbor(v)) {
                                    clique = new HashSet<>();
                                    clique.add(v.getKey());
                                    clique.add(n.getKey());
                                    clique.add(c.getKey());
                                    if (!cliques.contains(clique)) {
                                        cliques.add(clique);
                                        vn = this.graph.getVertices().get(v.getKey()).getConnectingEdge(this.graph.getVertices().get(n.getKey()));
                                        vc = this.graph.getVertices().get(v.getKey()).getConnectingEdge(this.graph.getVertices().get(c.getKey()));
                                        nc = this.graph.getVertices().get(n.getKey()).getConnectingEdge(this.graph.getVertices().get(c.getKey()));
                                        if ((vn.getCost().get() + vc.getCost().get()) <= nc.getCost().get()) {
                                            toBeRemoved.add(nc);
                                            System.out.println("Removes Edge from clique: [" + nc.getVertices()[0].getKey() + ", "
                                                    + nc.getVertices()[1].getKey() + "]");
                                        } else if ((vn.getCost().get() + nc.getCost().get()) <= vc.getCost().get()) {
                                            toBeRemoved.add(vc);
                                            System.out.println("Removes Edge from clique: [" + vc.getVertices()[0].getKey() + ", "
                                                    + vc.getVertices()[1].getKey() + "]");
                                        } else if ((vc.getCost().get() + nc.getCost().get()) <= vn.getCost().get()) {
                                            toBeRemoved.add(vn);
                                            System.out.println("Removes Edge from clique: [" + vn.getVertices()[0].getKey() + ", "
                                                    + vn.getVertices()[1].getKey() + "]");
                                        } else {
                                            System.out.println("Clique doesn't support Edge Removal");
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                this.checked.get(v.getKey()).set(2);
            }
        }
        for (Edge e : toBeRemoved) {
            this.graph.removeEdge(e);
        }
    }

    // /**
    // * This method looks more complicated than what it actually does. It removes
    // all
    // * Non-terminals with degree 2. It iteratively checks its neighbours until it
    // * finds a Terminal or a Vertex with degree higher than 2. It has to keep
    // track
    // * of subsumed vertices per New Edge. And it has to keep track of all Vertices
    // * to be removed and Edges to be created. This cannot happen concurrently as
    // the
    // * Iterator doesn't allow it, if we do do this it could cause checks on newly
    // * created edges which is unnecessary.
    // */
    // public void removeNonTerminalDegreeTwo() {
    // Set keys = this.graph.getVertices().keySet();
    // Iterator it = keys.iterator();
    // HashMap<Integer, Vertex> vertices = this.graph.getVertices();
    // Stack<int[]> subsumed;
    // HashSet<Integer> toBeRemovedVertices = new HashSet<>();
    // HashSet<Edge> toBeRemovedEdges = new HashSet<>();
    // ArrayList<int[]> newEdges = new ArrayList<>();
    // ArrayList<Stack<int[]>> containedWithinEdge = new ArrayList<>();
    // Vertex current, firstVertex, secondVertex, tempVertex;
    // Edge firstEdge, secondEdge, tempEdge, temp;
    // int cost, currentKey;
    //
    // while (it.hasNext()) {
    // // Gets the current Vertex in the Iterator
    // currentKey = (int) it.next();
    // current = vertices.get(currentKey);
    // // Checks if Vertex is Non-Terminal and degree 2
    // if (!(this.checked.get(currentKey).cardinality() ==
    // this.checked.get(currentKey).length())) {
    // if (!current.isTerminal() && current.getNeighbors().size() == 2 &&
    // !toBeRemovedVertices.contains(current.getKey())) {
    // // Creates a stack to be used for all vertices that will be subsumed by the
    // to
    // // be created Edge
    // subsumed = new Stack<>();
    // cost = 0;
    // // Creating first steps left and right of current to iteratively find a
    // terminal
    // // or degree greater than 2
    // firstVertex = (Vertex) current.getNeighbors().toArray()[0];
    // secondVertex = current.getOtherNeighborVertex(firstVertex);
    // firstEdge = current.getConnectingEdge(firstVertex);
    // secondEdge = current.getConnectingEdge(secondVertex);
    // // Pushes the original two removable Edges in the form of their two keys and
    // // their respective costs
    // subsumed.push(new int[] { current.getKey(), firstVertex.getKey(),
    // firstEdge.getCost().get() });
    // subsumed.push(new int[] { current.getKey(), secondVertex.getKey(),
    // secondEdge.getCost().get() });
    // // The total cost of the new Edge is the sum of the removed Edges
    // cost += firstEdge.getCost().get() + secondEdge.getCost().get();
    // // Keeps a list of the Vertices to be removed, Removal method will also
    // remove
    // // all connected
    // // Edges so no need to store the Edge objects
    // toBeRemovedVertices.add(current.getKey());
    // while (!firstVertex.isTerminal() && firstVertex.getNeighbors().size() == 2) {
    // // Tries the first side of the original Vertex until it finds a Vertex that
    // // doesn't hold to the criteria of this method
    // tempEdge = firstVertex.getOtherEdge(firstEdge);
    // tempVertex = tempEdge.getOtherSide(firstVertex);
    // subsumed.push(new int[] { firstVertex.getKey(), tempVertex.getKey(),
    // tempEdge.getCost().get() });
    // toBeRemovedVertices.add(firstVertex.getKey());
    // cost += tempEdge.getCost().get();
    // firstVertex = tempVertex;
    // firstEdge = tempEdge;
    // }
    // while (!secondVertex.isTerminal() && secondVertex.getNeighbors().size() == 2)
    // {
    // // Tries the second side of the original Vertex until it finds a Vertex that
    // // doesn't hold to the criteria of this method
    // tempEdge = secondVertex.getOtherEdge(secondEdge);
    // tempVertex = tempEdge.getOtherSide(secondVertex);
    // subsumed.push(new int[] { secondVertex.getKey(), tempVertex.getKey(),
    // tempEdge.getCost().get() });
    // toBeRemovedVertices.add(secondVertex.getKey());
    // cost += tempEdge.getCost().get();
    // secondVertex = tempVertex;
    // secondEdge = tempEdge;
    // }
    // boolean edgeExists = false;
    // if (firstVertex.isNeighbor(secondVertex)) {
    // if (cost > firstVertex.getConnectingEdge(secondVertex).getCost().get()) {
    // // Do Nothing the vertices can all be removed because there exists a shorter
    // // path between the two endpoints
    // } else {
    // temp = firstVertex.getConnectingEdge(secondVertex);
    // temp.setCost(cost);
    // temp.pushStack(subsumed);
    // }
    // edgeExists = true;
    // } else {
    // for (int i = 0; i < newEdges.size(); i++) {
    // if (newEdges.get(i)[0] == firstVertex.getKey() && newEdges.get(i)[1] ==
    // secondVertex.getKey()) {
    // if (newEdges.get(i)[2] > cost) {
    // newEdges.get(i)[2] = cost;
    // }
    // edgeExists = true;
    // break;
    // } else if (newEdges.get(i)[0] == firstVertex.getKey() && newEdges.get(i)[1]
    // == secondVertex.getKey()) {
    // if (newEdges.get(i)[2] > cost) {
    // newEdges.get(i)[2] = cost;
    // }
    // edgeExists = true;
    // break;
    // }
    // }
    // }
    // if (!edgeExists) {
    // newEdges.add(new int[] { firstVertex.getKey(), secondVertex.getKey(), cost
    // });
    // containedWithinEdge.add(subsumed);
    // }
    // } else {
    // this.checked.get(currentKey).set(1);
    // }
    // }
    // }
    // for (int i = 0; i < newEdges.size(); i++) {
    // this.checked.get(newEdges.get(i)[0]).set(1, false);
    // this.checked.get(newEdges.get(i)[1]).set(1, false);
    // temp = this.graph.addEdge(newEdges.get(i)[0], newEdges.get(i)[1],
    // newEdges.get(i)[2]);
    // temp.pushStack(containedWithinEdge.get(i));
    // }
    // it = toBeRemovedVertices.iterator();
    // while (it.hasNext()) {
    // currentKey = (int) it.next();
    // this.checked.remove(currentKey);
    // this.graph.removeVertex(this.graph.getVertices().get(currentKey));
    // }
    // it = toBeRemovedEdges.iterator();
    // while (it.hasNext()) {
    // this.graph.removeEdge((Edge) it.next());
    // }
    // }
    //
    // /**
    // * Removes Non-Terminal leaf nodes entirely as they will never be chosen (WE
    // * ASSUME THERE WILL BE NO NON-NEGATIVE EDGES) Removes Terminal leaf nodes and
    // * sets its neighbour to be a terminal to ensure connection
    // */
    // public void removeLeafNodes() {
    // Iterator it = this.graph.getVertices().keySet().iterator();
    // HashMap<Integer, Vertex> vertices = this.graph.getVertices();
    // HashSet<Vertex> toBeRemoved = new HashSet<>();
    // Vertex current, newCurrent, temp;
    // int currentKey;
    // System.out.println("starting");
    //
    // while (it.hasNext()) {
    // currentKey = (int) it.next();
    // if (!(this.checked.get(currentKey).cardinality() ==
    // this.checked.get(currentKey).length())) {
    // current = vertices.get(currentKey);
    // if (!current.isTerminal() && current.getNeighbors().size() == 1) {
    // toBeRemoved.add(current);
    // newCurrent = (Vertex) current.getNeighbors().toArray()[0];
    // while (!newCurrent.isTerminal() && newCurrent.getNeighbors().size() == 2) {
    // temp = newCurrent.getOtherNeighborVertex(current);
    // current = newCurrent;
    // newCurrent = temp;
    // toBeRemoved.add(current);
    // }
    // this.checked.get(newCurrent.getKey()).set(0, false);
    // } else if (current.isTerminal() && current.getNeighbors().size() == 1) {
    // toBeRemoved.add(current);
    // newCurrent = (Vertex) current.getNeighbors().toArray()[0];
    // while (newCurrent.isTerminal() && newCurrent.getNeighbors().size() == 2) {
    // temp = newCurrent.getOtherNeighborVertex(current);
    // current = newCurrent;
    // newCurrent = temp;
    // if (current.getSubsumed() != null) {
    // if (current.getSubsumed().size() > 0) {
    // newCurrent.pushStack(current.getSubsumed());
    // }
    // }
    // newCurrent.pushSubsumed(
    // new int[] { newCurrent.getKey(), current.getKey(), ((Edge)
    // (current.getEdges().toArray()[0])).getCost().get() });
    // this.graph.setTerminal(newCurrent.getKey());
    // current = newCurrent;
    // toBeRemoved.add(current);
    // }
    // System.out.println(newCurrent.getKey());
    // for (Vertex nb : newCurrent.getNeighbors())
    // System.out.print(nb.getKey() + " ");
    // System.out.println();
    // System.out.println(this.graph.getVertices().containsKey(newCurrent.getKey()));
    // System.out.println(this.checked.get(newCurrent.getKey()));
    // this.checked.get(newCurrent.getKey()).set(0, false);
    // } else {
    // this.checked.get(currentKey).set(0);
    // }
    // }
    // }
    // it = toBeRemoved.iterator();
    // while (it.hasNext()) {
    // current = (Vertex) it.next();
    // System.out.println(current.getKey());
    // this.checked.remove(current.getKey());
    // this.graph.removeVertex(current);
    // }
    // }
    /**
     * This method looks more complicated than what it actually does. It removes
     * all Non-terminals with degree 2. It iteratively checks its neighbours
     * until it finds a Terminal or a Vertex with degree higher than 2. It has
     * to keep track of subsumed vertices per New Edge. And it has to keep track
     * of all Vertices to be removed and Edges to be created. This cannot happen
     * concurrently as the Iterator doesn't allow it, if we do do this it could
     * cause checks on newly created edges which is unnecessary.
     */
    public void removeNonTerminalDegreeTwo() {
        Set keys = this.graph.getVertices().keySet();
        Iterator it = keys.iterator();
        HashMap<Integer, Vertex> vertices = this.graph.getVertices();
        Stack<int[]> subsumed;
        HashSet<Integer> toBeRemovedVertices = new HashSet<>();
        HashSet<Edge> toBeRemovedEdges = new HashSet<>();
        ArrayList<int[]> newEdges = new ArrayList<>();
        ArrayList<Stack<int[]>> containedWithinEdge = new ArrayList<>();
        Vertex current, firstVertex, secondVertex, tempVertex;
        Edge firstEdge, secondEdge, tempEdge, temp;
        int cost;
        while (it.hasNext()) {
            // Gets the current Vertex in the Iterator
            current = vertices.get((int) it.next());
            // Checks if Vertex is Non-Terminal and degree 2
            if (!current.isTerminal() && current.getNeighbors().size() == 2 && !toBeRemovedVertices.contains(current.getKey())) {
                // Creates a stack to be used for all vertices that will be subsumed by the to
                // be created Edge
                subsumed = new Stack<>();
                cost = 0;
                // Creating first steps left and right of current to iteratively find a terminal
                // or degree greater than 2
                firstVertex = (Vertex) current.getNeighbors().toArray()[0];
                secondVertex = current.getOtherNeighborVertex(firstVertex);
                firstEdge = current.getConnectingEdge(firstVertex);
                secondEdge = current.getConnectingEdge(secondVertex);
                // Pushes the original two removable Edges in the form of their two keys and
                // their respective costs
                subsumed.push(new int[]{current.getKey(), firstVertex.getKey(), firstEdge.getCost().get()});
                subsumed.push(new int[]{current.getKey(), secondVertex.getKey(), secondEdge.getCost().get()});
                // The total cost of the new Edge is the sum of the removed Edges
                cost += firstEdge.getCost().get() + secondEdge.getCost().get();
                // Keeps a list of the Vertices to be removed, Removal method will also remove
                // all connected
                // Edges so no need to store the Edge objects
                toBeRemovedVertices.add(current.getKey());
                while (!firstVertex.isTerminal() && firstVertex.getNeighbors().size() == 2) {
                    // Tries the first side of the original Vertex until it finds a Vertex that
                    // doesn't hold to the criteria of this method

                    tempEdge = firstVertex.getOtherEdge(firstEdge);
                    tempVertex = tempEdge.getOtherSide(firstVertex);
                    subsumed.push(new int[]{firstVertex.getKey(), tempVertex.getKey(), tempEdge.getCost().get()});
                    toBeRemovedVertices.add(firstVertex.getKey());
                    cost += tempEdge.getCost().get();
                    firstVertex = tempVertex;
                    firstEdge = tempEdge;
                }
                while (!secondVertex.isTerminal() && secondVertex.getNeighbors().size() == 2) {
                    // Tries the second side of the original Vertex until it finds a Vertex that
                    // doesn't hold to the criteria of this method

                    tempEdge = secondVertex.getOtherEdge(secondEdge);
                    tempVertex = tempEdge.getOtherSide(secondVertex);
                    subsumed.push(new int[]{secondVertex.getKey(), tempVertex.getKey(), tempEdge.getCost().get()});
                    toBeRemovedVertices.add(secondVertex.getKey());
                    cost += tempEdge.getCost().get();
                    secondVertex = tempVertex;
                    secondEdge = tempEdge;
                }
                // YOU NEED TO THINK ABOUT THIS MORE THERE COULD BE MULTIPLE NEW EDGES ADDED
                // THAT CREATE THE SAME PATH
                // COST SHOULD BE COMPARED TO SEE IF WE NEED TO REMOVE AN ORIGINAL EDGE OR NOT
                // ADD ANOTHER EDGE AT ALL
                boolean edgeExists = false;
                if (firstVertex.isNeighbor(secondVertex)) {
                    if (cost > firstVertex.getConnectingEdge(secondVertex).getCost().get()) {
                        // Do Nothing the vertices can all be removed because there exists a shorter
                        // path between the two endpoints
                    } else {
                        temp = firstVertex.getConnectingEdge(secondVertex);
                        temp.setCost(cost);
                        temp.pushStack(subsumed);
                    }
                    edgeExists = true;
                } else {
                    for (int i = 0; i < newEdges.size(); i++) {
                        if (newEdges.get(i)[0] == firstVertex.getKey() && newEdges.get(i)[1] == secondVertex.getKey()) {
                            if (newEdges.get(i)[2] > cost) {
                                newEdges.get(i)[2] = cost;
                            }
                            edgeExists = true;
                            break;
                        } else if (newEdges.get(i)[1] == firstVertex.getKey() && newEdges.get(i)[0] == secondVertex.getKey()) {
                            if (newEdges.get(i)[2] > cost) {
                                newEdges.get(i)[2] = cost;
                            }
                            edgeExists = true;
                            break;
                        }
                    }
                }
                if (!edgeExists) {
                    newEdges.add(new int[]{firstVertex.getKey(), secondVertex.getKey(), cost});
                    containedWithinEdge.add(subsumed);
                }
            }
        }

        for (int i = 0; i < newEdges.size(); i++) {
            temp = this.graph.addEdge(newEdges.get(i)[0], newEdges.get(i)[1], newEdges.get(i)[2]);
            temp.pushStack(containedWithinEdge.get(i));
            if (temp.getVertices()[0] == null || temp.getVertices()[1] == null) {
                System.out.println(temp.getVertices()[0] + "  " + temp.getVertices()[1]);
            }
        }
        it = toBeRemovedVertices.iterator();
        while (it.hasNext()) {
            this.graph.removeVertex(this.graph.getVertices().get((int) it.next()));
            it.remove();
        }
        it = toBeRemovedEdges.iterator();
        while (it.hasNext()) {
            this.graph.removeEdge((Edge) it.next());
        }
    }

    /**
     * Removes Non-Terminal leaf nodes entirely as they will never be chosen (WE
     * ASSUME THERE WILL BE NO NON-NEGATIVE EDGES) Removes Terminal leaf nodes
     * and sets its neighbour to be a terminal to ensure connection
     */
    public void removeLeafNodes() {
        Set keys = this.graph.getVertices().keySet();
        Iterator it = keys.iterator();
        HashMap<Integer, Vertex> vertices = this.graph.getVertices();
        HashSet<Vertex> toBeRemoved = new HashSet<>();
        Vertex current, newCurrent, temp;
        while (it.hasNext()) {
            current = vertices.get((int) it.next());
            if (!current.isTerminal() && current.getNeighbors().size() == 1) {
                toBeRemoved.add(current);
                newCurrent = (Vertex) current.getNeighbors().toArray()[0];
                while (!newCurrent.isTerminal() && newCurrent.getNeighbors().size() == 2) {
                    temp = newCurrent.getOtherNeighborVertex(current);
                    current = newCurrent;
                    newCurrent = temp;
                    toBeRemoved.add(current);
                }
            }
            if (current.isTerminal() && current.getNeighbors().size() == 1) {
                toBeRemoved.add(current);
                newCurrent = (Vertex) current.getNeighbors().toArray()[0];
                newCurrent.setTerminal(true);
                // if (newCurrent.getSubsumed() != null) {
                // if (newCurrent.getSubsumed().size() > 0) {
                // newCurrent.pushStack(current.getSubsumed());
                // }
                // }
                if (current.getConnectingEdge(newCurrent).getStack() != null) {
                    if (current.getConnectingEdge(newCurrent).getStack().size() > 0) {
                        newCurrent.pushStack(current.getConnectingEdge(newCurrent).getStack());
                    }
                    if (current.getSubsumed() != null) {
                        newCurrent.pushStack(current.getSubsumed());
                    }
                } else {
                    if (current.getSubsumed() != null) {
                        newCurrent.pushStack(current.getSubsumed());
                    }
                    newCurrent
                            .pushSubsumed(new int[]{newCurrent.getKey(), current.getKey(), current.getConnectingEdge(newCurrent).getCost().get()});

                }
                newCurrent.setTerminal(true);
                while (newCurrent.isTerminal() && newCurrent.getNeighbors().size() == 2) {
                    temp = newCurrent.getOtherNeighborVertex(current);
                    current = newCurrent;
                    newCurrent = temp;
                    // if (current.getSubsumed() != null) {
                    // if (current.getSubsumed().size() > 0) {
                    // newCurrent.pushStack(current.getSubsumed());
                    // }
                    // }
                    if (current.getConnectingEdge(newCurrent).getStack() != null) {
                        if (current.getConnectingEdge(newCurrent).getStack().size() > 0) {
                            newCurrent.pushStack(current.getConnectingEdge(newCurrent).getStack());
                        }
                        if (current.getSubsumed() != null) {
                            newCurrent.pushStack(current.getSubsumed());
                        }
                    } else {
                        if (current.getSubsumed() != null) {
                            newCurrent.pushStack(current.getSubsumed());
                        }
                        newCurrent.pushSubsumed(
                                new int[]{newCurrent.getKey(), current.getKey(), current.getConnectingEdge(newCurrent).getCost().get()});

                    }
                    toBeRemoved.add(current);
                    newCurrent.setTerminal(true);

                }
                this.graph.setTerminal(newCurrent.getKey());
            }
        }
        it = toBeRemoved.iterator();
        while (it.hasNext()) {
            this.graph.removeVertex((Vertex) it.next());
        }
    }

    /**
     * Finds and returns all articulation points in the graph.
     *
     * @param v0 The starting vertex
     * @param totalVertices The total amount of vertices in the graph
     * @return A list of Vertices
     *
     * @author Joshua Scheidt
     */
    public HashSet<Vertex> articulationPointFinding(Vertex v0, int totalVertices) {
        int count = 1;
        int[] iteratedValues = new int[totalVertices];
        int[] lowestFoundLabels = new int[totalVertices];
        HashSet<Vertex> articulationBridge = new HashSet<>();
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
                    if (next != parent) {
                        lowestFoundLabels[current.getKey() - 1] = Math.min(lowestFoundLabels[current.getKey() - 1],
                                lowestFoundLabels[next.getKey() - 1]); // Set current lowest to go to lowest neighbor
                    }
                    if (!it.hasNext()) {
                        if (lowestFoundLabels[current.getKey() - 1] == iteratedValues[current.getKey() - 1] && parent != fake) {
                            articulationBridge.add(current);
                            // articulationBridge.add(parent); //Use when both bridge endpoints need to be
                            // articulation points.
                        } else if (parent != fake && lowestFoundLabels[current.getKey() - 1] >= iteratedValues[parent.getKey() - 1]) {
                            articulationBridge.add(parent);
                        }
                        stack.pop();
                        break;
                    }
                }
            }
        }
        if (v0.getNeighbors().size() > 1) {
            int val = iteratedValues[v0.getKey() - 1];
            boolean remove = true;
            for (Vertex v : v0.getNeighbors()) {
                if (lowestFoundLabels[v.getKey() - 1] != val) {
                    remove = false;
                    break;
                }
            }
            if (remove) {
                articulationBridge.remove(v0);
            }
        }

        return articulationBridge;
    }

    /**
     * Performs an analysis on the section to check which vertices, terminals,
     * articulation points and edges lie within a section, and will afterwards
     * call shortest path if it reduces the number of edges.
     *
     * @param artiPoints The articulation points in the graph
     *
     * @author Joshua Scheidt
     */
    public void analyseSections(HashSet<Vertex> artiPoints) {
        // Map to keep track of all the already visited vertices in the set.
        // These will hold only the vertices which are not articulation points.
        Map<Vertex, Boolean> hasVisited;
        // The stack used for replacement of recursion
        Stack<Vertex> stack = new Stack<>();
        // The next and parent vertices in the stack
        Vertex next, parent;
        // Temporary vertex used for moving other vertices around
        Vertex tmp;
        // The iterator of the neighbours of the stack's last element
        Iterator<Vertex> it;
        // Map in a Map which shows for every articulation point which neighbours have
        // been checked (true=checked)
        Map<Vertex, Map<Vertex, Boolean>> artiNbCheck = new HashMap<>();
        for (Vertex v : artiPoints) {
            Map<Vertex, Boolean> map = new HashMap<>();
            for (Vertex nb : v.getNeighbors()) {
                map.put(nb, false);
            }
            artiNbCheck.put(v, map);
        }

        // VerticesInSection, TerminalsInSection, ArticulationInSection and
        // EdgesInSection
        // If a terminals is an articulation as well, it will not be added to tis.
        Set<Vertex> vis, tis, ais;
        Set<Edge> eis;

        for (Vertex arti : artiPoints) {
            stack.push(arti);
            parent = arti;
            checkArti:
            while (artiNbCheck.get(arti).values().contains(false)) {
                hasVisited = new HashMap<>();
                vis = new HashSet<>();
                tis = new HashSet<>();
                ais = new HashSet<>();
                eis = new HashSet<>();
                ais.add(arti);

                nbCheck:
                for (Vertex nb : arti.getNeighbors()) {
                    if (artiNbCheck.get(arti).containsKey(nb) && !artiNbCheck.get(arti).get(nb)) {

                        // The neighbour of the articulationPoint is an articulation as well.
                        // Do nothing
                        if (artiPoints.contains(nb)) {
                            artiNbCheck.get(arti).put(nb, true);
                            artiNbCheck.get(nb).put(arti, true);
                        } else { // Found new unvisited neighbour
                            stack.push(nb);
                            artiNbCheck.get(arti).put(nb, true);
                            if (nb.isTerminal()) {
                                tis.add(nb);
                            } else {
                                vis.add(nb);
                            }
                            hasVisited.put(nb, true);
                            eis.add(arti.getConnectingEdge(nb));
                            break nbCheck;
                        }
                    }
                }
                if (stack.size() > 2) {
                    System.out.println("Something went wrong in the neighbour checking");
                    System.exit(1);
                } else if (stack.size() == 1) {
                    break checkArti;
                } else { // Stack now contains arti and 1 neighbour
                    while (stack.size() > 1) { // If size is 1, then only arti available
                        it = stack.peek().getNeighbors().iterator();
                        if (!it.hasNext()) { // Current item on the stack has no neighbours
                            System.out.println("The current stack item does not have any neighbours");
                            System.exit(1);
                        }
                        nbLoop:
                        while (it.hasNext()) {
                            next = it.next();
                            if (parent == next) { // Found parent again, do nothing
                                // System.out.println("Parent found");
                            } else if (artiPoints.contains(next) && !artiNbCheck.get(next).get(stack.peek())) { // My next vertex is an arti point
                                artiNbCheck.get(next).put(stack.peek(), true);
                                ais.add(next);
                                eis.add(next.getConnectingEdge(stack.peek()));
                            } else if (!eis.contains(stack.peek().getConnectingEdge(next))) { // Current edge not in set yet
                                eis.add(stack.peek().getConnectingEdge(next));
                                if (!hasVisited.containsKey(next) || !hasVisited.get(next)) { // Neighbour is unvisited
                                    hasVisited.put(next, true);
                                    if (next.isTerminal()) {
                                        tis.add(next);
                                    } else {
                                        vis.add(next);
                                    }
                                    parent = stack.peek();
                                    stack.push(next);
                                    break nbLoop;
                                }
                            }
                            if (!it.hasNext()) {
                                stack.pop(); // Iterator went through all neighbours, thus backtrack.
                                tmp = stack.pop();
                                parent = (stack.size() == 0 ? arti : stack.peek());
                                stack.push(tmp);
                                break nbLoop;
                            }
                        }
                    }
                    // We are back to articulation point, thus end of section
                    if (((tis.size() * (tis.size() - 1) / 2) + (ais.size() * (ais.size() - 1) / 2) + (tis.size() * ais.size())) < eis.size()) {
                        ArrayList<Vertex> v = new ArrayList<>();
                        v.addAll(vis);
                        ArrayList<Vertex> t = new ArrayList<>();
                        t.addAll(tis);
                        ArrayList<Vertex> b = new ArrayList<>();
                        b.addAll(ais);
                        ArrayList<Edge> e = new ArrayList<>();
                        e.addAll(eis);
                        this.reduceSection(v, t, b, e);
                    }
                }

            }
            stack.removeAllElements();
        }
    }

    /**
     * Creates per found section in the graph a new UndirectedGraph object.
     *
     * @param artiPoints The articulation points in the graph
     *
     * @author Joshua Scheidt
     */
    public ArrayList<UndirectedGraph> createSeparateSections(Vertex v0, int totalVertices) {
        // First start finding all bridges
        int count = 1;
        int[] iteratedValues = new int[totalVertices];
        int[] lowestFoundLabels = new int[totalVertices];
        HashSet<Vertex> bridgeNodes = new HashSet<>();
        HashSet<Edge> bridges = new HashSet<>();
        Stack<Vertex> stack = new Stack<>();
        Vertex fake = new Vertex(0); // Fake vertex to use as the parent of the initial vertex v0
        stack.push(fake);
        stack.push(v0);
        iteratedValues[v0.getKey() - 1] = count;
        lowestFoundLabels[v0.getKey() - 1] = count;
        count++;
        Vertex current, parent, next;
        Iterator<Vertex> it;
        boolean backtracking = false;

        while (stack.size() > 1) { // If the stack is 1, we are back to the fake vertex -> stop
            current = stack.pop();
            parent = stack.peek();
            stack.push(current); // We need to use this trick to get the parent
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
                    if (next != parent) {
                        lowestFoundLabels[current.getKey() - 1] = Math.min(lowestFoundLabels[current.getKey() - 1],
                                lowestFoundLabels[next.getKey() - 1]); // Set current lowest to go to lowest neighbor
                    }
                    if (!it.hasNext()) {
                        if (lowestFoundLabels[current.getKey() - 1] == iteratedValues[current.getKey() - 1] && parent != fake) { // Found a bridge
                            bridges.add(current.getConnectingEdge(parent));
                        }
                        stack.pop();
                        break;
                    }
                }
            }
        }
        Set<Integer> bridgePoints = new HashSet<>();
        for (Edge e : bridges) {
            bridgePoints.add(e.getVertices()[0].getKey());
            bridgePoints.add(e.getVertices()[1].getKey());
            // System.out.println("Bridge: " + e.getVertices()[0].getKey() + " " +
            // e.getVertices()[1].getKey() + " " + e.getCost().get());
        }

        // Create separate sections for purely the removal of bridges:
        ArrayList<UndirectedGraph> subGraphs = new ArrayList<>();
        // Map to keep track of all the already visited vertices in the set.
        // These will hold only the vertices which are not articulation points.
        Set<Vertex> hasVisitedNode = new HashSet<>();
        Set<Edge> hasVisitedEdge = new HashSet<>(); // Bridges will never be added to this list due to terminal checking for bridge
        // endpoints
        // The stack used for replacement of recursion
        stack = new Stack<>();
        // Temporary vertex used for moving other vertices around
        Vertex tmp;
        // VerticesInSection, TerminalsInSection, BridgesInSection and
        // EdgesInSection
        Set<Vertex> vis, tis, bis;
        Set<Edge> eis;
        Map<UndirectedGraph, Set<Integer>> bridgePointsPerGraph = new HashMap<>();
        if (bridges.size() > 0) {
            for (Edge bridge : bridges) {
                for (Vertex bridgePoint : bridge.getVertices()) {
                    // There is already a subgraph with this bridge endpoint
                    if (hasVisitedNode.contains(bridgePoint)) {
                        continue;
                    }
                    // This node is new -> instantiate everything and create section.
                    vis = new HashSet<>();
                    tis = new HashSet<>();
                    bis = new HashSet<>();
                    eis = new HashSet<>();
                    stack.push(bridgePoint);
                    stackLoop:
                    while (!stack.isEmpty()) {
                        current = stack.peek();
                        if (!hasVisitedNode.contains(current)) { // First time visiting this node
                            vis.add(current);
                            hasVisitedNode.add(current);
                            if (current.isTerminal()) {
                                tis.add(current);
                            }
                        }
                        for (Edge e : current.getEdges()) {
                            if (!hasVisitedEdge.contains(e)) { // Unvisited edge
                                if (bridges.contains(e)) { // Found a bridge
                                    if (!hasVisitedNode.contains(e.getOtherSide(current))) {// The node is unexplored. This section will get the
                                        // bridge.
                                        vis.add(e.getOtherSide(current));
                                        // System.out.println(e.getOtherSide(current).getKey());
                                        // tis.add(e.getOtherSide(current)); // This is an endpoint -> Make it a
                                        // terminal. Later recheck if the neighboring
                                        // section is leaf and doesn't contain terminals, then remove if it isn't an
                                        // actual terminal.
                                        eis.add(e); // Same holds as above, this edge is unneeded if the neighboring section is leaf
                                        // without terminals.
                                        if (e.getOtherSide(current).getEdges().size() > 1) {
                                            bis.add(e.getOtherSide(current)); // Only add the bridgePoint which will be used to connect the sections
                                        }																				// and only add it as bridge if it isn't leaf
                                        if (e.getOtherSide(current).isTerminal()) {
                                            tis.add(e.getOtherSide(current));
                                        }
                                    } else {
                                        // tis.add(current);
                                        bis.add(current);
                                    }
                                } else { // This is a normal unvisited edge
                                    eis.add(e);
                                    hasVisitedEdge.add(e);
                                    if (!hasVisitedNode.contains(e.getOtherSide(current))) { // The node isn't visited yet, add to stack
                                        stack.push(e.getOtherSide(current));
                                        continue stackLoop;
                                    }
                                }
                            }
                        }
                        // No more unvisited edges, go to previous
                        stack.pop();
                    }
                    if (!eis.isEmpty()) { // All sections must have at least one edge. If not, it will already be
                        // incorporated in another section
                        // Found complete section, create it
                        UndirectedGraph subGraph = new UndirectedGraph();
                        for (Edge e : eis) {
                            subGraph.addEdge(e.getVertices()[0].getKey(), e.getVertices()[1].getKey(), e.getCost().get());
                            if (e.getStack() != null) {
                                subGraph.getVertices().get(e.getVertices()[0].getKey())
                                        .getConnectingEdge(subGraph.getVertices().get(e.getVertices()[1].getKey())).pushStack(e.getStack());
                            }
                        }
                        for (Vertex t : tis) {
                            subGraph.setTerminal(t.getKey());
                        }
                        for (Vertex v : vis) {
                            if (v.getSubsumed() != null) {
                                subGraph.getVertices().get(v.getKey()).pushStack(v.getSubsumed());
                            }
                        }
                        subGraphs.add(subGraph);
                        Set<Integer> bps = new HashSet<>();
                        for (Vertex bp : bis) {
                            bps.add(bp.getKey());
                        }
                        bridgePointsPerGraph.put(subGraph, bps);
                    }
                }
            }
            // for (UndirectedGraph sub : subGraphs) {
            // System.out.println("Section:");
            // System.out.println(sub);
            // for (Edge e : sub.getEdges())
            // System.out.println(e.getVertices()[0].getKey() + " " +
            // e.getVertices()[1].getKey() + " " + e.getCost().get());
            // for (Vertex t : sub.getTerminals().values())
            // System.out.println("Terminal: " + t.getKey());
            // for (Integer b : bridgePointsPerGraph.get(sub))
            // System.out.println("Bridge: " + b);
            // }
            // System.out.println("\n\n");
            // Found all sections purely on bridges. Next step: Check each section if they
            // are leaf and if they have at least one terminal which isn't the bridge.
            Stack<UndirectedGraph> toCheck = new Stack<>();
            Stack<UndirectedGraph> toRemove = new Stack<>();
            for (int i = 0; i < subGraphs.size(); i++) {
                toCheck.push(subGraphs.get(i));
            }
            while (!toCheck.isEmpty()) {
                UndirectedGraph sub = toCheck.pop();
                if (bridgePointsPerGraph.get(sub).size() == 1) { // There is only a single bridge -> Leaf section
                    if (sub.getTerminals().size() == 0) {// remove section and remove the corresponding bridgePoint from neighbouring
                        // section
                        toRemove.add(sub);
                    }
                    if (sub.getTerminals().size() >= 1 && sub.getTerminals().size() <= 2) { // If we have at most 2 terminals, we can possible remove
                        // the
                        // section or use shortest path to get the single path if
                        // the
                        // bridge is also a terminal.
                        Set<Integer> intersection = new HashSet<>(sub.getTerminals().keySet());
                        intersection.removeAll(bridgePointsPerGraph.get(sub));
                        if (intersection.size() == 0) // Bridge is the only terminal -> Is already in other set.
                        {
                            toRemove.add(sub);
                        } else if (intersection.size() == 1) { // Connect shortest path from bridge to terminal
                            Integer bridgePoint = bridgePointsPerGraph.get(sub).iterator().next(); // Only 1 bridgePoint
                            List<Edge> shortestPath = PathFinding.DijkstraSinglePath(sub, sub.getVertices().get(bridgePoint),
                                    sub.getVertices().get(intersection.iterator().next()));
                            List<Edge> removeEdges = new ArrayList<>(sub.getEdges());
                            removeEdges.removeAll(shortestPath);
                            for (Edge e : removeEdges) {
                                sub.removeEdge(e);
                            }
                            // System.out.println("Changed");
                        } // If intersection is 2. Then there are 2 terminals in the subGraph, let solver
                        // do the work.
                    }
                }
                if (toCheck.size() == 0) { // Went through all checks. Start removing
                    while (!toRemove.isEmpty()) {
                        UndirectedGraph remover = toRemove.pop();
                        if (bridgePointsPerGraph.size() == 0) {
                            toCheck.clear();
                            break;
                        }
                        Integer bridgePoint = bridgePointsPerGraph.get(remover).iterator().next(); // Only 1 bridgePoint
                        UndirectedGraph nb = null;
                        // System.out.println(bridgePoint);
                        for (UndirectedGraph nbCheck : subGraphs) {
                            if (nbCheck == remover) {
                                continue;
                            }
                            // System.out.println(Arrays.toString(bridgePointsPerGraph.get(nbCheck).toArray()));
                            if (bridgePointsPerGraph.get(nbCheck).contains(bridgePoint)) {
                                // System.out.println("Found it");
                                nb = nbCheck;
                                break;
                            }
                        }
                        toCheck.push(nb);
                        bridgePointsPerGraph.remove(remover);
                        subGraphs.remove(remover);
                        if (nb != null) {
                            Set<Integer> nbBps = bridgePointsPerGraph.get(nb); // Get the neighbours bridgePoints, remove the one from the leaf
                            // section
                            // and replace.
                            nbBps.remove(bridgePoint);
                            bridgePointsPerGraph.replace(nb, nbBps);
                        }
                    }
                }
            }

            // Lastly, make the bridgePoints as terminals, to ensure that solvers will
            // connect properly:
            for (UndirectedGraph sub : subGraphs) {
                for (Integer i : bridgePointsPerGraph.get(sub)) {
                    sub.setTerminal(i);
                }
            }
        } else {
            subGraphs.add(this.graph.clone());
        }
        // System.out.println("\n\n");
        // for (UndirectedGraph sub : subGraphs) {
        // System.out.println("Section:");
        // for (Edge e : sub.getEdges())
        // System.out.println(e.getVertices()[0].getKey() + " " +
        // e.getVertices()[1].getKey() + " " + e.getCost().get());
        // for (Vertex t : sub.getTerminals().values())
        // System.out.println("Terminal: " + t.getKey());
        // if (bridgePointsPerGraph.get(sub) != null)
        // for (Integer b : bridgePointsPerGraph.get(sub))
        // System.out.println("Bridge: " + b);
        // }
        // System.out.println("Sections remaining:" + subGraphs.size());

        // Sections are now quite separated for bridges.
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Now find the articulation points and eventually reduce even further.
        ArrayList<UndirectedGraph> resultGraphs = new ArrayList<>();
        HashMap<UndirectedGraph, Set<Integer>> artiPointsPerGraph = new HashMap<>();
        // Now start finding all articulation points
        for (UndirectedGraph sub : subGraphs) {
            HashSet<Vertex> originalArtiPoints = null;
            HashSet<Vertex> artiPoints = new HashSet<>();
            artiPoints.add(fake);
            UndirectedGraph remaining = sub.clone();
            // Iteratively go through the section until no more articulation points occur ->
            // Should be when there are no more leaf sections
            while (artiPoints.size() > 0 && remaining.getEdges().size() > 0) {
                artiPoints.clear();
                count = 1;
                v0 = remaining.getVertices().get(remaining.getVertices().keySet().iterator().next());
                iteratedValues = new int[totalVertices];
                lowestFoundLabels = new int[totalVertices];
                stack = new Stack<>();
                fake = new Vertex(0); // Fake vertex to use as the parent of the initial vertex v0
                stack.push(fake);
                stack.push(v0);
                iteratedValues[v0.getKey() - 1] = count;
                lowestFoundLabels[v0.getKey() - 1] = count;
                count++;
                backtracking = false;

                while (stack.size() > 1) { // If the stack is 1, we are back to the fake vertex -> stop
                    current = stack.pop();
                    parent = stack.peek();
                    stack.push(current); // We need to use this trick to get the parent
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
                        if (!it.hasNext()) {
                            stack.pop();
                        }
                        while (it.hasNext()) {
                            next = it.next();
                            if (next != parent) {
                                lowestFoundLabels[current.getKey() - 1] = Math.min(lowestFoundLabels[current.getKey() - 1],
                                        lowestFoundLabels[next.getKey() - 1]); // Set current lowest to go to lowest neighbor
                            }
                            if (!it.hasNext()) {
                                if (parent != fake && lowestFoundLabels[current.getKey() - 1] >= iteratedValues[parent.getKey() - 1]
                                        && parent.getEdges().size() > 2) { // Found an articulation
                                    artiPoints.add(parent);
                                }
                                stack.pop();
                                break;
                            }
                        }
                    }
                }
                if (v0.getNeighbors().size() > 1) { // Special case: Unless first node is an actual articulation point, remove from
                    // articulation points. This was due to some implementation strategy that v0 was
                    // almost always chosen as articulation
                    int val = iteratedValues[v0.getKey() - 1];
                    boolean remove = true;
                    for (Vertex v : v0.getNeighbors()) {
                        if (lowestFoundLabels[v.getKey() - 1] != val) {
                            remove = false;
                            break;
                        }
                    }
                    if (remove) {
                        artiPoints.remove(v0);
                    }
                }
                // Found the articulation points of this graph.
                // If none are found, then the section cannot be reduced in size:
                // System.out.println("Arti's:");
                // for (Vertex a : artiPoints)
                // System.out.println(a.getKey());
                // Set the original arti's. Only happens once.
                if (originalArtiPoints == null) {
                    originalArtiPoints = artiPoints;
                }

                // The two cases: There are no arti's -> Single section
                if (artiPoints.size() == 0) {
                    resultGraphs.add(remaining);
                } else { // It is possible to make sections from here.
                    hasVisitedEdge = new HashSet<>();
                    hasVisitedNode = new HashSet<>();
                    ArrayList<UndirectedGraph> sectionSubs = new ArrayList<>(); // The results for this section. All remaining in this will be added
                    // to
                    // resultGraphs.
                    HashSet<Vertex> ais;
                    for (Vertex arti : artiPoints) {
                        for (Edge sectionEdge : arti.getEdges()) {
                            if (hasVisitedEdge.contains(sectionEdge)) // Visited edge -> No new section
                            {
                                continue;
                            }
                            stack.clear();
                            stack.push(arti);
                            stack.push(sectionEdge.getOtherSide(arti));
                            eis = new HashSet<>();
                            eis.add(sectionEdge);
                            hasVisitedEdge.add(sectionEdge);
                            vis = new HashSet<>();
                            vis.add(arti);
                            tis = new HashSet<>();
                            if (arti.isTerminal()) {
                                tis.add(arti);
                            }
                            ais = new HashSet<>();
                            ais.add(arti);
                            sectionLoop:
                            while (stack.size() > 1) {
                                current = stack.peek();
                                if (artiPoints.contains(current)) {// Current node is articulation -> pop and continue
                                    ais.add(current);
                                    stack.pop();
                                    continue sectionLoop;
                                }
                                if (!hasVisitedNode.contains(current)) {
                                    vis.add(current);
                                    if (current.isTerminal()) {
                                        tis.add(current);
                                    }
                                    hasVisitedNode.add(current);
                                }
                                edgesCheck:
                                for (Edge e : current.getEdges()) {
                                    if (hasVisitedEdge.contains(e)) {
                                        continue edgesCheck;
                                    }
                                    eis.add(e);
                                    hasVisitedEdge.add(e);
                                    if (!hasVisitedNode.contains(e.getOtherSide(current))) {
                                        stack.push(e.getOtherSide(current));
                                        continue sectionLoop;
                                    }
                                }
                                // Went over all edges, but found no new nodes
                                stack.pop();
                            }
                            // Returned back to articulation node -> Section is done.
                            UndirectedGraph subGraph = new UndirectedGraph();
                            for (Edge e : eis) {
                                subGraph.addEdge(e.getVertices()[0].getKey(), e.getVertices()[1].getKey(), e.getCost().get());
                                if (e.getStack() != null) {
                                    subGraph.getVertices().get(e.getVertices()[0].getKey())
                                            .getConnectingEdge(subGraph.getVertices().get(e.getVertices()[1].getKey())).pushStack(e.getStack());
                                }
                            }
                            for (Vertex t : tis) {
                                subGraph.setTerminal(t.getKey());
                            }
                            for (Vertex v : vis) {
                                if (v.getSubsumed() != null) {
                                    subGraph.getVertices().get(v.getKey()).pushStack(v.getSubsumed());
                                }
                            }
                            sectionSubs.add(subGraph);
                            Set<Integer> aps = new HashSet<>();
                            for (Vertex ap : ais) {
                                aps.add(ap.getKey());
                            }
                            artiPointsPerGraph.put(subGraph, aps);
                        }
                    }
                    remaining = new UndirectedGraph();
                    Set<UndirectedGraph> toAdd = new HashSet<>();

                    for (UndirectedGraph sectionSub : sectionSubs) {
                        if (artiPointsPerGraph.get(sectionSub).size() == 1) {
                            // Encountered leaf section
                            if (sectionSub.getTerminals().size() >= 1 && sectionSub.getTerminals().size() <= 2) { // If we have at most 2 terminals,
                                // we can possible
                                // remove the section or use shortest path to get
                                // the single path if the articulation is also a
                                // terminal.
                                Set<Integer> intersection = new HashSet<>(sectionSub.getTerminals().keySet());
                                intersection.removeAll(artiPointsPerGraph.get(sectionSub));
                                if (intersection.size() == 0) // arti is the only terminal -> Is already in other set.
                                {
                                    continue;
                                } else if (intersection.size() == 1) { // Connect shortest path from arti to terminal
                                    Integer artiPoint = artiPointsPerGraph.get(sectionSub).iterator().next(); // Only 1 artiPoint
                                    List<Edge> shortestPath = PathFinding.DijkstraSinglePath(sectionSub, sectionSub.getVertices().get(artiPoint),
                                            sectionSub.getVertices().get(intersection.iterator().next()));
                                    List<Edge> removeEdges = new ArrayList<>(sectionSub.getEdges());
                                    removeEdges.removeAll(shortestPath);
                                    for (Edge e : removeEdges) {
                                        sectionSub.removeEdge(e);
                                    }
                                    // The articulation point is needed-> Make it terminal in all cases.
                                    for (UndirectedGraph g : sectionSubs) {
                                        if (g.getVertices().containsKey(artiPoint)) {
                                            g.setTerminal(artiPoint);
                                        }
                                    }
                                    sectionSub.setTerminal(artiPoint); // Should be unnecessary
                                    resultGraphs.add(sectionSub);
                                } // If intersection is 2. Then there are 2 terminals in the subGraph, let solver
                                // do the work.
                            } else {
                                resultGraphs.add(sectionSub);
                            }
                        } else {
                            // Connects to multiple
                            toAdd.add(sectionSub);
                        }
                    }
                    for (UndirectedGraph sectionSub : toAdd) {
                        remaining.addGraph(sectionSub);
                    }

                    // // Use an adaptation of tarjan to go through sections instead of vertices.
                    // // Then check if there is a section which finds its own value again -> Cycle
                    // // Connect this cycle
                    // Map<Integer, Set<UndirectedGraph>> graphPerArtiPoint = new HashMap<>();
                    // for (Vertex arti : artiPoints) {
                    // Set<UndirectedGraph> attached = new HashSet<>();
                    // for (UndirectedGraph nb : sectionSubs) {
                    // if (artiPointsPerGraph.get(nb).contains(arti.getKey()))
                    // attached.add(nb);
                    // }
                    // graphPerArtiPoint.put(arti.getKey(), attached);
                    // }
                    //
                    // Stack<UndirectedGraph> graphStack = new Stack<>();
                    // Map<UndirectedGraph, Integer> graphOrder = new HashMap<>();
                    // Map<UndirectedGraph, Integer> smallestFound = new HashMap<>();
                    // backtracking = false;
                    // UndirectedGraph dummy = new UndirectedGraph();
                    // graphStack.push(dummy);
                    // graphStack.push(sectionSubs.get(0));
                    // graphOrder.put(sectionSubs.get(0), 1);
                    // smallestFound.put(sectionSubs.get(0), 1);
                    // count = 2;
                    // Stack<Integer> artiStack = new Stack<>(); // Use the stack for remembering
                    // through which arti point was connected.
                    // artiStack.push(0);
                    // UndirectedGraph currentSection, parentSection;
                    // stackLoop: while (graphStack.size() > 1) {
                    // currentSection = graphStack.pop();
                    // System.out.println("current:" +
                    // currentSection.getEdges().iterator().next().getVertices()[0].getKey() + " "
                    // + currentSection.getEdges().iterator().next().getVertices()[1].getKey());
                    // parentSection = graphStack.peek();
                    // if (parentSection.getEdges().size() > 0)
                    // System.out.println("parent:" +
                    // parentSection.getEdges().iterator().next().getVertices()[0].getKey() + " "
                    // + parentSection.getEdges().iterator().next().getVertices()[1].getKey());
                    // else
                    // System.out.println("parent: null");
                    // System.out.println("Connected arti: " + artiStack.peek());
                    // graphStack.push(currentSection);
                    // // backtracking = true;
                    //
                    // for (Integer currentArti : artiPointsPerGraph.get(currentSection)) {
                    // for (UndirectedGraph nb : graphPerArtiPoint.get(currentArti)) {
                    // if (!graphOrder.containsKey(nb) && currentArti != artiStack.peek()) {
                    // graphStack.push(nb);
                    // artiStack.push(currentArti);
                    // graphOrder.put(nb, count);
                    // smallestFound.put(nb, count);
                    // System.out.println("Pushing " +
                    // nb.getEdges().iterator().next().getVertices()[0].getKey() + " "
                    // + nb.getEdges().iterator().next().getVertices()[1].getKey() + " with arti " +
                    // currentArti + " and count "
                    // + count);
                    // count++;
                    // continue stackLoop;
                    // }
                    // }
                    // }
                    // // Got here. All connected graphs have already been visited.
                    // System.out.println("CurrentSection: " +
                    // currentSection.getEdges().iterator().next().getVertices()[0].getKey() + " "
                    // + currentSection.getEdges().iterator().next().getVertices()[1].getKey());
                    // for (Integer currentArti : artiPointsPerGraph.get(currentSection)) {
                    // if (currentArti == artiStack.peek())
                    // continue;
                    // System.out.println("Arti: " + currentArti);
                    // for (UndirectedGraph nb : graphPerArtiPoint.get(currentArti)) {
                    // System.out.println("neighbour " +
                    // nb.getEdges().iterator().next().getVertices()[0].getKey() + " "
                    // + nb.getEdges().iterator().next().getVertices()[1].getKey());
                    // System.out.println("Checks: " + (nb == parentSection) + " " +
                    // (artiStack.peek() == currentArti) + " "
                    // + (artiPointsPerGraph.get(currentSection).size() == 1) + " " +
                    // (artiPointsPerGraph.get(nb).size() == 1));
                    // if (nb == parentSection || artiStack.peek() == currentArti ||
                    // artiPointsPerGraph.get(currentSection).size() == 1
                    // || artiPointsPerGraph.get(nb).size() == 1) // This check needs to change
                    // continue;
                    // if (smallestFound.get(currentSection) > smallestFound.get(nb)) {
                    // smallestFound.put(currentSection, smallestFound.get(nb));
                    // System.out.println("Found new smallest: " +
                    // smallestFound.get(currentSection));
                    // }
                    // }
                    // }
                    // System.out.println("pop");
                    // artiStack.pop();
                    // graphStack.pop();
                    // }
                    // System.out.println("Sectioning values:");
                    // for (UndirectedGraph g : smallestFound.keySet())
                    // System.out.println(g.getEdges().iterator().next().getVertices()[0].getKey() +
                    // " "
                    // + g.getEdges().iterator().next().getVertices()[1].getKey() + " " +
                    // smallestFound.get(g));
                    //
                    // resultGraphs.addAll(sectionSubs); // Done with this section
                }
            }
            // System.out.println("Done with section");
        }
        // for (UndirectedGraph sub : resultGraphs) {
        // System.out.println("Section:");
        // for (Edge e : sub.getEdges())
        // System.out.println(e.getVertices()[0].getKey() + " " +
        // e.getVertices()[1].getKey() + " " + e.getCost().get());
        // for (Vertex t : sub.getTerminals().values())
        // System.out.println("Terminal: " + t.getKey());
        // }
        // OLD
        // OLD
        // OLD
        // OLD
        // OLD
        // OLD
        /*
		 * while (stack.size() > 1) { // If the stack is 1, we are back to the fake
		 * vertex -> stop current = stack.pop(); parent = stack.peek();
		 * stack.push(current); // We need to use this trick to get the parent it =
		 * current.getNeighbors().iterator(); backtracking = true; for (Vertex neighbor
		 * : current.getNeighbors()) { if (iteratedValues[neighbor.getKey() - 1] == 0) {
		 * // If any neighbor is unexplored, don't backtrack. backtracking =
		 * backtracking && false; } } if (!backtracking) { // We still have unexplored
		 * neighbors while ((next = it.next()) != null) { if
		 * (iteratedValues[next.getKey() - 1] == 0) { // Find the unexplored neighbor
		 * iteratedValues[next.getKey() - 1] = count; lowestFoundLabels[next.getKey() -
		 * 1] = count; count++; stack.push(next); break; } if (!it.hasNext()) { //
		 * Should never get it here, would mean there is something wrong with unexplored
		 * // neighbors check System.out.println("Still got in here"); break; } } } else
		 * { // All neighbors explored while ((next = it.next()) != null) { if (next !=
		 * parent) { lowestFoundLabels[current.getKey() - 1] =
		 * Math.min(lowestFoundLabels[current.getKey() - 1],
		 * lowestFoundLabels[next.getKey() - 1]); // Set current lowest to go to lowest
		 * neighbor } if (!it.hasNext()) { if (lowestFoundLabels[current.getKey() - 1]
		 * == iteratedValues[current.getKey() - 1] && parent != fake) { // Found a
		 * bridge bridges.add(current.getConnectingEdge(parent));
		 * bridgeNodes.add(current); bridgeNodes.add(parent); } else if (parent != fake
		 * && lowestFoundLabels[current.getKey() - 1] >= iteratedValues[parent.getKey()
		 * - 1] && !bridgeNodes.contains(parent)) { // Found an articulation point
		 * artiPoints.add(parent); } stack.pop(); break; } } } } if
		 * (v0.getNeighbors().size() > 1) { // Special case: Unless first node is an
		 * actual articulation point, remove from // articulation points. This was due
		 * to some implementation strategy that v0 was // almost always chosen as
		 * articulation int val = iteratedValues[v0.getKey() - 1]; boolean remove =
		 * true; for (Vertex v : v0.getNeighbors()) { if (lowestFoundLabels[v.getKey() -
		 * 1] != val) { remove = false; break; } } if (remove) { artiPoints.remove(v0);
		 * } } artiPoints.removeAll(bridgeNodes); // Remove all bridgeNodes from the
		 * articulation points, and then add the first // bridgenode again to
		 * articulation points to ensure only a single node from // bridge is
		 * articulation
		 * 
		 * for (Vertex v : artiPoints) { System.out.println("Arti: " + v.getKey()); } //
		 * System.out.println("Artipoints:"); // for (Vertex v : artiPoints) { //
		 * System.out.println(v.getKey()); // } // System.out.println("Bridges:"); //
		 * for (Edge e : bridges) { // System.out.println(e.getVertices()[0].getKey() +
		 * " " + // e.getVertices()[1].getKey() + " " + e.getCost().get()); // }
		 * 
		 * // FROM HERE, ARTICULATION POINTS HAVE BEEN FOUND // START WITH CREATING THE
		 * SECTIONS // ArrayList<UndirectedGraph> subGraphs = new ArrayList<>();
		 * 
		 * // Map to keep track of all the already visited vertices in the set. // These
		 * will hold only the vertices which are not articulation points. // Map<Vertex,
		 * Boolean> hasVisited;
		 * 
		 * // The stack used for replacement of recursion stack = new Stack<>();
		 * 
		 * // Temporary vertex used for moving other vertices around // Vertex tmp;
		 * 
		 * // Map in a Map which shows for every articulation point which neighbours
		 * have // been checked (true=checked) Map<Vertex, Map<Vertex, Boolean>>
		 * artiNbCheck = new HashMap<>(); for (Vertex v : artiPoints) { Map<Vertex,
		 * Boolean> map = new HashMap<>(); for (Vertex nb : v.getNeighbors()) {
		 * map.put(nb, false); } artiNbCheck.put(v, map); } Map<UndirectedGraph,
		 * Set<Vertex>> artiPerGraph = new HashMap<>();
		 * 
		 * // VerticesInSection, TerminalsInSection, ArticulationInSection and //
		 * EdgesInSection // If a terminals is an articulation as well, it will not be
		 * added to tis. // Set<Vertex> vis, tis; // Set<Edge> eis; Set<Vertex> ais;
		 * 
		 * for (Vertex arti : artiPoints) { stack.push(arti); // Create a new stack with
		 * this articulation point as the root parent = arti; // The current will always
		 * be a nb, the starting articulation point will be the // parent for the first
		 * nb checkArti: while (artiNbCheck.get(arti).values().contains(false)) { // The
		 * articulation point has searched all its corresponding sections // once every
		 * edge has been used hasVisitedNode = new HashSet<>(); vis = new HashSet<>();
		 * tis = new HashSet<>(); ais = new HashSet<>(); eis = new HashSet<>();
		 * ais.add(arti); nbCheck: for (Vertex nb : arti.getNeighbors()) { if
		 * (artiNbCheck.get(arti).containsKey(nb) && !artiNbCheck.get(arti).get(nb)) {
		 * // Only check if nb is not used yet stack.push(nb);
		 * artiNbCheck.get(arti).put(nb, true); if (artiPoints.contains(nb)) {
		 * ais.add(nb); artiNbCheck.get(nb).put(arti, true); } else if (nb.isTerminal())
		 * { tis.add(nb); } else { vis.add(nb); } hasVisitedNode.add(nb);
		 * eis.add(arti.getConnectingEdge(nb)); break nbCheck; } } if (stack.size() > 2)
		 * { System.out.println("Something went wrong in the neighbour checking");
		 * System.exit(1); } else if (stack.size() == 1) { // No nb found which hasn't
		 * been traversed, this articulation point is done break checkArti; } else if
		 * (stack.size() == 2 && artiPoints.contains(stack.peek())) { stack.pop();//
		 * Single edge section, add to the subgraphs directly and afterwards try to //
		 * combine } else { // Stack now contains arti and 1 neighbour while
		 * (stack.size() > 1) { // There is still at least one node except for the
		 * articulation point we need to // traverse it =
		 * stack.peek().getNeighbors().iterator(); if (!it.hasNext()) { // Current item
		 * on the stack has no neighbours
		 * System.out.println("The current stack item does not have any neighbours");
		 * System.exit(1); } nbLoop: while (it.hasNext()) { next = it.next(); if (parent
		 * == next) { // Found parent again, do nothing //
		 * System.out.println("Parent found"); } else if (artiPoints.contains(next) &&
		 * !artiNbCheck.get(next).get(stack.peek())) { // My next vertex is an arti
		 * point artiNbCheck.get(next).put(stack.peek(), true); ais.add(next);
		 * eis.add(next.getConnectingEdge(stack.peek())); } else if
		 * (!eis.contains(stack.peek().getConnectingEdge(next))) { // Current edge not
		 * in set yet eis.add(stack.peek().getConnectingEdge(next)); if
		 * (!hasVisitedNode.contains(next)) { // Neighbour is unvisited
		 * hasVisitedNode.add(next); if (next.isTerminal()) { tis.add(next); } else {
		 * vis.add(next); } parent = stack.peek(); stack.push(next); break nbLoop; } }
		 * if (!it.hasNext()) { stack.pop(); // Iterator went through all neighbours,
		 * thus backtrack. tmp = stack.pop(); parent = (stack.size() == 0 ? arti :
		 * stack.peek()); stack.push(tmp); break nbLoop; } } } }
		 * 
		 * UndirectedGraph subGraph = new UndirectedGraph(); for (Edge e : eis) {
		 * subGraph.addEdge(e.getVertices()[0].getKey(), e.getVertices()[1].getKey(),
		 * e.getCost().get()); if (e.getStack() != null)
		 * subGraph.getVertices().get(e.getVertices()[0].getKey())
		 * .getConnectingEdge(subGraph.getVertices().get(e.getVertices()[1].getKey())).
		 * pushStack(e.getStack()); } for (Vertex t : tis) {
		 * subGraph.setTerminal(t.getKey()); } for (Vertex a : ais) {
		 * subGraph.setTerminal(a.getKey()); } for (Vertex v : vis) { if
		 * (v.getSubsumed() != null)
		 * subGraph.getVertices().get(v.getKey()).pushStack(v.getSubsumed()); }
		 * subGraphs.add(subGraph); artiPerGraph.put(subGraph, ais); }
		 * 
		 * stack.removeAllElements(); }
		 * 
		 * // for (UndirectedGraph sub : subGraphs) { // System.out.println("Section:");
		 * // for (Edge e : sub.getEdges()) //
		 * System.out.println(e.getVertices()[0].getKey() + " " + //
		 * e.getVertices()[1].getKey() + " " + e.getCost().get()); // }
		 * 
		 * // Here we still need to check how we can combine multiple sections //
		 * Collection<Integer> intersection, toBeIntersected; // UndirectedGraph sub0;
		 * // UndirectedGraph sub1; // ArrayList<Integer> toBeRemoved = new
		 * ArrayList<>(); // ArrayList<Integer> recheck = new ArrayList<>(); //
		 * HashSet<Integer[]> toCheck = new HashSet<>(); // for (int i = 0; i <
		 * subGraphs.size(); i++) { // for (int j = i + 1; j < subGraphs.size(); j++) {
		 * // toCheck.add(new Integer[] { i, j }); // // System.out.println("ToCheck: "
		 * + i + " " + j); // } // } // // while (true) { // Iterator<Integer[]> checks
		 * = toCheck.iterator(); // while (checks.hasNext()) { // Integer[] cur =
		 * checks.next(); // if (toBeRemoved.contains(cur[0]) ||
		 * toBeRemoved.contains(cur[1])) // continue; // // System.out.println("Size: "
		 * + subGraphs.size()); // // System.out.println("Indices: " + cur[0] + " " +
		 * cur[1]); // sub0 = subGraphs.get(cur[0]); // sub1 = subGraphs.get(cur[1]); //
		 * Set<Vertex> artis0 = artiPerGraph.get(sub0); // Set<Vertex> artis1 =
		 * artiPerGraph.get(sub1); // intersection = new ArrayList<>(); // for (Vertex v
		 * : artis0) // intersection.add(v.getKey()); // toBeIntersected = new
		 * ArrayList<>(); // for (Vertex v : artis1) // toBeIntersected.add(v.getKey());
		 * // intersection.retainAll(toBeIntersected); // if (intersection.size() >= 2)
		 * { // More than 2 articulation points in common // -> Merge them // //
		 * System.out.println("Merging"); // sub0.addGraph(sub1); //
		 * toBeRemoved.add(cur[1]); // recheck.add(cur[0]); // } // // Add more cases
		 * here to connect // // .... // } // toCheck.clear(); // //
		 * System.out.println("Removing: " + Arrays.toString(toBeRemoved.toArray())); //
		 * // System.out.println("Must Recheck: " + Arrays.toString(recheck.toArray()));
		 * // if (recheck.size() == 0) // break; // else { // toBeRemoved.sort((Integer
		 * i, Integer j) -> { // if (i > j) // return 1; // if (j > i) // return -1; //
		 * return 0; // }); // for (int i = toBeRemoved.size() - 1; i >= 0; i--) { //
		 * subGraphs.remove(toBeRemoved.get(i).intValue()); // } // //
		 * System.out.println("New subGraph size: " + subGraphs.size()); // for (int i =
		 * 0; i < recheck.size(); i++) { // for (int j = 0; j < subGraphs.size(); j++) {
		 * // if (recheck.get(i) == j) // continue; // if (!recheck.contains(j) ||
		 * recheck.indexOf(j) > i) { // // System.out.println("Adding to reCheck:" +
		 * recheck.get(i) + " " + j); // if (recheck.get(i) < j) { // Ensure that the
		 * smallest of the two is always // first for ordering. // toCheck.add(new
		 * Integer[] { recheck.get(i), j }); // } else { // toCheck.add(new Integer[] {
		 * j, recheck.get(i) }); // } // // } // } // } // // for (Integer[] i :
		 * toCheck) // // System.out.println("Recheck " + i[0] + " " + i[1]); //
		 * recheck.clear(); // toBeRemoved.clear(); // } // } // If there is no
		 * articulation point -> return a deepcopy of the original if (artiPoints.size()
		 * == 0) { subGraphs.add(this.graph.clone()); }
         */
        return subGraphs;

        //
        // for (Vertex arti : artiPoints) {
        // stack.push(arti);
        // parent = arti;
        // checkArti: while (artiNbCheck.get(arti).values().contains(false)) {
        // hasVisited = new HashMap<>();
        // vis = new HashSet<>();
        // tis = new HashSet<>();
        // ais = new HashSet<>();
        // eis = new HashSet<>();
        // ais.add(arti);
        //
        // nbCheck: for (Vertex nb : arti.getNeighbors()) {
        // if (artiNbCheck.get(arti).containsKey(nb) && !artiNbCheck.get(arti).get(nb))
        // {
        //
        // // The neighbour of the articulationPoint is an articulation as well.
        // // Do nothing
        // if (artiPoints.contains(nb)) {
        // artiNbCheck.get(arti).put(nb, true);
        // artiNbCheck.get(nb).put(arti, true);
        //
        // Collection<Integer> intersection;
        // boolean added = false;
        // for (UndirectedGraph prevGraph : subGraphs) {
        // intersection = prevGraph.getTerminals().keySet();
        // HashSet<Integer> arts = new HashSet<>();
        // // System.out.println(Arrays.toString(intersection.toArray()));
        // arts.add(arti.getKey());
        // arts.add(nb.getKey());
        // // System.out.println(Arrays.toString(arts.toArray()));
        // intersection.retainAll(arts);
        // // System.out.println("Checking section:");
        // // System.out.println(prevGraph.getEdges().size());
        // // System.out.println("Intersection size: " + intersection.size());
        // if (intersection.size() >= 2) {
        // added = true;
        // prevGraph.addEdge(arti.getKey(), nb.getKey(),
        // arti.getConnectingEdge(nb).getCost().get());
        // HashSet<Vertex> temp = new HashSet<>();
        // temp.add(nb);
        // temp.add(arti);
        // temp.addAll(artiPerGraph.get(prevGraph));
        // artiPerGraph.replace(prevGraph, temp);
        // break;
        // }
        // }
        // if (!added) {
        // UndirectedGraph sub = new UndirectedGraph();
        // sub.addEdge(arti.getKey(), nb.getKey(),
        // arti.getConnectingEdge(nb).getCost().get());
        // subGraphs.add(sub);
        // HashSet<Vertex> temp = new HashSet<>();
        // temp.add(nb);
        // temp.add(arti);
        // artiPerGraph.put(sub, temp);
        // }
        //
        // } else { // Found new unvisited neighbour
        // stack.push(nb);
        // artiNbCheck.get(arti).put(nb, true);
        // if (nb.isTerminal()) {
        // tis.add(nb);
        // } else {
        // vis.add(nb);
        // }
        // hasVisited.put(nb, true);
        // eis.add(arti.getConnectingEdge(nb));
        // break nbCheck;
        // }
        // }
        // }
        // if (stack.size() > 2) {
        // System.out.println("Something went wrong in the neighbour checking");
        // System.exit(1);
        // } else if (stack.size() == 1) {
        // break checkArti;
        // } else { // Stack now contains arti and 1 neighbour
        // while (stack.size() > 1) { // If size is 1, then only arti available
        // it = stack.peek().getNeighbors().iterator();
        // if (!it.hasNext()) { // Current item on the stack has no neighbours
        // System.out.println("The current stack item does not have any neighbours");
        // System.exit(1);
        // }
        // nbLoop: while (it.hasNext()) {
        // next = it.next();
        // if (parent == next) { // Found parent again, do nothing
        // // System.out.println("Parent found");
        // } else if (artiPoints.contains(next) &&
        // !artiNbCheck.get(next).get(stack.peek())) { // My next vertex is an arti
        // point
        // artiNbCheck.get(next).put(stack.peek(), true);
        // ais.add(next);
        // eis.add(next.getConnectingEdge(stack.peek()));
        // } else if (!eis.contains(stack.peek().getConnectingEdge(next))) { // Current
        // edge not in set yet
        // eis.add(stack.peek().getConnectingEdge(next));
        // if (!hasVisited.containsKey(next) || !hasVisited.get(next)) { // Neighbour is
        // unvisited
        // hasVisited.put(next, true);
        // if (next.isTerminal()) {
        // tis.add(next);
        // } else {
        // vis.add(next);
        // }
        // parent = stack.peek();
        // stack.push(next);
        // break nbLoop;
        // }
        // }
        // if (!it.hasNext()) {
        // stack.pop(); // Iterator went through all neighbours, thus backtrack.
        // tmp = stack.pop();
        // parent = (stack.size() == 0 ? arti : stack.peek());
        // stack.push(tmp);
        // break nbLoop;
        // }
        // }
        // }
        // // if (ais.size() == 2 && tis.size() == 0) {
        // // System.out.println("THIS HAPPENED");
        // // }
        // Collection<Integer> intersection;
        // boolean added = false;
        // for (UndirectedGraph prevGraph : subGraphs) {
        // intersection = new ArrayList<>(prevGraph.getTerminals().keySet());
        // HashSet<Integer> arts = new HashSet<>();
        // // System.out.println(Arrays.toString(intersection.toArray()));
        // for (Vertex v : ais)
        // arts.add(v.getKey());
        // // System.out.println(Arrays.toString(arts.toArray()));
        // intersection.retainAll(arts);
        // // System.out.println("Checking section:");
        // // System.out.println(prevGraph.getEdges().size());
        // // System.out.println("Intersection size: " + intersection.size());
        // if (intersection.size() >= 2) {
        // added = true;
        // for (Edge e : eis) {
        // prevGraph.addEdge(e.getVertices()[0].getKey(), e.getVertices()[1].getKey(),
        // e.getCost().get());
        // }
        // for (Vertex t : tis) {
        // prevGraph.setTerminal(t.getKey());
        // }
        // for (Vertex a : ais) {
        // prevGraph.setTerminal(a.getKey());
        // }
        // HashSet<Vertex> temp = new HashSet<>();
        // temp.addAll(ais);
        // temp.addAll(artiPerGraph.get(prevGraph));
        // artiPerGraph.replace(prevGraph, temp);
        // break;
        // }
        // }
        // if (!added) {
        // // We are back to articulation point, thus end of section
        // UndirectedGraph subGraph = new UndirectedGraph();
        // for (Edge e : eis) {
        // subGraph.addEdge(e.getVertices()[0].getKey(), e.getVertices()[1].getKey(),
        // e.getCost().get());
        // }
        // for (Vertex t : tis) {
        // subGraph.setTerminal(t.getKey());
        // }
        // for (Vertex a : ais) {
        // subGraph.setTerminal(a.getKey());
        // }
        // subGraphs.add(subGraph);
        // artiPerGraph.put(subGraph, ais);
        // }
        // }
        //
        // }
        // stack.removeAllElements();
        // }
        // //
        // // for (UndirectedGraph sub1 : subGraphs) {
        // // for (UndirectedGraph sub2 : subGraphs) {
        // //
        // // }
        // // }
        //
        // return subGraphs;
    }

    /**
     * Reduces the size of a section after using an analysis on what can be
     * removed and what has to be connected. The assumption is made that, when
     * this method is called, it will always be to reduce the size of the graph.
     *
     * @param vertices The set of vertices that can be removed.
     * @param terminals The set of terminals in the section.
     * @param bridgeEndpoints The set of bridge endpoints in the section.
     * @param edges The set of edges that can be removed.
     *
     * @author Joshua Scheidt
     */
    public void reduceSection(ArrayList<Vertex> vertices, ArrayList<Vertex> terminals, ArrayList<Vertex> articulations, ArrayList<Edge> edges) {
        ArrayList<Edge> toBeAddedEdges = new ArrayList<>();
        ArrayList<Vertex> ends;
        for (int i = 0; i < articulations.size(); i++) {
            ends = new ArrayList<>();
            for (int j = i + 1; j < articulations.size(); j++) {
                ends.add(articulations.get(j));
            }
            ArrayList<Edge> tmp = (ends.size() > 0 ? PathFinding.DijkstraMultiPath(this.graph, articulations.get(i), ends, edges)
                    : new ArrayList<>());
            toBeAddedEdges.addAll(tmp);
        }
        // Create new edges between terminals
        for (int i = 0; i < terminals.size(); i++) {
            ends = new ArrayList<>();
            for (int j = i + 1; j < terminals.size(); j++) {
                ends.add(terminals.get(j));
            }
            ArrayList<Edge> tmp = (ends.size() > 0 ? PathFinding.DijkstraMultiPath(this.graph, terminals.get(i), ends, edges) : new ArrayList<>());
            toBeAddedEdges.addAll(tmp);
        }

        // Create new edges between the terminals and bridges
        for (int i = 0; i < articulations.size(); i++) {
            ends = new ArrayList<>();
            for (int j = 0; j < terminals.size(); j++) {
                if (articulations.get(i) == terminals.get(j)) {
                    continue;
                }
                ends.add(terminals.get(j));
            }
            ArrayList<Edge> tmp = (ends.size() > 0 ? PathFinding.DijkstraMultiPath(this.graph, articulations.get(i), ends, edges)
                    : new ArrayList<>());
            toBeAddedEdges.addAll(tmp);
        }

        // Remove all vertices and edges which are now not needed anymore
        for (Edge e : edges) {
            if (!e.getVertices()[0].isTerminal() && !e.getVertices()[1].isTerminal() && !articulations.contains(e.getVertices()[0])
                    && !articulations.contains(e.getVertices()[1])) {
                this.graph.removeEdge(e);
            }
        }

        for (Vertex v : vertices) {
            this.graph.removeVertex(v);
        }

        for (Edge e : toBeAddedEdges) {
            this.graph.addEdge(e);
        }

        // if (toBeAddedEdges.size() == 0 && bridgeEndpoints.size() == 1) {
        // System.out.println("removing");
        // this.graph.removeVertex(bridgeEndpoints.get(0).getKey());
        // }
    }

    /**
     * Removes possible sections without terminals and/or sections containing
     * only single terminals.
     *
     * @return All the bridges in the graph.
     *
     * @author Joshua Scheidt
     */
    public void removeBridgesAndSections(int totalVertices) {
        long start = System.currentTimeMillis();
        // ArrayList<Edge> bridges =
        // this.TarjansBridgeFinding(this.graph.getVertices().get(this.graph.getVertices().keySet().toArray()[0]),
        // totalVertices);
        HashSet<Vertex> sectionPoints = this.articulationPointFinding(this.graph.getVertices().get(this.graph.getVertices().keySet().toArray()[0]),
                totalVertices);
        long middle = System.currentTimeMillis();
        this.analyseSections(sectionPoints);
        // this.analyseSections(bridges, totalVertices);
        // this.analyseSectionsPoints(sectionPoints, totalVertices);
        System.out.println("Step 1:" + (middle - start) + " ms");
        System.out.println("Step 2:" + (System.currentTimeMillis() - middle) + " ms");
    }

    /**
     * Below method was made to 'hide' terminals which had 2 neighbours which
     * were also terminals However currently this doesn't happen in any graph
     */
    public void removeTerminals() {
        HashMap<Integer, Vertex> vertices = this.graph.getVertices();
        Set keys = vertices.keySet();
        Iterator it = keys.iterator();
        Vertex current;
        while (it.hasNext()) {
            current = vertices.get((int) it.next());
            int counter = 0;
            Iterator neighbours = current.getNeighbors().iterator();
            while (neighbours.hasNext()) {
                if (((Vertex) neighbours.next()).isTerminal() && current.isTerminal()) {
                    counter++;
                }
            }
            // if (counter > 0) {
            // System.out.println("This Terminal has " + counter + " Terminal neighbours");
            // }
            if (current.getNeighbors().size() == 2 && ((Vertex) (current.getNeighbors().toArray()[0])).isTerminal()
                    && ((Vertex) (current.getNeighbors().toArray()[1])).isTerminal()) {
                System.out.println("This actually happens?");
            }
        }

    }
}
