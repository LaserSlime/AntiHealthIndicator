package de.laserslime.antihealthindicator.packetadapters;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wither;
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

	public EntityMetadataAdapter(Plugin plugin, Iterable<PacketType> types) {
		super(plugin, types);
	}

	@Override
	public void onPacketSending(PacketEvent event) {
		Entity entity = event.getPacket().getEntityModifier(event).readSafely(0);
		if(entity == null) return; // Return if entity can't be found (NPC plugins or similar might do this)
		if(event.getPacketType() != PacketType.Play.Server.ENTITY_METADATA) {
			StructureModifier<WrappedDataWatcher> watcherModifier = event.getPacket().getDataWatcherModifier();
			WrappedDataWatcher watcher = watcherModifier.readSafely(0);
			if(watcher != null)
				watcherModifier.writeSafely(0, new WrappedDataWatcher(filter(entity, event.getPlayer(), watcher.getWatchableObjects())));
		}
		StructureModifier<List<WrappedWatchableObject>> watchableCollectionModifier = event.getPacket().getWatchableCollectionModifier();
		List<WrappedWatchableObject> watchersold = watchableCollectionModifier.readSafely(0);
		if(watchersold != null) watchableCollectionModifier.writeSafely(0, filter(entity, event.getPlayer(), watchersold));
	}

	private List<WrappedWatchableObject> filter(Entity entity, Player receiver, List<WrappedWatchableObject> olddata) {
		// Create a copy to prevent concurrency issues
		List<WrappedWatchableObject> newdata = new LinkedList<>(olddata);
		for(WrappedWatchableObject current : olddata) {
			if(EntityDataIndex.HEALTH.match(entity.getClass(), current.getIndex())
					|| EntityDataIndex.ABSORPTION.match(entity.getClass(), current.getIndex())) {
				if(!plugin.getConfig().getBoolean("filters.entitydata.health.enabled", true) || receiver.equals(entity)
						|| (receiver.getVehicle() == entity && plugin.getConfig().getBoolean("filters.entitydata.health.ignore-vehicles", true))
						|| (float) current.getValue() <= 0f)
					continue;
				if(entity instanceof Wolf && plugin.getConfig().getBoolean("filters.entitydata.health.ignore-tamed-dogs", true)) {
					Wolf wolf = (Wolf) entity;
					if(!wolf.isTamed()) newdata.remove(current);
				} else if(!(entity instanceof EnderDragon && plugin.getConfig().getBoolean("filters.entitydata.health.ignore-enderdragon", true))
						&& !(entity instanceof Wither && plugin.getConfig().getBoolean("filters.entitydata.health.ignore-wither", true)))
					newdata.remove(current);
			} else if(EntityDataIndex.AIR_TICKS.match(entity.getClass(), current.getIndex())) {
				if(plugin.getConfig().getBoolean("filters.entitydata.airticks.enabled", false)) newdata.remove(current);
			} else if(EntityDataIndex.XP.match(entity.getClass(), current.getIndex())) {
				if(plugin.getConfig().getBoolean("filters.entitydata.xp.enabled", true)) newdata.remove(current);
			}
		}
		return newdata;
	}
}
