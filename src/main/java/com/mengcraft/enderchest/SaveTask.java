package com.mengcraft.enderchest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.comphenix.protocol.utility.StreamSerializer;
import com.mengcraft.db.MengBuilder;
import com.mengcraft.db.MengDB;
import com.mengcraft.db.MengManager;
import com.mengcraft.db.MengRecord;
import com.mengcraft.db.MengTable;

public class SaveTask implements Runnable {
	
	private final Map<String, Inventory> map = DataManager.getManager().getInventories();

	@Override
	public void run() {
		MengManager db = MengDB.getManager();
		MengTable table = db.getTable("enderchest");
		for (Entry<String, Inventory> entry : this.map.entrySet()) {
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
