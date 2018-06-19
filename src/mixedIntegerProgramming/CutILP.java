/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mixedIntegerProgramming;

import graph.Edge;
import graph.UndirectedGraph;
import graph.Vertex;
import ilog.concert.*;
import ilog.cplex.*;
import java.io.File;
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
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Marciano
 */
public class CutILP {

    private UndirectedGraph g;
    private HashMap<Integer, Edge> edges;
    private HashMap<Edge, Integer> keys;
    private HashSet<HashSet<Integer>> result;
    private String fileName;
    private Path LPFile;
    private final boolean DEBUG = false;

    public CutILP(UndirectedGraph g, String fileName) {
        this.g = g;
        this.fileName = fileName;
        this.LPFile = Paths.get("data\\ILP\\" + this.fileName + ".lp");
    }

    public void initiateCutSearch() {
//        File f = new File(this.LPFile.toString());
//        if(f.exists()){
//            System.out.println("This already exists so we can just run it through ILP");
//            System.out.println(this.LPFile);
//        } else {
        this.createIdentifiableEdges();
        this.writeStartOfFile();
        this.recursiveInit();
        this.writeEndOfFile();

//        }
        double[] result = this.activateCPLEX(this.LPFile.toString());
        this.printSolution(result);

    }

    public void writeIntermediate() {
        ArrayList<String> lines = new ArrayList<>();
        String temp = "";
        Iterator resultingList = this.result.iterator();
        Iterator it, constraintIterator;
        HashSet constraint;
        Edge current;
        while (resultingList.hasNext()) {
            constraint = ((HashSet) resultingList.next());
            constraintIterator = constraint.iterator();
            temp = "";
            while (constraintIterator.hasNext()) {
                Vertex v = this.g.getVertices().get((int) constraintIterator.next());
                it = v.getEdges().iterator();
                while (it.hasNext()) {
                    current = (Edge) it.next();
                    if (constraint.contains(current.getOtherSide(v).getKey())) {

                    } else {
                        temp = temp.concat("x_" + this.keys.get(current) + " + ");
                    }
                }
            }
            if (!temp.isEmpty()) {
                temp = temp.substring(0, temp.length() - 2);
                temp = temp.concat(" >= 1");
                lines.add(temp);
            }
        }
        this.result.clear();

        try {
            Files.write(this.LPFile, lines, Charset.forName("UTF-8"), StandardOpenOption.APPEND);
        } catch (IOException ex) {
            Logger.getLogger(CutILP.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void writeEndOfFile() {
        ArrayList<String> lines = new ArrayList<>();
        lines.add("Bounds");
        lines.add("General");
        lines.add("Binaries");
        Iterator it = this.edges.keySet().iterator();
        while (it.hasNext()) {
            lines.add("x_" + (int) it.next());
        }
        lines.add("End");
        try {
            Files.write(this.LPFile, lines, Charset.forName("UTF-8"), StandardOpenOption.APPEND);
        } catch (IOException ex) {
            Logger.getLogger(CutILP.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void writeStartOfFile() {
        ArrayList<String> lines = new ArrayList<>();

        lines.add("Minimize");
        String temp = "obj: ";
        Iterator it = this.keys.keySet().iterator();
        Edge current = (Edge) it.next();
        int key = this.keys.get(current);

        temp = temp.concat(current.getCost().get() + "x_" + key);
        while (it.hasNext()) {
            current = (Edge) it.next();
            key = this.keys.get(current);
            temp = temp.concat(" + " + current.getCost().get() + "x_" + key);
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

    public void recursiveInit() {
        Set keys = this.g.getTerminals().keySet();
        Set others = this.g.getVertices().keySet();
        HashSet alreadyUsed = new HashSet<>();
        Iterator it = keys.iterator();
        int temp;
        HashSet<Integer> sub1, sub2, newAlreadyUsed;
        this.result = new HashSet<>();
        int counter = 1;
        while (it.hasNext()) {
            temp = (int) it.next();
            sub1 = new HashSet<>();
            sub1.add(temp);
            sub2 = new HashSet<>();
            sub2.addAll(others);
            sub2.remove(temp);
            newAlreadyUsed = new HashSet<>();
            newAlreadyUsed.addAll(alreadyUsed);
            System.out.println("Starts cut finding for Terminal: " + counter);
            this.recursiveCut(sub1, sub2, alreadyUsed, 1);
            alreadyUsed.add(temp);
            counter++;
        }
        if (this.result.size() > 0) {
            System.out.println("Just write pls");
            this.writeIntermediate();
        }
    }

    public void recursiveCut(HashSet<Integer> sub1, HashSet<Integer> sub2, HashSet<Integer> alreadyUsed, int term1) {
        if (term1 == this.g.getNumberOfTerminals()) {
            return;
        }
        this.result.add(sub1);
        if (this.result.size() >= 50) {
            this.writeIntermediate();
        }
        HashSet<Integer> newSub1, newSub2, newAlreadyUsed;
        for (int i : sub1) {
            for (Vertex v : this.g.getVertices().get(i).getNeighbors()) {
                if (!sub1.contains(v.getKey()) && !alreadyUsed.contains(v.getKey())) {
                    newAlreadyUsed = new HashSet<>();
                    newSub1 = new HashSet<>();
                    newSub2 = new HashSet<>();
                    
                    newSub1.addAll(sub1);
                    newSub1.add(v.getKey());
                    
                    newSub2.addAll(sub2);
                    
                    newAlreadyUsed.add(v.getKey());
                    newAlreadyUsed.addAll(alreadyUsed);
                    
                    alreadyUsed.add(v.getKey());
                    if (this.g.getTerminals().containsKey(v.getKey())) {
                        term1++;
                    }
                    this.recursiveCut(newSub1, newSub2, newAlreadyUsed, term1);
                }
            }
        }
    }

    public void createIdentifiableEdges() {
        this.edges = new HashMap<>();
        this.keys = new HashMap<>();
        Iterator it = this.g.getEdges().iterator();
        Edge e;
        int counter = 1;
        while (it.hasNext()) {
            e = (Edge) it.next();
            this.keys.put(e, counter);
            this.edges.put(counter, e);
            counter++;
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

    public void printSolution(double[] result) {
        int sum = 0;
        Edge current = null;
        String temp = "";
        for (int i = 0; i < result.length; i++) {
            if (result[i] == 1) {
                current = this.edges.get(i + 1);
                sum += current.getCost().get();
                temp = temp.concat(current.getVertices()[0].getKey() + " " + current.getVertices()[1].getKey() + "\n");
            }
        }
        System.out.println("VALUE: " + sum);
        System.out.println(temp);
    }
//    public String flowFormulation(){
//        ArrayList<String> lines = new ArrayList<>();
//        lines.add("Minimize");
//        String temp = "obj: ";
//        String variables = "";
//        Iterator<Edge> it = this.g.getEdges().iterator();
//        Edge e;
//        int cnt = 0;
//        while(it.hasNext()){
//            e = (Edge) it.next();
//            if(cnt == 0){
//                temp = temp.concat(e.getCost() + "x_" + ( (e.getVertices()[0])).getKey() + "" + ((e.getVertices()[1])).getKey());
//                variables = variables.concat( "x_" + ((e.getVertices()[0])).getKey() + "" + ((e.getVertices()[1])).getKey() + "\n");
//            } else {
//                temp = temp.concat("+" + e.getCost() + "x_" + ((e.getVertices()[0])).getKey() + "" + ((e.getVertices()[1])).getKey());
//                variables = variables.concat( "x_" + ( (e.getVertices()[0])).getKey() + "" + ((e.getVertices()[1])).getKey() + "\n");
//            }
//        }
//        lines.add(temp);
//        
//        
//        return null;
//    }
}
