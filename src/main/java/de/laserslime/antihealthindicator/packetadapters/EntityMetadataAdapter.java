package de.laserslime.antihealthindicator.packetadapters;

import java.util.Iterator;
import java.util.List;

import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wither;
import org.bukkit.entity.Wolf;
import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
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
		if(entity == null)
			return; // Return if entity can't be found (NPC plugins or similar might do this)

		PacketContainer packet = event.getPacket().shallowClone();
		if(event.getPacketType() != PacketType.Play.Server.ENTITY_METADATA) {
			StructureModifier<WrappedDataWatcher> watcherModifier = packet.getDataWatcherModifier();
			WrappedDataWatcher watcher = watcherModifier.readSafely(0);
			if(watcher != null)
				watcherModifier.writeSafely(0, new WrappedDataWatcher(filter(entity, event.getPlayer(), watcher.getWatchableObjects())));
		}
		
		StructureModifier<List<WrappedWatchableObject>> watchableCollectionModifier = packet.getWatchableCollectionModifier();
		List<WrappedWatchableObject> watchersold = watchableCollectionModifier.readSafely(0);
		if(watchersold != null)
			watchableCollectionModifier.writeSafely(0, filter(entity, event.getPlayer(), watchersold));

		event.setPacket(packet);
	}

	private List<WrappedWatchableObject> filter(Entity entity, Player receiver, List<WrappedWatchableObject> data) {
		Iterator<WrappedWatchableObject> iterator = data.iterator();
		while(iterator.hasNext()) {
			WrappedWatchableObject current = iterator.next();

			if(EntityDataIndex.HEALTH.match(entity.getClass(), current.getIndex())) {
				if(!plugin.getConfig().getBoolean("filters.entitydata.health.enabled", true)
						|| (receiver.getVehicle() == entity && plugin.getConfig().getBoolean("filters.entitydata.health.ignore-vehicles", true)) || (float) current.getValue() <= 0f)
					continue;

				if(entity instanceof Wolf && plugin.getConfig().getBoolean("filters.entitydata.health.ignore-tamed-dogs", true)) {
					Wolf wolf = (Wolf) entity;
					if(!wolf.isTamed())
						iterator.remove();
				} else if(!(entity instanceof EnderDragon && plugin.getConfig().getBoolean("filters.entitydata.health.ignore-enderdragon", true))
						&& !(entity instanceof Wither && plugin.getConfig().getBoolean("filters.entitydata.health.ignore-wither", true)))
					iterator.remove();
			} else if(EntityDataIndex.ABSORPTION.match(entity.getClass(), current.getIndex())) {
				if(!receiver.equals(entity))
					iterator.remove();
			} else if(EntityDataIndex.AIR_TICKS.match(entity.getClass(), current.getIndex())) {
				if(plugin.getConfig().getBoolean("filters.entitydata.airticks.enabled", false))
					iterator.remove();
			} else if(EntityDataIndex.XP.match(entity.getClass(), current.getIndex())) {
				if(plugin.getConfig().getBoolean("filters.entitydata.xp.enabled", true))
					iterator.remove();
			}
		}
		return data;
	}
}
