/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mixedIntegerProgramming;

import graph.Edge;
import graph.UndirectedGraph;
import graph.Vertex;
import ilog.concert.IloException;
import ilog.concert.IloLPMatrix;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.util.Pair;
import mainAlgorithms.SteinerTreeSolver;

/**
 *
 * @author Marciano
 */
public class FlowILP implements SteinerTreeSolver {

    private UndirectedGraph g;
    private HashMap<Integer, Pair<Vertex, Edge>> edgeKeys;
    private HashMap<Vertex, HashSet<Pair<Integer, Edge>>> vertexNeighbours;
    //Holds the keys for the fake variables which simulate flow
    private int source;
    private HashSet<Integer> sinks;
    private String fileName;
    private Path LPFile;
    private final boolean DEBUG = true;

    public FlowILP(UndirectedGraph g, String fileName) {
        this.g = g;
        this.sinks = new HashSet<>();
        this.fileName = fileName;
        this.LPFile = Paths.get("data\\ILP\\" + this.fileName + ".lp");
    }

    @Override
    public List<Edge> solve(UndirectedGraph g) {
        double[] result;
        ArrayList<Edge> resultingEdges;
        this.createIdentifiableEdges();
        System.out.println("Done Creating Identifiable Edges");
        this.createSourceAndSinks();
        System.out.println("Created Source and Sink Edges");

        if (this.LPFile.toFile().exists()) {
            result = this.activateCPLEX(this.LPFile.toString());
            resultingEdges = new ArrayList<>();
        } else {
            this.writeStartOfFile();
            System.out.println("Written Start of File");
            this.writeSourceAndSinksConstraints();
            System.out.println("Written Source and Sink constraints");
            this.writeBinaryEdgeConstraints();
            System.out.println("Written Binary Edge constraints");
            this.writeFlowConservationConstraints();
            System.out.println("Written Flow Conservation constraints");
            this.writeEndOfFile();
            System.out.println("Written end of File");
            result = this.activateCPLEX(this.LPFile.toString());
            resultingEdges = new ArrayList<>();
        }
        for (int i = 0; i < this.edgeKeys.size() - this.g.getNumberOfTerminals(); i++) {
            if (result[i] >= 0.5) {
                resultingEdges.add(this.edgeKeys.get(i + 1).getValue());
            }
        }
        return resultingEdges;
    }

    public void writeBinaryEdgeConstraints() {
        ArrayList<String> lines = new ArrayList<>();
        String temp = "";
        Iterator it = this.edgeKeys.keySet().iterator();
        int current;
        while (it.hasNext()) {
            current = (int) it.next();
            if (this.edgeKeys.get(current).getValue() != null) {
                temp = "x_" + current + " - " + this.g.getNumberOfTerminals() + "y_" + current + " <= 0";
                lines.add(temp);
            }
        }
        try {
            Files.write(this.LPFile, lines, Charset.forName("UTF-8"), StandardOpenOption.APPEND);
        } catch (IOException ex) {
            Logger.getLogger(CutILP.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void writeFlowConservationConstraints() {
        ArrayList<String> lines = new ArrayList<>();
        String temp = "";
        boolean first = true;
        Pair<Vertex, Edge> current;
        for (Vertex v : this.g.getVertices().values()) {
            for (Pair<Integer, Edge> p : this.vertexNeighbours.get(v)) {
                if (first && (p.getKey() == this.source || this.sinks.contains(p.getKey()))) {
                    if (this.sinks.contains(p.getKey())) {
                        temp = temp.concat("-x_" + p.getKey());
                    } else {
                        temp = temp.concat("x_" + p.getKey());
                    }
                    first = false;
                } else if (this.sinks.contains(p.getKey())) {
                    temp = temp.concat(" - x_" + p.getKey());
                } else if (first) {
                    temp = temp.concat("x_" + p.getKey());
                    first = false;
                } else {
                    temp = temp.concat(" +  x_" + p.getKey());
                }
                for (Vertex n : v.getNeighbors()) {
                    if (n.getEdges().contains(p.getValue())) {
                        for (Pair<Integer, Edge> pn : this.vertexNeighbours.get(n)) {
                            if (pn.getValue() == null) {
                                //Your neighbour has a sink or source connected to it
                            } else if (p.getValue() == null) {
                                //This Edge is a fake source/sink edge this is already
                                //taken care of
                            } else if (p.getValue().equals(pn.getValue())) {
                                temp = temp.concat(" - x_" + pn.getKey());
                            }
                        }
                    }
                }
            }
            first = true;
            temp = temp.concat(" = 0");
            lines.add(temp);
            temp = "";
        }
        try {
            Files.write(this.LPFile, lines, Charset.forName("UTF-8"), StandardOpenOption.APPEND);
        } catch (IOException ex) {
            Logger.getLogger(CutILP.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void writeSourceAndSinksConstraints() {
        ArrayList<String> lines = new ArrayList<>();
        lines.add("x_" + this.source + " = " + (this.g.getNumberOfTerminals() - 1));
        for (Integer i : this.sinks) {
            lines.add("x_" + i + " = 1");
        }
        try {
            Files.write(this.LPFile, lines, Charset.forName("UTF-8"), StandardOpenOption.APPEND);
        } catch (IOException ex) {
            Logger.getLogger(CutILP.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void createSourceAndSinks() {
        Iterator it = this.g.getTerminals().keySet().iterator();
        int key = (int) it.next(), sinkCounter = 0;
        int counter = this.edgeKeys.size() + 1;

        Pair<Vertex, Edge> temp = new Pair(this.g.getTerminals().get(key), null);
        this.edgeKeys.put(counter, temp);

        Pair<Integer, Edge> edgePair = new Pair(counter, null);
        HashSet<Pair<Integer, Edge>> pairs = this.vertexNeighbours.get(this.g.getTerminals().get(key));
        this.vertexNeighbours.get(this.g.getTerminals().get(key)).add(edgePair);

        this.source = counter;
        counter++;

        while (it.hasNext()) {
            key = (int) it.next();
            temp = new Pair(this.g.getTerminals().get(key), null);
            this.edgeKeys.put(counter, temp);
            this.vertexNeighbours.get(this.g.getVertices().get(key)).add(new Pair(counter, null));
            this.sinks.add(counter);
            counter++;
            sinkCounter++;
        }
    }

    public void createIdentifiableEdges() {
        this.edgeKeys = new HashMap<>();
        this.vertexNeighbours = new HashMap<>();
        Pair<Vertex, Edge> temp;
        HashSet<Pair<Integer, Edge>> neighbours;
        int counter = 1;
        for (Vertex v : this.g.getVertices().values()) {
            neighbours = new HashSet<>();
            for (Edge e : v.getEdges()) {
                temp = new Pair(v, e);
                neighbours.add(new Pair(counter, temp.getValue()));
                this.edgeKeys.put(counter, temp);
                counter++;
            }
            this.vertexNeighbours.put(v, neighbours);
        }
    }

    public void writeStartOfFile() {
        ArrayList<String> lines = new ArrayList<>();

        lines.add("Minimize");
        String temp = "obj: ";
        Iterator it = this.edgeKeys.keySet().iterator();
        int current = (int) it.next();
        Pair<Vertex, Edge> VEPair = this.edgeKeys.get(current);

        temp = temp.concat(VEPair.getValue().getCost().get() + "y_" + current);
        while (it.hasNext()) {
            current = (int) it.next();
            VEPair = this.edgeKeys.get(current);
            if (this.edgeKeys.get(current).getValue() != null) {
                temp = temp.concat(" + " + VEPair.getValue().getCost().get() + "y_" + current);
            }
        }
        lines.add(temp);
        temp = "Subject To";
        lines.add(temp);
        try {
            Files.write(this.LPFile, lines, Charset.forName("UTF-8"));
        } catch (IOException ex) {
            Logger.getLogger(CutILP.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void writeEndOfFile() {
        ArrayList<String> lines = new ArrayList<>();
        lines.add("Bounds");
        Iterator it = this.edgeKeys.keySet().iterator();
        while (it.hasNext()) {
            lines.add("x_" + (int) it.next() + " >= 0");
        }
        lines.add("General");
        lines.add("Binaries");
        it = this.edgeKeys.keySet().iterator();
        int current;
        while (it.hasNext()) {
            current = (int) it.next();
            if (current < (this.edgeKeys.size() - this.g.getNumberOfTerminals() + 1)) {
                lines.add("y_" + current);
            }
        }
        lines.add("End");

        try {
            Files.write(this.LPFile, lines, Charset.forName("UTF-8"), StandardOpenOption.APPEND);
        } catch (IOException ex) {
            Logger.getLogger(CutILP.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public double[] activateCPLEX(String txt) {
        double[] vals = null;
        try {
            IloCplex cplex = new IloCplex();

            cplex.importModel(txt);
            cplex.setOut(null);
            if (cplex.solve()) {
                if (DEBUG) {
                    cplex.output().println("Solution status = " + cplex.getStatus());
                    cplex.output().println("Solution value  = " + cplex.getObjValue());
                }
                IloLPMatrix lp = (IloLPMatrix) cplex.LPMatrixIterator().next();

                IloNumVar[] vars = lp.getNumVars();
                vals = cplex.getValues(vars);
                int lengthBasic = this.g.getEdges().size();
                if (DEBUG) {
                    System.out.println("Number of Basic variables: " + lengthBasic);
                    System.out.println(Arrays.toString(vars));
                    System.out.println(Arrays.toString(vals));
                }
//                basic = new int[lengthBasic];
//                if(DEBUG){
//                    System.out.println(Arrays.toString(vars));
//                    System.out.println(Arrays.toString(vals));
//                }
//                int cnt = 0;
//                for(int i = 0; i < vars.length; i++){
//                    if(vals[i] == 1 && i < g.getEdges().size()){
//                       basic[cnt] = i;
//                       cnt++;
//                    }
//                }
            }
            cplex.end();
        } catch (IloException e) {
            System.err.println("Concert exception '" + e + "'caught");
        }
        if (DEBUG) {
            System.out.println(Arrays.toString(vals));
        }
        return vals;
    }

    public void adjacencyMatrix() {
        String temp = "";
        Vertex v;
        temp = temp.concat("[0, ");
        for (int i = 0; i < this.g.getVertices().size(); i++) {
            temp = temp = temp.concat("0, ");
        }
        temp = temp.substring(0, temp.length() - 2);
        temp = temp.concat("]");
        System.out.println(temp);
        temp = "";
        for (int j = 0; j < this.g.getVertices().size(); j++) {
            v = this.g.getVertices().get(j + 1);
            temp = temp.concat("[0, ");
            for (int i = 0; i < this.g.getVertices().size(); i++) {
                if (v.getNeighbors().contains(this.g.getVertices().get(i + 1))) {
                    temp = temp.concat(v.getConnectingEdge(this.g.getVertices().get(i + 1)).getCost().get() + ", ");
                } else {
                    temp = temp.concat("0, ");
                }
            }
            temp = temp.substring(0, temp.length() - 2);
            temp = temp.concat("]");
            System.out.println(temp);
            temp = "";
        }
    }
}
