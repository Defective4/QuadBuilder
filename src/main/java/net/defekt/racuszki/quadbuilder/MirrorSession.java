package net.defekt.racuszki.quadbuilder;

import org.bukkit.util.Vector;

public class MirrorSession {
    private final MirrorAxis axis;
    private final Vector center;
    private final int npc;

    public MirrorSession(MirrorAxis axis, Vector center) {
        this.axis = axis;
        this.center = center;
        npc = axis == MirrorAxis.X ? 1 : axis == MirrorAxis.Z ? 2 : 3;
    }

    public int getNpc() {
        return npc;
    }

    public MirrorAxis getAxis() {
        return axis;
    }

    public Vector getCenter() {
        return center;
    }
}
