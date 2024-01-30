package net.defekt.racuszki.quadbuilder.clones;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.Pair;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import net.defekt.racuszki.quadbuilder.QuadBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.UUID;

public class ClonesUtil {
    private final ProtocolManager protocol;

    public ClonesUtil() {
        protocol = ProtocolLibrary.getProtocolManager();
    }

    public void animate(Player player, int id) {
        PacketContainer packet = protocol.createPacket(PacketType.Play.Server.ANIMATION);
        packet.getIntegers().write(0, id).write(1, 0);
        protocol.sendServerPacket(player, packet);
    }

    public void setHand(Player player, int id, ItemStack item) {
        PacketContainer packet = protocol.createPacket(PacketType.Play.Server.ENTITY_EQUIPMENT);
        packet.getIntegers().write(0, id);
        packet.getSlotStackPairLists()
              .write(0, Collections.singletonList(new Pair<>(EnumWrappers.ItemSlot.MAINHAND, item)));
        protocol.sendServerPacket(player, packet);
    }

    public void movePlayer(Player player, Location loc, int id) {
        PacketContainer packet = protocol.createPacket(PacketType.Play.Server.ENTITY_TELEPORT);
        packet.getIntegers().write(0, id);
        packet.getDoubles().write(0, loc.getX()).write(1, loc.getY()).write(2, loc.getZ());
        packet.getBytes().write(0, (byte) loc.getYaw()).write(1, (byte) loc.getPitch());

        protocol.sendServerPacket(player, packet);

        PacketContainer headPacket = protocol.createPacket(PacketType.Play.Server.ENTITY_HEAD_ROTATION);
        headPacket.getIntegers().write(0, id);
        headPacket.getBytes().write(0, (byte) loc.getYaw());

        protocol.sendServerPacket(player, headPacket);
    }

    public void hidePlayer(Player player, int id) {
        PacketContainer packet = protocol.createPacket(PacketType.Play.Server.ENTITY_DESTROY);
        packet.getIntLists().write(0, Collections.singletonList(id));
        protocol.sendServerPacket(player, packet);
    }

    public void spawnPlayer(Player player, Location loc, int id) {
        PacketContainer packet = protocol.createPacket(PacketType.Play.Server.PLAYER_INFO);
        packet.getPlayerInfoActions().write(0, Collections.singleton(EnumWrappers.PlayerInfoAction.ADD_PLAYER));

        UUID uid = UUID.randomUUID();
        packet.getPlayerInfoDataLists()
              .write(1,
                     Collections.singletonList(new PlayerInfoData(new WrappedGameProfile(uid, player.getName()),
                                                                  0,
                                                                  EnumWrappers.NativeGameMode.CREATIVE,
                                                                  null)));
        protocol.sendServerPacket(player, packet);

        packet = protocol.createPacket(PacketType.Play.Server.NAMED_ENTITY_SPAWN);
        packet.getIntegers().write(0, id);
        packet.getUUIDs().write(0, uid);
        packet.getDoubles().write(0, loc.getX()).write(1, loc.getY()).write(2, loc.getZ());

        protocol.sendServerPacket(player, packet);

        PacketContainer removePacket = protocol.createPacket(PacketType.Play.Server.PLAYER_INFO_REMOVE);
        removePacket.getUUIDLists().write(0, Collections.singletonList(uid));

        Bukkit.getScheduler()
              .scheduleSyncDelayedTask(QuadBuilder.getInstance(),
                                       () -> protocol.sendServerPacket(player, removePacket),
                                       20);

        setHand(player, id, player.getInventory().getItemInMainHand());
    }
}
