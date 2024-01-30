package net.defekt.racuszki.quadbuilder.ui;


import net.defekt.racuszki.quadbuilder.NBTEditor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class GUI2Descriptor {

    public static class ItemDescriptor {
        private final Material type;
        private final String name, action, secondaryAction, data;
        private final boolean glowing;
        private final int count;
        private final List<String> lore;

        public ItemDescriptor(
                Material type, String name, List<String> lore, String action, String secondaryAction, String data,
                boolean glowing, int count
        ) {
            this.type = type;
            this.name = name;
            this.action = action;
            this.secondaryAction = secondaryAction;
            this.data = data;
            this.glowing = glowing;
            this.count = count;
            this.lore = lore == null ? new ArrayList<>() : lore;
        }

        public ItemStack convert() {
            ItemStack item = new ItemStack(type, count);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(name);
                meta.setLore(lore);
                if (glowing) {
                    meta.addEnchant(Enchantment.DURABILITY, 1, true);
                }
                meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_POTION_EFFECTS);
                item.setItemMeta(meta);
            }
            return NBTEditor.set(item, GUI2Manager.RAND.nextLong(), "g2Seed");
        }

        public int getCount() {
            return count;
        }

        public Material getType() {
            return type;
        }

        public String getName() {
            return name;
        }

        public String getAction() {
            return action;
        }

        public String getSecondaryAction() {
            return secondaryAction;
        }

        public String getData() {
            return data;
        }

        public boolean isGlowing() {
            return glowing;
        }
    }

    private final String title;
    private final List<String> layout;

    private final Map<String, ItemDescriptor> items;

    public GUI2Descriptor(String title, List<String> layout, Map<String, ItemDescriptor> items) {
        this.title = title;
        this.layout = layout;
        this.items = items;
    }

    public String getTitle() {
        return title;
    }

    public List<String> getLayout() {
        return Collections.unmodifiableList(layout);
    }

    public Map<String, ItemDescriptor> getItems() {
        return Collections.unmodifiableMap(items);
    }
}
