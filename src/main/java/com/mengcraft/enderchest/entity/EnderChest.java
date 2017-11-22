package com.mengcraft.enderchest.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "enderchest")
@Data
@EqualsAndHashCode(of = "id")
public class EnderChest {

    @Id
    private int id;

    @Column(unique = true)
    private String player;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "chest")
    private List<EnderChestStack> all = new ArrayList<>();

    @Column(columnDefinition = "LONGTEXT")
    private String contend;

    private int maxRow;

    @Transient
    private List<Row> allRow = new ArrayList<>();
}
