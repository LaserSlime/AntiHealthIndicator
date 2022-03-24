package de.laserslime.antihealthindicator.packetadapters;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.BukkitConverters;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;

import de.laserslime.antihealthindicator.data.EntityDataIndex;

public class EntityMetadataAdapter extends PacketAdapter {

	public EntityMetadataAdapter(Plugin plugin) {
		super(plugin, PacketType.Play.Server.ENTITY_METADATA);
	}

	@Override
	public void onPacketSending(PacketEvent event) {
		Entity entity = event.getPacket().getEntityModifier(event).readSafely(0);
		StructureModifier<List<WrappedWatchableObject>> listModifier = event.getPacket().getLists(BukkitConverters.getWatchableObjectConverter());
		List<WrappedWatchableObject> watchersold = listModifier.readSafely(0);
		List<WrappedWatchableObject> watchersnew = new LinkedList<>(watchersold); // Create a copy to prevent ConcurrentModificationException
		for(WrappedWatchableObject current : watchersold) {
			if(plugin.getConfig().getBoolean("filters.entitydata.health.enabled")
					&& (EntityDataIndex.HEALTH.match(entity.getClass(), current.getIndex()) || EntityDataIndex.ABSORPTION.match(entity.getClass(), current.getIndex()))
					&& !entity.equals(event.getPlayer()) && (float) current.getValue() > 0f) // Only filter if health is greater than 0 to keep the player death animation
				watchersnew.remove(current);

			if(plugin.getConfig().getBoolean("filters.entitydata.airticks.enabled") && EntityDataIndex.AIR_TICKS.match(entity.getClass(), current.getIndex()))
				watchersnew.remove(current);

			if(plugin.getConfig().getBoolean("filters.entitydata.xp.enabled") && EntityDataIndex.XP.match(entity.getClass(), current.getIndex()))
				watchersnew.remove(current);
			
			if(current.getIndex() == 1)
				System.out.println(current.getValue());
		}
		listModifier.writeSafely(0, watchersnew);
	}
}
