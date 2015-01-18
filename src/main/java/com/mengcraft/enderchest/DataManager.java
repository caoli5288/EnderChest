package com.mengcraft.enderchest;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.inventory.Inventory;

public class DataManager {
	private final static DataManager MANAGER = new DataManager();

	private final Map<String, Inventory> inventories;

	private DataManager() {
		this.inventories = new HashMap<>();
	}

	public static DataManager getManager() {
		return MANAGER;
	}

	public Map<String, Inventory> getInventories() {
		return inventories;
	}

}
