/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package interfaces;

import java.util.HashSet;

/**
 *
 * @author Marciano
 */
public interface Node {

    public HashSet<Node> getNeighbors();

    public boolean isNeighbor(Node N);

}
