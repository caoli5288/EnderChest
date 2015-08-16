package com.mengcraft.enderchest;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

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

public class Executor implements Listener {

	private final Map<String, EnderChest> cache;

	private Main main;
	private ItemUtil util;
	private int size;

	private final ExecutorService service;

	public Executor() {
		this.service = new ThreadPoolExecutor(
		        1, 4,
		        60, SECONDS,
		        new LinkedBlockingQueue());
		this.cache = new ConcurrentHashMap<>();
	}

	@EventHandler
	public void handle(InventoryOpenEvent event) {
		InventoryType type = event.getInventory().getType();
		if (type.equals(InventoryType.ENDER_CHEST)) {
			main.getServer().getScheduler()
			        .runTask(main, new B(event.getPlayer()));
			event.setCancelled(true);
		}
	}

	private final Holder holder = new Holder();

	private class B implements Runnable {

		private final HumanEntity player;

		public B(HumanEntity player) {
			this.player = player;
		}

		public void run() {
			if (player instanceof Player) {
				player.closeInventory();
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
				for (int i = 0; i < inventory.getSize(); i++) {
					inventory.setItem(i, convert(list.get(i)));
				}
			}
		}

		private ItemStack convert(String string) {
			try {
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
		service.execute(new A(event.getPlayer().getName()));
	}

	@EventHandler
	public void handle(InventoryCloseEvent event) {
		if (event.getInventory().getHolder().equals(holder)) {
			JSONArray list = new JSONArray();
			for (ItemStack stack : event.getInventory().getContents()) {
				fill(list, stack);
			}
			service.execute(new C(event.getPlayer().getName(), list.toString()));
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

	private class C implements Runnable {

		private final String name;
		private final String data;

		public C(String name, String data) {
			this.name = name;
			this.data = data;
		}

		public void run() {
			EnderChest c = cache.get(name);
			c.setChest(data);
			main.getDatabase().save(c);
		}

	}

	private class A implements Runnable {

		private final String name;

		public A(String name) {
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
			this.main = main;
			this.util = util;
			this.size = main.getConfig().getInt("defaultSize", 3);
		}
		main.getServer().getPluginManager().registerEvents(this, main);
	}

}
