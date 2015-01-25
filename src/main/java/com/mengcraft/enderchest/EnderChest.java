package com.mengcraft.enderchest;

import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public class EnderChest extends JavaPlugin {

	@Override
	public void onLoad() {
		saveDefaultConfig();
	}

	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(new Events(this), this);
		getServer().getScheduler().runTaskTimer(this, new SaveTask(), 6000, 6000);
		getCommand("chest").setExecutor(new Commands());
		String[] messages = {
				ChatColor.GREEN + "梦梦家服务器出租|我的世界|淘宝店",
				ChatColor.GREEN + "http://shop105595113.taobao.com/"
		};
		getServer().getConsoleSender().sendMessage(messages);
	}

	@Override
	public void onDisable() {
		new SaveTask().run();
	}

}
