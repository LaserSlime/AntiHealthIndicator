package me.lasersli.antihealthindicator.packetadapters;

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
import com.comphenix.protocol.wrappers.WrappedDataValue;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;

import me.lasersli.antihealthindicator.entitydata.EntityDataFilter;
import me.lasersli.antihealthindicator.util.Version;

public class EntityMetadataAdapter extends PacketAdapter {

	private Map<Integer, EntityDataFilter> filters;

	public EntityMetadataAdapter(Plugin plugin, Map<Integer, EntityDataFilter> filters) {
		super(plugin, PacketType.Play.Server.ENTITY_METADATA);
		this.filters = filters;
	}

	@Override
	public void onPacketSending(PacketEvent event) {
		Entity entity = event.getPacket().getEntityModifier(event).readSafely(0);
		if(entity == null)
			return; // If ProtocolLib can't find the entity, it's likely an npc or similar from a plugin

		PacketContainer packet = event.getPacket().shallowClone();

		// I really don't understand why ProtocolLib made these classes incompatible with each other.
		// The only way to clean this up would be to create some sort of wrapper class for wrapper classes, which is just ridiculous...
		if(Version.getServerVersion().isBefore(Version.V1_19_3)) {
			StructureModifier<List<WrappedWatchableObject>> watchableCollectionModifier = packet.getWatchableCollectionModifier();
			List<WrappedWatchableObject> data = watchableCollectionModifier.readSafely(0);
			if(data == null)
				return;
			Iterator<WrappedWatchableObject> iterator = data.iterator();
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
			watchableCollectionModifier.writeSafely(0, data);
		} else {
			StructureModifier<List<WrappedDataValue>> dataValueCollectionModifier = packet.getDataValueCollectionModifier();
			List<WrappedDataValue> data = dataValueCollectionModifier.readSafely(0);
			if(data == null)
				return;
			Iterator<WrappedDataValue> iterator = data.iterator();
			while(iterator.hasNext()) {
				WrappedDataValue current = iterator.next();
				if(!filters.containsKey(current.getIndex()))
					continue;
				Object filteredValue = filters.get(current.getIndex()).filter(entity, event.getPlayer(), current.getValue());
				if(filteredValue == null)
					iterator.remove();
				else
					current.setValue(filteredValue);
			}
			dataValueCollectionModifier.writeSafely(0, data);
		}

		event.setPacket(packet);
	}
}
