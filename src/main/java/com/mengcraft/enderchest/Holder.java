package com.mengcraft.enderchest;

import com.google.common.collect.ImmutableList;
import com.mengcraft.enderchest.entity.EnderChest;
import com.mengcraft.enderchest.entity.EnderChestStack;
import com.mengcraft.enderchest.entity.Row;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.experimental.var;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.ArrayList;
import java.util.List;

import static com.mengcraft.enderchest.Main.nil;

@Data
public class Holder implements InventoryHolder {

    private EnderChest entity;
    private Player player;

    private String title;

    private int page;
    private int maxRow;

    @Override
    public Inventory getInventory() {
        Inventory inv = createInventory();
        List<Row> list = pickRow();
        int i = -1;
        for (Row row : list) {
            val l = row.getContend().iterator();
            while (l.hasNext()) {
                inv.setItem(++i, l.next());
            }
        }
        return inv;
    }

    public void close(Pair<Integer, Inventory> removal) {
        int idx = removal.getKey() * ROW_PER_PAGE;
        List<Row> input = Row.build(removal.getValue());
        int i = idx + input.size();

        List<Row> nList = new ArrayList<>();
        List<Row> all = entity.getAllRow();
        if (all.size() >= idx) {
            nList.addAll(all.subList(0, idx));
        }
        nList.addAll(input);
        if (all.size() >= i) {
            nList.addAll(all.subList(i, all.size()));
        }
        entity.setAllRow(nList);

        if (Main.flip(entity)) Main.runAsync(this::saveAll);
    }

    private List<Row> pickRow() {
        int idx = page * ROW_PER_PAGE;
        int i = idx + getPageRow();
        val all = entity.getAllRow();
        if (all.size() < idx) {
            return ImmutableList.of();
        }
        if (all.size() < i) {
            return all.subList(idx, all.size());
        }
        return all.subList(idx, i);
    }

    private Inventory createInventory() {
        return Bukkit.createInventory(this, getPageSlot(), getTitle());
    }

    private int getPageSlot() {
        return getPageRow() * 9;
    }

    private int getPageRow() {
        int i = maxRow - page * ROW_PER_PAGE;
        return i < ROW_PER_PAGE ? i : ROW_PER_PAGE;
    }

    public void saveAll() {
        Main.getDb().save(entity);
    }

    /**
     * Check if this holder's page cursor can be set to. Depend
     * {@code maxRow} field.
     *
     * @param i the page
     * @return {@code true} if can be
     */
    public boolean hasPage(int i) {
        return i > -1 && i * ROW_PER_PAGE < maxRow;
    }

    public int getPage() {
        return page;
    }

    /**
     * Please call {@link Holder#hasPage(int)} before. Set this holder's
     * current page cursor.
     *
     * @param page
     */
    public void setPage(int page) {
        this.page = page;
    }

    public int getMaxRow() {
        return maxRow;
    }

    public Holder setMaxRow(int maxRow) {
        this.maxRow = maxRow;
        return this;
    }

    public Holder addMaxRow(int maxRow) {
        this.maxRow += maxRow;
        return this;
    }

    public String getTitle() {
        return String.format(title, page + 1);
    }

    public Holder setTitle(String title) {
        this.title = title;
        return this;
    }

    public Player getPlayer() {
        return player;
    }

    public Holder setPlayer(Player player) {
        this.player = player;
        return this;
    }

    @SneakyThrows
    public void update() {
        var i = Main.getDb().find(EnderChest.class)
                .where("player = :name")
                .setParameter("name", player.getName())
                .findUnique();
        if (nil(i)) {
            i = Main.getDb().createEntityBean(EnderChest.class);
            i.setPlayer(player.getName());
            entity = i;
        } else {
            entity = i;
            if (nil(i.getContend())) {
                List<EnderChestStack> allPage = i.getAll();
                if (!(nil(allPage) || allPage.isEmpty())) {
                    ArrayList<Row> rowList = new ArrayList<>();
                    allPage.forEach(l -> rowList.addAll(Row.build(l.getStack())));
                    i.setAllRow(rowList);
                    Main.flip(i);
                    saveAll();
                }
            } else {
                Main.buildRow(i);
            }
            maxRow += i.getMaxRow();
        }
    }

    private static final int ROW_PER_PAGE = 6;
}
