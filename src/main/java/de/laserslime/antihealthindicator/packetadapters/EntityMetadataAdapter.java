package de.laserslime.antihealthindicator.packetadapters;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
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
			WrappedDataWatcher watcher = event.getPacket().getDataWatcherModifier().readSafely(0);
			if(watcher != null) {
				List<WrappedWatchableObject> data = watcher.deepClone().getWatchableObjects();
				event.getPacket().getDataWatcherModifier().write(0, new WrappedDataWatcher(filter(entity, event.getPlayer(), data)));
			}
		}
		StructureModifier<List<WrappedWatchableObject>> listModifier = event.getPacket().getWatchableCollectionModifier();
		List<WrappedWatchableObject> watchersold = listModifier.readSafely(0);
		if(watchersold != null)
			listModifier.writeSafely(0, filter(entity, event.getPlayer(), watchersold));
	}

	private List<WrappedWatchableObject> filter(Entity entity, Player receiver, List<WrappedWatchableObject> olddata) {
		List<WrappedWatchableObject> newdata = new LinkedList<>(olddata); // Create a copy to prevent ConcurrentModificationException
		for(WrappedWatchableObject current : olddata) {
			if(plugin.getConfig().getBoolean("filters.entitydata.health.enabled", true)
					&& (EntityDataIndex.HEALTH.match(entity.getClass(), current.getIndex()) || EntityDataIndex.ABSORPTION.match(entity.getClass(), current.getIndex()))
					&& !entity.equals(receiver) && receiver.getVehicle() != entity && (float) current.getValue() > 0f) // Only filter if health is greater than 0 to keep the player
																																			// death animation
				newdata.remove(current);

			if(plugin.getConfig().getBoolean("filters.entitydata.airticks.enabled", false) && EntityDataIndex.AIR_TICKS.match(entity.getClass(), current.getIndex()))
				newdata.remove(current);

			if(plugin.getConfig().getBoolean("filters.entitydata.xp.enabled", true) && EntityDataIndex.XP.match(entity.getClass(), current.getIndex()))
				newdata.remove(current);
		}
		return newdata;
	}
}
