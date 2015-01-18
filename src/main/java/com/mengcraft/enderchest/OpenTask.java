package com.mengcraft.enderchest;

import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Inventory;

public class OpenTask implements Runnable {

	private final Inventory inventory;
	private final HumanEntity entity;

	public OpenTask(HumanEntity player, Inventory inventory) {
		this.entity = player;
		this.inventory = inventory;
	}

	@Override
	public void run() {
		this.entity.closeInventory();
		this.entity.openInventory(this.inventory);
	}

}
