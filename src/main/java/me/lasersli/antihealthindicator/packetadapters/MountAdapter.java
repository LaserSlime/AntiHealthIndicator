package me.lasersli.antihealthindicator.packetadapters;

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
import com.comphenix.protocol.wrappers.WrappedDataValue;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.Registry;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.WrappedDataWatcherObject;

import me.lasersli.antihealthindicator.entitydata.EntityDataIndexes;
import me.lasersli.antihealthindicator.util.Version;

public class MountAdapter extends PacketAdapter {

	public MountAdapter(Plugin plugin) {
		super(plugin, PacketType.Play.Server.MOUNT);
	}

	@Override
	public void onPacketSending(PacketEvent event) {
		int[] ids = event.getPacket().getIntegerArrays().readSafely(0);
		// if the array is empty they are dismounting
		if(ids.length <= 0)
			return;
		Entity passenger = ProtocolLibrary.getProtocolManager().getEntityFromID(event.getPlayer().getWorld(), ids[0]);
		if(passenger instanceof Player && passenger.equals(event.getPlayer())) {
			StructureModifier<Entity> entityModifier = event.getPacket().getEntityModifier(event);
			Entity vehicle = entityModifier.readSafely(0);
			if(vehicle instanceof LivingEntity livingVehicle) {
				PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
				packet.getEntityModifier(event).writeSafely(0, livingVehicle);
				if(Version.getServerVersion().isBefore(Version.V1_19_3)) {
					WrappedDataWatcher watcher = new WrappedDataWatcher(livingVehicle);
					watcher.setObject(new WrappedDataWatcherObject(EntityDataIndexes.HEALTH, Registry.get(Float.class)), (float) livingVehicle.getHealth());
					packet.getWatchableCollectionModifier().writeSafely(0, watcher.getWatchableObjects());
				} else {
					List<WrappedDataValue> values = new LinkedList<>();
					values.add(new WrappedDataValue(EntityDataIndexes.HEALTH, Registry.get(Float.class), (float) livingVehicle.getHealth()));
					packet.getDataValueCollectionModifier().write(0, values);
				}
				ProtocolLibrary.getProtocolManager().sendServerPacket((Player) passenger, packet);
			}
		}
	}
}
