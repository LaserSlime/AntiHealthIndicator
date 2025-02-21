package me.lasersli.antihealthindicator.entitydata;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public interface EntityDataFilter {

	public Object filter(Entity entity, Player receiver, Object data);
}
