package com.mengcraft.enderchest;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import com.avaje.ebean.annotation.UpdatedTimestamp;

@Entity
public class EnderChest {

	@Id
	private int id;

	@Column(unique = true)
	private String player;

	@Column(columnDefinition = "text")
	private String chest;

	@UpdatedTimestamp
	private Timestamp timeUpdate;

	public Timestamp getTimeUpdate() {
		return timeUpdate;
	}

	public void setTimeUpdate(Timestamp timeUpdate) {
		this.timeUpdate = timeUpdate;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getPlayer() {
		return player;
	}

	public void setPlayer(String player) {
		this.player = player;
	}

	public String getChest() {
		return chest;
	}

	public void setChest(String chest) {
		this.chest = chest;
	}

}
