package net.defekt.racuszki.quadbuilder;

import net.defekt.racuszki.quadbuilder.clones.ClonesUtil;
import net.defekt.racuszki.quadbuilder.mirror.MirrorAxis;
import net.defekt.racuszki.quadbuilder.mirror.MirrorSession;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.bukkit.ChatColor.GREEN;
import static org.bukkit.ChatColor.RED;

public class QuadBuilder extends JavaPlugin {

    private static QuadBuilder instance;

    private final Map<UUID, MirrorSession> sessions = new HashMap<>();
    private ClonesUtil clonesUtil;

    public static QuadBuilder getInstance() {
        return instance;
    }

    public Map<UUID, MirrorSession> getSessions() {
        return sessions;
    }

    public ClonesUtil getShadowUtil() {
        return clonesUtil;
    }

    @Override
    public void onEnable() {
        instance = this;
        clonesUtil = new ClonesUtil();
        getCommand("mirror").setExecutor((sender, a, b, args) -> {
            if (!(sender instanceof Player)) {
                sender.sendMessage(RED + "Tej komendy może użyć tylko gracz!");
                return true;
            }

            Player player = (Player) sender;

            if (args.length > 2) {
                try {
                    MirrorAxis axis = MirrorAxis.valueOf(args[0].toUpperCase());
                    double x, y, z;
                    x = Double.parseDouble(args[1]);
                    z = Double.parseDouble(args[2]);
                    sessions.put(player.getUniqueId(), new MirrorSession(axis, new Vector(x, 0, z)));
                    player.sendMessage(GREEN + "Włączono lustro!");
                    Location loc = new Location(player.getWorld(), x, 0, z);
                    clonesUtil.spawnPlayer(player, loc, -1);
                    clonesUtil.spawnPlayer(player, loc, -2);
                    clonesUtil.spawnPlayer(player, loc, -3);
                } catch (Exception e) {
                    e.printStackTrace();
                    player.sendMessage(RED + e.toString());
                }
            } else
                player.sendMessage(RED + (sessions.remove(player.getUniqueId()) == null ? "Użycie: /mirror <oś> <x> <z>" : "Wyłączono lustro"));

            return true;
        });

        Bukkit.getPluginManager().registerEvents(new BuildListener(this), this);
    }
}
