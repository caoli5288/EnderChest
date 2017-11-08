package com.mengcraft.enderchest;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.mengcraft.enderchest.Main.nil;
import static org.bukkit.event.inventory.ClickType.LEFT;
import static org.bukkit.event.inventory.ClickType.RIGHT;

public class Executor implements Listener, CommandExecutor {

    private final Map<String, Holder> cache;

    private Main main;
    private String title;

    private boolean getOrigin;
    private int minRow;
    private int maxRow;
    private String[] warning;

    public Executor() {
        this.cache = new ConcurrentHashMap<>();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command arg1, String arg2,
                             String[] args) {
        if (sender instanceof Player) {
            Player player = Player.class.cast(sender);
            if (args.length != 1) {
                open(player);
            } else try {
                openWithPage(player, Integer.parseInt(args[0]) - 1);
            } catch (Exception e) {
                player.sendMessage(ChatColor.DARK_RED + "发生了一些问题");
            }
            return true;
        }
        return false;
    }

    private void openWithPage(Player player, int page) {
        Holder holder = getCache().get(player.getName());
        if (holder != null && holder.hasPage(page)) {
            holder.setPage(page);
            open(player);
        } else {
            player.sendMessage(ChatColor.DARK_RED + "发生了一些问题");
        }
    }

    public void bind(Main main, ItemUtil util) {
        if (this.main == null) {
            main.getServer().getPluginManager().registerEvents(this, main);
            main.getCommand("enderchest").setExecutor(this);
            // Setup environment.
            this.title = main.getConfig().getString("global.title", "第%d页");
            this.main = main;
            this.minRow = main.getConfig().getInt("global.minRow", 3);
            this.maxRow = main.getConfig().getInt("global.maxRow", 30);
            this.getOrigin = main.getConfig().getBoolean("global.getOrigin");
            this.warning = main.getConfig()
                    .getStringList("global.warning")
                    .toArray(new String[]{});
        }
    }

    @EventHandler
    public void handle(PlayerJoinEvent event) {
        Holder holder = new Holder();
        holder.setPlayer(event.getPlayer())
                .setMaxRow(getMaxRow(event.getPlayer(), maxRow))
                .setTitle(title);
        getCache().put(event.getPlayer().getName(), holder);
        if (getOrigin && transform(event.getPlayer()) != 0) {
            event.getPlayer().sendMessage(warning);
        }
        Main.runAsync(holder::update);
    }

    @EventHandler
    public void handle(InventoryClickEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof Holder) {
            int slot = event.getRawSlot();
            if (slot == -999) {
                if (event.getClick().equals(LEFT)) {
                    openWithOffset(event.getWhoClicked(), 1);
                } else if (event.getClick().equals(RIGHT)) {
                    openWithOffset(event.getWhoClicked(), -1);
                }
            }
        }
    }

    @EventHandler
    public void handle(InventoryOpenEvent event) {
        InventoryType type = event.getInventory().getType();
        if (type.equals(InventoryType.ENDER_CHEST)) {
            openWithOffset(event.getPlayer(), 0);
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void handle(InventoryCloseEvent event) {
        if (event.getInventory().getHolder() instanceof Holder) {
            Pair<Integer, Inventory> removal = Open.OPEN_INVENTORY.remove(event.getPlayer().getUniqueId());
            if (nil(removal)) {
                return;
            }

            Holder holder = (Holder) event.getInventory().getHolder();
            holder.close(removal);
        }
    }

    @EventHandler
    public void handle(PlayerQuitEvent event) {
        Holder remove = cache.remove(event.getPlayer().getName());
        Main.runAsync(remove::saveAll);
    }

    public int getMaxRow(Player player, int i) {
        while (!player.hasPermission("enderchest.size." + i) && i > minRow) {
            i = i - 1;
        }
        return i;
    }

    private int transform(Player p) {
        Collection<ItemStack> array = new ArrayList<>();
        // Purge and transform origin ender chest.
        for (ItemStack stack : p.getEnderChest()) {
            if (stack != null && stack.getTypeId() != 0) array.add(stack);
        }
        p.getEnderChest().clear();

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

    /**
     * Let given player open his chest on current page.
     *
     * @param player
     */
    private void open(HumanEntity player) {
        openWithOffset(player, 0);
    }

    /**
     * Let given player open his chest with given page offset.
     *
     * @param player Player who will open chest.
     * @param offset Chest page offset.
     */
    private void openWithOffset(HumanEntity player, int offset) {
        Bukkit.getScheduler().runTask(main, new Open(getCache(), player, offset));
    }

    public Map<String, Holder> getCache() {
        return cache;
    }

}
