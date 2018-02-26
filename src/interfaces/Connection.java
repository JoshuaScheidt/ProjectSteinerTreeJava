/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package interfaces;

import java.util.ArrayList;
import java.util.Optional;

/**
 *
 * @author Marciano
 */
public interface Connection {

    public ArrayList<Node> getNodes();

    public Node getOtherSide(Node N);
    
    public Optional<Integer> getCost();
}
