package com.mengcraft.enderchest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.utility.StreamSerializer;
import com.mengcraft.db.MengDB;
import com.mengcraft.db.MengRecord;

public class Events implements Listener {

	private final Map<String, Inventory> inventories = DataManager.getManager().getInventories();
	private final Plugin plugin;

	@EventHandler
	public void open(InventoryOpenEvent event) {
		String type = event.getInventory().getType().name();
		if (type.equals("ENDER_CHEST")) {
			Inventory inventory = getInventory(event.getPlayer());
			this.plugin.getServer().getScheduler().runTaskLater(this.plugin, new OpenTask(event.getPlayer(), inventory), 1);
			event.setCancelled(true);
		}
	}

	private Inventory getInventory(HumanEntity player) {
		if (this.inventories.containsKey(player.getName())) {
			return this.inventories.get(player.getName());
		} else {
			int row = checkPermission(player);
			Inventory empty = this.plugin.getServer().createInventory(null, row * 9, "container.enderchest");
			fill(empty, player.getName());
			this.inventories.put(player.getName(), empty);
			
			return empty;
		}
	}

	private void fill(Inventory empty, String name) {
		MengRecord record = MengDB.getManager().getTable("enderchest").findOne("name", name);
		if (record != null) {
			List<String> items = record.getStringList("items");
			ItemStack[] stacks = getItems(items, empty.getSize());
			empty.setContents(stacks);
		}
	}

	private ItemStack[] getItems(List<String> items, int i) {
		List<ItemStack> list = new ArrayList<>();
		for (Iterator<String> iterator = items.iterator(); iterator.hasNext() && list.size() < i;) {
			String in = iterator.next();
			list.add(fromString(in));
		}
		return list.toArray(new ItemStack[] {});
	}

	private ItemStack fromString(String in) {
		StreamSerializer serializer = StreamSerializer.getDefault();
		try {
			ItemStack stack = serializer.deserializeItemStack(in);
			return stack;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new ItemStack(Material.AIR);
	}

	private int checkPermission(HumanEntity player) {
		if (player.hasPermission("enderchest.ssvip")) {
			return this.plugin.getConfig().getInt("permission.ssvip");
		} else if (player.hasPermission("enderchest.svip")) {
			return this.plugin.getConfig().getInt("permission.svip");
		} else if (player.hasPermission("enderchest.vip")) {
			return this.plugin.getConfig().getInt("permission.vip");
		} else {
			return this.plugin.getConfig().getInt("permission.default");
		}
	}

	public Events(Plugin plugin) {
		this.plugin = plugin;
	}

}
