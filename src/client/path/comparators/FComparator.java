package client.path.comparators;

import client.path.Node;

import java.util.Comparator;

public class FComparator implements Comparator<Node> {

    @Override
    public int compare(Node n1, Node n2) {
        return n1.f() - n2.f();
    }

}
