package com.mengcraft.enderchest;

import org.bukkit.ChatColor;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class Executor implements Listener {

    private final Map<String, EnderChest> cache;
    private final List<UUID> list = new ArrayList<>();

    private Main main;
    private ItemUtil util;
    private int size;

    public Executor() {
        this.cache = new ConcurrentHashMap<>();
    }

    @EventHandler
    public void handle(InventoryOpenEvent event) {
        InventoryType type = event.getInventory().getType();
        if (type.equals(InventoryType.ENDER_CHEST)) {
            main.getServer().getScheduler()
                    .runTask(main, new Open(event.getPlayer()));
            event.setCancelled(true);
            list.add(event.getPlayer().getUniqueId());
        }
    }

    private final Holder holder = new Holder();

    private class Open implements Runnable {

        private final HumanEntity player;

        public Open(HumanEntity player) {
            this.player = player;
        }

        public void run() {
            if (player instanceof Player) {
                player.openInventory(getInventory());
            }
        }

        private Inventory getInventory() {
            Inventory inventory = createInventory();
            fill(inventory);
            return inventory;
        }

        private Inventory createInventory() {
            return main.getServer().createInventory(holder, point() * 9);
        }

        private void fill(Inventory inventory) {
            EnderChest chest = cache.get(player.getName());
            if (chest != null && chest.getChest() != null) {
                List<String> list = (List) JSONValue.parse(chest.getChest());
                for (int i = 0; i < list.size() && i < inventory.getSize(); i++) {
                    inventory.setItem(i, convert(list.get(i)));
                }
            }
        }

        private ItemStack convert(String string) {
            if (string != null) try {
                return util.convert(string);
            } catch (Exception e) {
                main.getLogger().warning(e.getMessage());
            }
            return null;
        }

        private int point() {
            int i = 9;
            while (!player.hasPermission("enderchest.size." + i) && i > 0) {
                i = i - 1;
            }
            return i > size ? i : size;
        }
    }

    @EventHandler
    public void handle(PlayerJoinEvent event) {
        if (transform(event.getPlayer()) != 0) {
            String[] strings = {
                    ChatColor.GOLD + "在取出原版末影箱时发生了一些问题",
                    ChatColor.GOLD + "您的背包空余空间无法存放所有物品",
                    ChatColor.GOLD + "这些无法存放的物品已经返回末影箱",
                    ChatColor.GOLD + "请整理背包后重新上下线以取出他们",
            };
            event.getPlayer().sendMessage(strings);
        }
        main.execute(new Fetch(event.getPlayer().getName()));
    }

    private int transform(Player p) {
        Collection<ItemStack> array = new ArrayList<>();
        for (ItemStack stack : p.getEnderChest()) {
            if (stack != null && stack.getTypeId() != 0) array.add(stack);
        }
        p.getEnderChest().clear();
        // Check if origin end-er-chest is empty.
        if (array.size() != 0) {
            array = p.getInventory()
                    .addItem(array.toArray(new ItemStack[]{}))
                    .values();
        }
        // Check if player's inventory is full.
        if (array.size() != 0) {
            p.getEnderChest().addItem(array.toArray(new ItemStack[]{}));
        }
        return array.size();
    }

    @EventHandler
    public void handle(InventoryCloseEvent event) {
        if (holder.equals(event.getInventory().getHolder()) && list.remove(event.getPlayer().getUniqueId())) {
            JSONArray list = new JSONArray();
            for (ItemStack stack : event.getInventory().getContents()) {
                fill(list, stack);
            }
            main.getLogger().log(Level.SEVERE, "Scheduled chest save for " + event.getPlayer().getName() +
                    "!");
            main.execute(new Push(cache.get(event.getPlayer().getName()),
                    list.toString())
            );
        }
    }

    private void fill(List list, ItemStack stack) {
        if (stack != null && stack.getTypeId() != 0) try {
            list.add(util.convert(stack));
        } catch (Exception e) {
            main.getLogger().warning(e.getMessage());
        }
        else {
            list.add(null);
        }
    }

    private class Push implements Runnable {

        private final EnderChest chest;
        private final String data;

        public Push(EnderChest chest, String data) {
            this.chest = chest;
            this.data = data;
        }

        public synchronized void run() {
            chest.setChest(data);
            main.getDatabase().save(chest);
        }

    }

    private class Fetch implements Runnable {

        private final String name;

        public Fetch(String name) {
            this.name = name;
        }

        public void run() {
            EnderChest c = main.getDatabase()
                    .find(EnderChest.class)
                    .where()
                    .eq("player", name)
                    .findUnique();
            if (c == null) {
                c = main.getDatabase().createEntityBean(EnderChest.class);
                c.setPlayer(name);
            }
            cache.put(name, c);
        }

    }

    @EventHandler
    public void handle(PlayerQuitEvent event) {
        cache.remove(event.getPlayer().getName());
    }

    public void bind(Main main, ItemUtil util) {
        if (this.main == null) {
            main.getServer().getPluginManager().registerEvents(this, main);
            // Setup environment.
            this.main = main;
            this.util = util;
            this.size = main.getConfig().getInt("defaultSize", 3);
        }
    }

}
