package client.graph;

import java.util.Comparator;

public class CommandComparator implements Comparator<Command> {

    @Override
    public int compare(Command c1, Command c2) {
        switch (c1.actionType) {
            case NoOp:
                switch (c2.actionType) {
                    case NoOp:
                        return 0;
                    default:
                        return -1;
                }
            case Move:
                switch (c2.actionType) {
                    case NoOp:
                        return 1;
                    case Pull:
                    case Push:
                        return -1;
                    default:
                        return this.compareDirection(c1.dir1, c2.dir1);
                }
            case Pull:
                switch (c2.actionType) {
                    case NoOp:
                    case Move:
                        return 1;
                    case Push: return -1;
                    default:
                        int pullDir1 = this.compareDirection(c1.dir1, c2.dir1);
                        return pullDir1 == 0 ? this.compareDirection(c1.dir2, c2.dir2) : pullDir1;
                }
            case Push:
                switch (c2.actionType) {
                    case NoOp:
                    case Move:
                    case Pull:
                        return 1;
                    default:
                        int pushDir1 = this.compareDirection(c1.dir1, c2.dir1);
                        return pushDir1 == 0 ? this.compareDirection(c1.dir2, c2.dir2) : pushDir1;
                }
            default:
                return 0;
        }
    }

    private int compareDirection(Command.Dir d1, Command.Dir d2) {
        if (d1 == d2) {
            return 0;
        } else {
            switch (d1) {
                case N: return -1;
                case E:
                    switch (d2) {
                        case N: return 1;
                        default: return -1;
                    }
                case S:
                    switch (d2) {
                        case N:
                        case E: return 1;
                        default: return -1;
                    }
                case W: return 1;
                default: return 0;
            }
        }
    }

}
