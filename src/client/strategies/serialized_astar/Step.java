//package client.strategies.serialized_astar;
//
//import client.state.Position;
//
//import java.util.Comparator;
//
//class Step implements Comparator<Step> {
//
//    public Position position;
//    public int g;
//    public int h;
//    public Step parent;
//
//    public Step(Position position, int g, int h, Step parent) {
//        this.position = position;
//        this.g = g;
//        this.h = h;
//        this.parent = parent;
//    }
//
//    @Override
//    public int compare(Step s1, Step s2) {
//        return s1.g + s1.h - s2.g - s2.h;
//    }
//
//}
