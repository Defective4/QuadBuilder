package net.defekt.racuszki.quadbuilder.ui;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GUI2Holder implements InventoryHolder {

    public static class GUIAction {
        private final String action, secondaryAction;
        private final String[] data;

        GUIAction(String action, String secondaryAction, String data) {
            this(action, secondaryAction, new String[]{
                    data
            });
        }

        GUIAction(String action, String secondaryAction, String[] data) {
            this.action = action;
            this.secondaryAction = secondaryAction;
            this.data = data;
        }

        public String getAction() {
            return action;
        }

        public String getSecondaryAction() {
            return secondaryAction;
        }

        public String[] getData() {
            return data;
        }
    }

    private final GUI2Manager parent;
    private final Map<Integer, GUIAction> actions = new ConcurrentHashMap<>();
    private Inventory inventory;

    public GUI2Holder(GUI2Manager parent) {
        this.parent = parent;
    }

    public GUI2Manager getParent() {
        return parent;
    }

    public void fill(List<GUI2Descriptor.ItemDescriptor> items, int page) {
        if (page < 0)
            page = 0;
        int air = 0;
        for (int x = 0; x < inventory.getSize(); x++) {
            ItemStack item = inventory.getItem(x);
            if (item == null || item.getType() == Material.AIR)
                air++;
        }

        int start = air * page;
        for (int x = start; x < start + air; x++) {
            if (items.size() <= x)
                break;
            int slot = inventory.firstEmpty();
            GUI2Descriptor.ItemDescriptor desc = items.get(x);
            inventory.setItem(slot, desc.convert());
            actions.put(slot, new GUIAction(desc.getAction(), desc.getSecondaryAction(), desc.getData()));
        }
    }

    public GUIAction getAction(int slot) {
        return actions.getOrDefault(slot, new GUIAction(
                null, null, ""
        ));
    }

    public Map<Integer, GUIAction> getActions() {
        return actions;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }
}
