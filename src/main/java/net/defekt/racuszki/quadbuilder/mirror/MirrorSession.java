package net.defekt.racuszki.quadbuilder.mirror;

import org.bukkit.util.Vector;

public class MirrorSession {
    private final MirrorAxis axis;
    private final Vector center;
    private final int npc;
    private byte visibleNPCs = 0b111;

    public MirrorSession(MirrorAxis axis, Vector center) {
        this.axis = axis;
        this.center = center;
        npc = axis == MirrorAxis.X ? 1 : axis == MirrorAxis.Z ? 2 : 3;
    }

    public byte getVisibleNPCs() {
        return visibleNPCs;
    }

    public void setVisibleNPCs(byte visibleNPCs) {
        this.visibleNPCs = visibleNPCs;
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
