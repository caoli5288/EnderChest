package com.mengcraft.enderchest;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@AllArgsConstructor
@Data
@RequiredArgsConstructor
public class Pair<K, V> {

    private final K key;
    private V value;
}
