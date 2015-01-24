package com.mengcraft.enderchest;

import java.util.Map;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class Commands implements CommandExecutor {

	private final Map<String, Inventory> map = DataManager.getManager().getInventories();

	@Override
	public boolean onCommand(CommandSender sender, Command arg1, String arg2, String[] args) {
		if (sender instanceof ConsoleCommandSender) {
			return false;
		} else if (args.length < 1) {
			return false;
		} else if (args.length < 2) {
			getWithName((Player) sender, args[0]);
		}
		return false;
	}

	private void getWithName(Player sender, String name) {
		Inventory inventory = this.map.get(name);
		if (inventory != null) {
			sender.openInventory(inventory);
		} else {
			sender.sendMessage("玩家不存在");
		}
	}

}
