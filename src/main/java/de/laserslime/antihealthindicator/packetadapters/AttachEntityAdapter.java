package de.laserslime.antihealthindicator.packetadapters;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;

import de.laserslime.antihealthindicator.entitydata.EntityDataIndexes;

public class AttachEntityAdapter extends PacketAdapter {

	public AttachEntityAdapter(Plugin plugin) {
		super(plugin, PacketType.Play.Server.ATTACH_ENTITY);
	}

	@Override
	public void onPacketSending(PacketEvent event) {
		// value of 1 indicates leashing while 0 indicates passenger change
		if(event.getPacket().getIntegers().readSafely(0) != 0)
			return;
		StructureModifier<Entity> entityModifier = event.getPacket().getEntityModifier(event);
		Entity passenger = entityModifier.readSafely(1);
		if(passenger instanceof Player) {
			Entity vehicle = entityModifier.readSafely(2);
			if(vehicle instanceof LivingEntity) {
				LivingEntity livingVehicle = (LivingEntity) vehicle;
				PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
				packet.getEntityModifier(event).writeSafely(0, vehicle);
				List<WrappedWatchableObject> watchers = new LinkedList<>();
				watchers.add(new WrappedWatchableObject(EntityDataIndexes.HEALTH, (float) livingVehicle.getHealth()));
				packet.getWatchableCollectionModifier().writeSafely(0, watchers);
				ProtocolLibrary.getProtocolManager().sendServerPacket((Player) passenger, packet);
			}
		}
	}
}
