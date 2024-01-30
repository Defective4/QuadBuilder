package net.defekt.racuszki.quadbuilder.ui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GUI2Manager implements Listener {

    public static class GUIData {
        private final String[] data;

        public GUIData(String... data) {
            this.data = data;
        }

        public String[] getData() {
            return data;
        }
    }

    protected final static Random RAND = new Random();
    private final JavaPlugin plugin;
    private final Map<String, GUI2Descriptor> loadedWindows = new ConcurrentHashMap<>();
    private final List<GUI2Listener> listeners = new ArrayList<>();

    public GUI2Manager(JavaPlugin plugin) {
        this.plugin = plugin;

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public static GUIData[] calculatePages(int page) {
        return new GUIData[]{new GUIData(String.valueOf(page <= 0 ? 0 : page - 1)),
                             new GUIData(String.valueOf(page + 1))};
    }

    private static List<String> createLore(String... lines) {
        return Stream.of(lines).map(GUI2Manager::translate).collect(Collectors.toList());
    }

    private static String translate(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public void addListener(GUI2Listener listener) {
        listeners.add(listener);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        Inventory inv = e.getInventory();
        if (inv.getHolder() instanceof GUI2Holder && e.getWhoClicked() instanceof Player) {
            e.setCancelled(true);
            GUI2Holder holder = (GUI2Holder) inv.getHolder();
            if (holder.getParent() == this && e.getClickedInventory() == inv) {
                GUI2Holder.GUIAction action = holder.getAction(e.getSlot());
                String[] data = action.getData();
                if (data == null) data = new String[0];
                String aString = e.getClick()
                                  .name()
                                  .contains("RIGHT") ? action.getSecondaryAction() : action.getAction();
                if (aString != null) for (GUI2Listener ls : listeners)
                    ls.guiClicked((Player) e.getWhoClicked(), aString, data, inv);
            }
        }
    }

    public Inventory openWindow(String resource, HumanEntity target, GUIData... data) {
        return openWindow(resource, target, null, data);
    }

    public Inventory openWindow(String resource, HumanEntity target, GUI2Provider provider, GUIData... data) {
        GUI2Descriptor gui = loadedWindows.get(resource);
        if (gui == null) return null;

        List<String> layout = gui.getLayout();
        Map<String, GUI2Descriptor.ItemDescriptor> items = gui.getItems();

        GUI2Holder holder = new GUI2Holder(this);
        Map<Integer, GUI2Holder.GUIAction> actions = holder.getActions();
        Inventory inv;
        inv = layout.size() == 1 && layout.get(0).length() == 5 ? Bukkit.createInventory(holder,
                                                                                         InventoryType.HOPPER,
                                                                                         gui.getTitle()) : Bukkit.createInventory(
                holder,
                layout.size() * 9,
                gui.getTitle());
        holder.setInventory(inv);

        int dataIndex = 0;
        for (int x = 0; x < layout.size(); x++) {
            String line = layout.get(x);
            if (line.length() > 9) line = line.substring(0, 8);
            for (int z = 0; z < line.length(); z++) {
                String key = line.substring(z, z + 1).toLowerCase();
                GUI2Descriptor.ItemDescriptor idesc = items.get(key);

                if (idesc != null) {
                    int index = x * 9 + z;
                    String dt = idesc.getData();
                    String[] localData;
                    if (dt == null) localData = new String[0];
                    else if (dt.equalsIgnoreCase("context")) {
                        localData = data[dataIndex % data.length].getData();
                        dataIndex++;
                    } else localData = new String[]{dt};
                    GUI2Holder.GUIAction action = new GUI2Holder.GUIAction(idesc.getAction(),
                                                                           idesc.getSecondaryAction(),
                                                                           localData);
                    actions.put(index, action);
                    ItemStack item = idesc.convert();
                    if (provider != null) {
                        Object value = provider.provide(action);
                        if (value != null) {
                            ItemMeta meta = item.getItemMeta();
                            if (meta != null) {
                                List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
                                if (lore != null) {
                                    lore.addAll(createLore("&7",
                                                           "&eCurrent value&7: " + value,
                                                           "&7&l >&a Click to change"));
                                    meta.setLore(lore);
                                }
                                item.setItemMeta(meta);
                            }
                        }
                    }
                    inv.setItem(index, item);
                }
            }
        }

        target.openInventory(inv);
        return inv;
    }

    public void loadWindows(String... resources) {
        for (String res : resources) {
            loadWindow(res, plugin.getResource(res + ".yml"));
        }
    }

    private void loadWindow(String resource, InputStream is) {
        if (is != null) try {
            FileConfiguration config = YamlConfiguration.loadConfiguration(new InputStreamReader(is));
            String title = translate(config.isString("title") ? config.getString("title") : "&8Generated GUI");
            List<String> layout = config.isList("layout") ? config.getStringList("layout") : Collections.singletonList(
                    "000000000");
            Map<String, GUI2Descriptor.ItemDescriptor> items = new ConcurrentHashMap<>();
            ConfigurationSection itemsSect = config.getConfigurationSection("items");
            if (itemsSect != null) {
                for (Map.Entry<String, Object> itemSect : itemsSect.getValues(false).entrySet()) {
                    if (itemSect.getValue() instanceof ConfigurationSection) {
                        String key = itemSect.getKey().toLowerCase();
                        ConfigurationSection value = (ConfigurationSection) itemSect.getValue();
                        GUI2Descriptor.ItemDescriptor item = new GUI2Descriptor.ItemDescriptor(value.isString("type") ? Material.valueOf(
                                value.getString("type").toUpperCase(Locale.ENGLISH)) : Material.BARRIER,
                                                                                               translate(value.isString(
                                                                                                       "name") ? "&f" + value.getString(
                                                                                                       "name") : "&f"),
                                                                                               value.isList("lore") ? createLore(
                                                                                                       value.getStringList(
                                                                                                                    "lore")
                                                                                                            .toArray(new String[0])) : Collections.emptyList(),
                                                                                               value.isString("action") ? value.getString(
                                                                                                       "action") : null,
                                                                                               value.isString(
                                                                                                       "secondaryAction") ? value.getString(
                                                                                                       "secondaryAction") : null,
                                                                                               value.isString(
                                                                                                       "actionData") ? value.getString(
                                                                                                       "actionData") : null,
                                                                                               value.getBoolean(
                                                                                                       "glowing"),
                                                                                               value.isInt("count") ? value.getInt(
                                                                                                       "count") : 1);
                        items.put(key, item);
                    }
                }
            }
            loadedWindows.put(resource, new GUI2Descriptor(title, layout, items));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException ignored) {
            }
        }
    }

    public JavaPlugin getBoundPlugin() {
        return plugin;
    }

    public void unregister() {
        loadedWindows.clear();
    }
}
