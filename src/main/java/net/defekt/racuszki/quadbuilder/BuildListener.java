package net.defekt.racuszki.quadbuilder;

import io.papermc.paper.event.player.PlayerInventorySlotChangeEvent;
import net.defekt.racuszki.quadbuilder.clones.ClonesUtil;
import net.defekt.racuszki.quadbuilder.mirror.MirrorAxis;
import net.defekt.racuszki.quadbuilder.mirror.MirrorSession;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.structure.Mirror;
import org.bukkit.block.structure.StructureRotation;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class BuildListener implements Listener {
    private final QuadBuilder plugin;

    public BuildListener(QuadBuilder plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBuild(BlockPlaceEvent e) {
        mirrorBlock(e.getPlayer(), e.getBlock().getLocation(), e.getBlock().getBlockData());
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        mirrorBlock(e.getPlayer(), e.getBlock().getLocation(), Material.AIR.createBlockData());
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getClickedBlock() == null) return;
        BlockData data = e.getClickedBlock().getBlockData();
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            if (data != null && !data.equals(e.getClickedBlock().getBlockData()))
                mirrorBlock(e.getPlayer(), e.getClickedBlock().getLocation(), e.getClickedBlock().getBlockData());
        }, 1);
    }

    private void mirrorBlock(Player player, Location at, BlockData block) {
        MirrorSession ses = plugin.getSessions().get(player.getUniqueId());
        if (ses != null) {
            at = at.toCenterLocation();
            Vector center = ses.getCenter();
            MirrorAxis axis = ses.getAxis();
            World world = at.getWorld();

            double cx, cz;
            cx = center.getX();
            cz = center.getZ();

            double x, y, z;
            x = at.getX();
            y = at.getY();
            z = at.getZ();

            double mx, mz;
            mx = mirror(cx, x);
            mz = mirror(cz, z);

            if (axis == MirrorAxis.X || axis == MirrorAxis.HORIZONTAL) {
                BlockData clone = block.clone();
                clone.mirror(Mirror.LEFT_RIGHT);
                new Location(world, x, y, mz).getBlock().setBlockData(clone);
                plugin.getShadowUtil().animate(player, -1);
            }

            if (axis == MirrorAxis.Z || axis == MirrorAxis.HORIZONTAL) {
                BlockData clone = block.clone();
                clone.mirror(Mirror.FRONT_BACK);
                new Location(world, mx, y, z).getBlock().setBlockData(clone);
                plugin.getShadowUtil().animate(player, -2);
            }

            if (axis == MirrorAxis.HORIZONTAL) {
                BlockData clone = block.clone();
                clone.rotate(StructureRotation.CLOCKWISE_180);
                new Location(world, mx, y, mz).getBlock().setBlockData(clone);
                plugin.getShadowUtil().animate(player, -3);
            }

        }
    }

    private void handChanged(Player player, ItemStack item, MirrorSession ses) {
        ClonesUtil util = plugin.getShadowUtil();
        int npc = ses.getNpc();
        switch (npc) {
            case 2:
            case 1: {
                util.setHand(player, -npc, item);
                break;
            }
            case 3: {
                for (int x = 1; x <= 3; x++)
                    util.setHand(player, -x, item);
                break;
            }
            default:
                break;
        }
    }

    @EventHandler
    public void onHandChange(PlayerItemHeldEvent e) {
        Player player = e.getPlayer();
        MirrorSession ses = plugin.getSessions().get(player.getUniqueId());
        if (ses != null) {
            ItemStack item = player.getInventory().getItem(e.getNewSlot());
            handChanged(player, item, ses);
        }
    }

    @EventHandler
    public void onInventoryChange(PlayerInventorySlotChangeEvent e) {
        Player player = e.getPlayer();
        MirrorSession ses = plugin.getSessions().get(player.getUniqueId());
        if (ses != null) {
            if (e.getSlot() == player.getInventory().getHeldItemSlot()) handChanged(player, e.getNewItemStack(), ses);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();
        MirrorSession ses = plugin.getSessions().get(player.getUniqueId());
        if (ses != null) {
            ClonesUtil util = plugin.getShadowUtil();
            MirrorAxis axis = ses.getAxis();
            Vector center = ses.getCenter();
            Location loc = player.getLocation();
            World world = loc.getWorld();

            float yaw, pitch;
            yaw = loc.getYaw();
            pitch = loc.getPitch();

            double x, y, z;
            x = loc.getX();
            y = loc.getY();
            z = loc.getZ();

            double mx, mz;
            mx = mirror(center.getX(), loc.getX());
            mz = mirror(center.getZ(), loc.getZ());

            if (axis == MirrorAxis.Z || axis == MirrorAxis.HORIZONTAL) {
                util.movePlayer(player, new Location(world, x, y, mz, -toAngle(yaw), pitch), -1);
            }
            if (axis == MirrorAxis.X || axis == MirrorAxis.HORIZONTAL) {
                util.movePlayer(player, new Location(world, mx, y, z, -toAngle(yaw - 180), pitch), -2);
            }
            if (axis == MirrorAxis.HORIZONTAL) {
                util.movePlayer(player, new Location(world, mx, y, mz, toAngle(yaw), pitch), -3);
            }
        }
    }

    private byte toAngle(float yaw) {
        yaw = (yaw + 180) % 360;
        return (byte) (yaw / 360 * 256);
    }

    private double mirror(double center, double pos) {
        return center + (pos - center) * -1;
    }
}
