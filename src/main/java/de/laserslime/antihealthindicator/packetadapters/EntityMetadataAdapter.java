package de.laserslime.antihealthindicator.packetadapters;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;

import de.laserslime.antihealthindicator.data.EntityDataIndex;

public class EntityMetadataAdapter extends PacketAdapter {

	public EntityMetadataAdapter(Plugin plugin) {
		super(plugin, PacketType.Play.Server.ENTITY_METADATA, PacketType.Play.Server.SPAWN_ENTITY_LIVING, PacketType.Play.Server.NAMED_ENTITY_SPAWN);
	}

	@Override
	public void onPacketSending(PacketEvent event) {
		Entity entity = event.getPacket().getEntityModifier(event).readSafely(0);
		if(event.getPacketType() != PacketType.Play.Server.ENTITY_METADATA) {
			StructureModifier<WrappedDataWatcher> watcherModifier = event.getPacket().getDataWatcherModifier();
			WrappedDataWatcher watcher = watcherModifier.readSafely(0);
			if(watcher != null)
				watcherModifier.writeSafely(0, new WrappedDataWatcher(filter(entity, event.getPlayer(), watcher.getWatchableObjects())));
		}
		StructureModifier<List<WrappedWatchableObject>> watchableCollectionModifier = event.getPacket().getWatchableCollectionModifier();
		List<WrappedWatchableObject> watchersold = watchableCollectionModifier.readSafely(0);
		if(watchersold != null)
			watchableCollectionModifier.writeSafely(0, filter(entity, event.getPlayer(), watchersold));
	}

	private List<WrappedWatchableObject> filter(Entity entity, Player receiver, List<WrappedWatchableObject> olddata) {
		List<WrappedWatchableObject> newdata = new LinkedList<>(olddata); // Create a copy to prevent ConcurrentModificationException
		for(WrappedWatchableObject current : olddata) {
			if(EntityDataIndex.HEALTH.match(entity.getClass(), current.getIndex()) || EntityDataIndex.ABSORPTION.match(entity.getClass(), current.getIndex())) {
				if(!plugin.getConfig().getBoolean("filters.entitydata.health.enabled", true) || receiver.equals(entity) || receiver.getVehicle() == entity || (float) current.getValue() <= 0f)
					continue;
				if(entity instanceof Wolf) {
					Wolf wolf = (Wolf) entity;
					if(!wolf.isTamed())
						newdata.remove(current);
				} else
					newdata.remove(current);
			} else if(EntityDataIndex.AIR_TICKS.match(entity.getClass(), current.getIndex())) {
				if(plugin.getConfig().getBoolean("filters.entitydata.airticks.enabled", false))
					newdata.remove(current);
			} else if(EntityDataIndex.XP.match(entity.getClass(), current.getIndex())) {
				if(plugin.getConfig().getBoolean("filters.entitydata.xp.enabled", true))
					newdata.remove(current);
			}
		}
		return newdata;
	}
}
