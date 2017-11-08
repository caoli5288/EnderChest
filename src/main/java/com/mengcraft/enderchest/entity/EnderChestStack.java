package com.mengcraft.enderchest.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "enderchest_stack")
@Data
@EqualsAndHashCode(of = "id")
public class EnderChestStack {

    @Id
    private int id;

    @ManyToOne
    private EnderChest chest;

    @Column(columnDefinition = "text")
    private String stack;
}
