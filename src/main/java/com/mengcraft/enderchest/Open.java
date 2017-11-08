package com.mengcraft.enderchest;

import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.mengcraft.enderchest.Main.nil;

public class Open implements Runnable {

    public static final Map<UUID, Pair<Integer, Inventory>> OPEN_INVENTORY = new HashMap<>();

    private final HumanEntity player;
    private final Holder hold;
    private final int offset;

    public Open(Map<String, Holder> cache, HumanEntity player, int offset) {
        this.hold = cache.get(player.getName());
        this.player = player;
        this.offset = offset;
    }

    @Override
    public void run() {
        if (!(hold.getEntity() == null)) {
            validPage(hold.getPage() + offset);
            open();
        }
    }

    private void validPage(int i) {
        boolean b = hold.hasPage(i);
        if (b) {
            player.closeInventory();
            // Close opened inventory before change page.
            hold.setPage(i);
        } else {
            player.closeInventory();
            hold.setPage(0);
        }
    }

    private void open() {
        Inventory open = hold.getInventory();
        Pair<Integer, Inventory> removal = OPEN_INVENTORY.put(player.getUniqueId(), new Pair<Integer, Inventory>(hold.getPage(), open));
        if (!nil(removal)) {
            hold.close(removal);
        }
        player.openInventory(open);
    }

}
