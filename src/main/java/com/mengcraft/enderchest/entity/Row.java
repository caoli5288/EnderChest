package com.mengcraft.enderchest.entity;

import com.mengcraft.enderchest.Main;
import lombok.Data;
import lombok.val;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.json.simple.JSONValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

@Data
public class Row {

    private final List<ItemStack> contend = new ArrayList<>(ROW_SIZE);

    public List<String> toRawList() {
        List<String> out = new ArrayList<>(contend.size());
        contend.forEach(i -> out.add(Main.encode(i)));
        return out;
    }

    public static List<Row> build(String data) {
        val itr = ((List<String>) JSONValue.parse(data)).iterator();
        val out = new ArrayList<Row>();
        while (itr.hasNext()) {
            out.add(build(itr));
        }
        return out;
    }

    public static List<Row> build(Inventory input) {
        ItemStack[] list = input.getContents();
        Iterator<ItemStack> itr = Arrays.asList(list).iterator();
        val out = new ArrayList<Row>();
        while (itr.hasNext()) {
            out.add(build(itr));
        }
        return out;
    }

    private static Row build(Iterator itr) {
        Row out = new Row();
        while (!(out.contend.size() == ROW_SIZE) && itr.hasNext()) {
            Object next = itr.next();
            if (next instanceof String) {
                out.contend.add(Main.decode(((String) next)));
            } else {
                out.contend.add(((ItemStack) next));
            }
        }
        return out;
    }

    private static final int ROW_SIZE = 9;
}
