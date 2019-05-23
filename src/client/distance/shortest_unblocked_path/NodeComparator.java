package client.distance.shortest_unblocked_path;

import java.util.Comparator;

public class NodeComparator implements Comparator<Node> {

    @Override
    public int compare(Node n1, Node n2) {
        return n1.f() - n2.f();
    }

}
