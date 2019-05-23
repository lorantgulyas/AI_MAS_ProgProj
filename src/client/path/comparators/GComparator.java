package client.path.comparators;

import client.path.Node;

import java.util.Comparator;

public class GComparator implements Comparator<Node> {

    @Override
    public int compare(Node n1, Node n2) {
        return n1.g() - n2.g();
    }

}
