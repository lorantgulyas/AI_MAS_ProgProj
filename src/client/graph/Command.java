package client.graph;

import java.util.ArrayList;

public class Command {
    // Order of enum important for determining opposites.
    public enum Dir {
        N, W, E, S
    }

    public enum Type {
        Move, Push, Pull, NoOp
    }

    public static final Command NoOp = new Command();

    public static final Command[] EVERY;
    static {
		ArrayList<Command> cmds = new ArrayList<>();
        for (Dir d1 : Dir.values()) {
            for (Dir d2 : Dir.values()) {
                if (!Command.isOpposite(d1, d2)) {
                    cmds.add(new Command(Type.Push, d1, d2));
                }
            }
        }
        for (Dir d1 : Dir.values()) {
            for (Dir d2 : Dir.values()) {
                if (d1 != d2) {
                    cmds.add(new Command(Type.Pull, d1, d2));
                }
            }
        }
        for (Dir d : Dir.values()) {
            cmds.add(new Command(d));
        }

        cmds.add(Command.NoOp);

        EVERY = cmds.toArray(new Command[0]);
    }

    private static boolean isOpposite(Dir d1, Dir d2) {
        return d1.ordinal() + d2.ordinal() == 3;
    }

    public static int dirToRowChange(Dir d) {
        // South is down one row (1), north is up one row (-1).
        switch (d) {
        case S:
            return 1;
        case N:
            return -1;
        default:
            return 0;
        }
    }

    public static int dirToColChange(Dir d) {
        // East is right one column (1), west is left one column (-1).
        switch (d) {
        case E:
            return 1;
        case W:
            return -1;
        default:
            return 0;
        }
    }

    public final Type actionType;
    public final Dir dir1;
    public final Dir dir2;

    public Command(Dir d) {
        this.actionType = Type.Move;
        this.dir1 = d;
        this.dir2 = null;
    }

    public Command(Type t, Dir d1, Dir d2) {
        this.actionType = t;
        this.dir1 = d1;
        this.dir2 = d2;
    }

    private Command() {
        this.actionType = Type.NoOp;
        this.dir1 = null;
        this.dir2 = null;
    }

    @Override
    public String toString() {
        if (this.actionType == Type.Move)
            return String.format("%s(%s)", this.actionType.toString(), this.dir1.toString());
        else if (this.actionType == Type.NoOp)
            return "NoOp";
        else
            return String.format("%s(%s,%s)", this.actionType.toString(), this.dir1.toString(), this.dir2.toString());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (obj.getClass() != this.getClass())
            return false;
        Command other = (Command) obj;
        if (this.actionType != other.actionType)
            return false;
        switch (this.actionType) {
            case Move: return this.dir1 == other.dir1;
            case Push:
            case Pull: return this.dir1 == other.dir1 && this.dir2 == other.dir2;
            case NoOp: return true;
            default: return false;
        }
    }

    @Override
    public int hashCode() {
        switch (this.actionType) {
            case Move: return 1;
            case Push: return 2;
            case Pull: return 3;
            case NoOp: return 4;
            default: return 5;
        }
    }
}
