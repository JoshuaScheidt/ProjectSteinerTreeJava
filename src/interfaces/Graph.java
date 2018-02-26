/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package interfaces;

import java.util.HashMap;
import java.util.HashSet;

/**
 *
 * @author Marciano
 */
public interface Graph{

    public void addNode(Node N);
    
    public void addEdge(Node N1, Node N2, int cost);

    public void removeNode(Node N);

    public boolean containsNode(Node N);

    public boolean containsEdge(Connection E);

    public HashMap<Integer, Node> getNode();

    public HashSet<Connection> getEdges();
    
    public int getEdgesSize();
    
    public int getNodesSize();

	

}
