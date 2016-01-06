package com.mengcraft.enderchest;

import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import com.mengcraft.simpleorm.EbeanHandler;
import com.mengcraft.simpleorm.EbeanManager;

public class Main extends JavaPlugin {

	@Override
	public void onEnable() {
		getConfig().options().copyDefaults(true);
		saveConfig();

		ItemUtil util = new ItemUtilHandler(this).handle();

		EbeanManager manager = getServer().getServicesManager()
		        .getRegistration(EbeanManager.class)
		        .getProvider();
		EbeanHandler handler = manager.getHandler(this);

		if (!handler.isInitialized()) {
			handler.define(EnderChest.class);
			try {
				handler.initialize();
			} catch (Exception e) {
				throw new RuntimeException(e.getMessage());
			}
		}

		handler.install();
		handler.reflect();
		
		String[] strings = {
                ChatColor.GREEN + "梦梦家高性能服务器出租店",
                ChatColor.GREEN + "shop105595113.taobao.com"
        };
        getServer().getConsoleSender().sendMessage(strings);
		
		new Executor().bind(this, util);
	}

	public void execute(Runnable runnable) {
		getServer().getScheduler().runTaskAsynchronously(this, runnable);
	}

}
