package de.laserslime.antihealthindicator.packetadapters;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;

import de.laserslime.antihealthindicator.entitydata.EntityDataFilter;

public class EntityMetadataAdapterAditional extends PacketAdapter {

	private Map<Integer, EntityDataFilter> filters;

	@SuppressWarnings("deprecation")
	public EntityMetadataAdapterAditional(Plugin plugin, Map<Integer, EntityDataFilter> filters) {
		super(plugin, PacketType.Play.Server.SPAWN_ENTITY_LIVING, PacketType.Play.Server.NAMED_ENTITY_SPAWN);
		this.filters = filters;
	}

	@Override
	public void onPacketSending(PacketEvent event) {
		Entity entity = event.getPacket().getEntityModifier(event).readSafely(0);
		if(entity == null)
			return; // If ProtocolLib can't find the entity, it's likely an npc or similar from a plugin

		PacketContainer packet = event.getPacket().shallowClone();
		StructureModifier<WrappedDataWatcher> watcherModifier = packet.getDataWatcherModifier();
		WrappedDataWatcher watcher = watcherModifier.readSafely(0);
		if(watcher != null) {
			List<WrappedWatchableObject> watchableObjects = new ArrayList<>(watcher.getWatchableObjects());
			for(WrappedWatchableObject current : watcher.getWatchableObjects()) {
				if(!filters.containsKey(current.getIndex()))
					continue;
				Object filteredValue = filters.get(current.getIndex()).filter(entity, event.getPlayer(), current.getValue());
				if(filteredValue == null)
					watchableObjects.remove(current);
				else
					current.setValue(filteredValue, true);
			}
			watcherModifier.writeSafely(0, new WrappedDataWatcher(watchableObjects));
		}
		
		StructureModifier<List<WrappedWatchableObject>> watchableObjectsModifier = packet.getWatchableCollectionModifier();
		List<WrappedWatchableObject> watchableObjects = watchableObjectsModifier.readSafely(0);
		if(watchableObjects != null) {
			Iterator<WrappedWatchableObject> iterator = watchableObjects.iterator();
			while(iterator.hasNext()) {
				WrappedWatchableObject current = iterator.next();
				if(!filters.containsKey(current.getIndex()))
					continue;
				Object filteredValue = filters.get(current.getIndex()).filter(entity, event.getPlayer(), current.getValue());
				if(filteredValue == null)
					iterator.remove();
				else
					current.setValue(filteredValue, true);
			}
			watchableObjectsModifier.writeSafely(0, watchableObjects);
		}
		
		event.setPacket(packet);
	}
}
