package com.mengcraft.enderchest;

import com.avaje.ebean.EbeanServer;
import com.mengcraft.enderchest.entity.EnderChest;
import com.mengcraft.enderchest.entity.EnderChestStack;
import com.mengcraft.enderchest.entity.Row;
import com.mengcraft.simpleorm.EbeanHandler;
import com.mengcraft.simpleorm.EbeanManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONArray;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class Main extends JavaPlugin {

    private static Messenger messenger;
    private static ItemUtil util;
    private static EbeanServer database;

    public static EbeanServer getDb() {
        return database;
    }

    public static Messenger getMessenger() {
        return messenger;
    }

    /**
     * @return true if contend need persist
     */
    public static boolean flip(EnderChest i) {
        List<String> list = new ArrayList<>();
        i.getAllRow().forEach(row -> list.addAll(row.toRawList()));
        String old = i.getContend();
        String ctx = JSONArray.toJSONString(list);
        i.setContend(ctx);
        return !ctx.equals(old);
    }

    public static void buildRow(EnderChest i) {
        i.setAllRow(Row.build(i.getContend()));
    }


    @Override
    public void onEnable() {
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();

        messenger = new Messenger(this);

        util = new ItemUtilHandler(this).handle();

		EbeanManager manager = getServer().getServicesManager()
		        .getRegistration(EbeanManager.class)
		        .getProvider();
		EbeanHandler handler = manager.getHandler(this);

		if (!handler.isInitialized()) {
			handler.define(EnderChest.class);
            handler.define(EnderChestStack.class);
            try {
                handler.initialize();
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage());
            }
        }

		handler.install();
		handler.reflect();

        database = handler.getServer();

        String[] strings = {
                ChatColor.GREEN + "梦梦家高性能服务器出租店",
                ChatColor.GREEN + "shop105595113.taobao.com"
        };
        getServer().getConsoleSender().sendMessage(strings);

        new Executor().bind(this, util);
    }

    public static String encode(ItemStack item) {
        if (nil(item) || item.getType() == Material.AIR) {
            return "";
        }
        return util.convert(item);
    }

    public static ItemStack decode(String data) {
        if (nil(data) || data.isEmpty()) {
            return AIR;
        }
        return util.convert(data);
    }

    public static void thr(boolean b, String message) {
        if (b) throw new IllegalStateException(message);
    }

    public static boolean nil(Object any) {
        return any == null;
    }

    public static void runAsync(Runnable runnable) {
        CompletableFuture.runAsync(runnable).exceptionally(thr -> {
            Bukkit.getLogger().log(Level.WARNING, thr, thr::getMessage);
            return null;
        });
    }

    private static final ItemStack AIR = new ItemStack(Material.AIR);
}
