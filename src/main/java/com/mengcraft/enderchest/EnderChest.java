package com.mengcraft.enderchest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.utility.StreamSerializer;
import com.mengcraft.db.MengBuilder;
import com.mengcraft.db.MengDB;
import com.mengcraft.db.MengManager;
import com.mengcraft.db.MengRecord;
import com.mengcraft.db.MengTable;

public class EnderChest extends JavaPlugin {

	private final Map<String, Inventory> inventories = DataManager.getManager().getInventories();

	@Override
	public void onLoad() {
		saveDefaultConfig();
	}

	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(new Events(this), this);
		getCommand("chest").setExecutor(new Commands());
		String[] messages = {
				ChatColor.GREEN + "梦梦家服务器出租|我的世界|淘宝店",
				ChatColor.GREEN + "http://shop105595113.taobao.com/"
		};
		getServer().getConsoleSender().sendMessage(messages);
	}

	@Override
	public void onDisable() {
		MengManager db = MengDB.getManager();
		MengTable table = db.getTable("enderchest");
		for (Entry<String, Inventory> entry : this.inventories.entrySet()) {
			MengRecord record = table.findOne("name", entry.getKey());
			if (record == null) {
				record = new MengBuilder().getEmptyRecord();
				record.put("name", entry.getKey());
			}
			List<String> items = fromStacks(entry.getValue().getContents());
			record.put("items", items);
			table.update(record);
		}
		db.saveTable("enderchest");
	}

	private List<String> fromStacks(ItemStack[] contents) {

		List<String> list = new ArrayList<>();
		for (ItemStack item : contents) {
			list.add(fromStack(item));
		}
		return list;
	}

	private String fromStack(ItemStack item) {
		StreamSerializer serializer = StreamSerializer.getDefault();
		try {
			String out = serializer.serializeItemStack(item);
			return out;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

}
