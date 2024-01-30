package net.defekt.racuszki.quadbuilder.ui;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public interface GUI2Listener {
    void guiClicked(Player player, String action, String[] data, Inventory inv);
}
