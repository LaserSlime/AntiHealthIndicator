package de.laserslime.antihealthindicator.packetadapters;

import java.lang.reflect.InvocationTargetException;
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
import com.comphenix.protocol.wrappers.BukkitConverters;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;

import de.laserslime.antihealthindicator.data.EntityDataIndex;

public class MountAdapter extends PacketAdapter {

	public MountAdapter(Plugin plugin) {
		super(plugin, PacketType.Play.Server.MOUNT);
	}

	@Override
	public void onPacketSending(PacketEvent event) {
		int id = event.getPacket().getIntegerArrays().read(0)[0]; //TODO change to read safely after ensuring cross compatibility
		Entity passenger = ProtocolLibrary.getProtocolManager().getEntityFromID(event.getPlayer().getWorld(), id);
		if(passenger instanceof Player) {
			Entity vehicle = event.getPacket().getEntityModifier(event).read(0);
			if(vehicle instanceof LivingEntity) {
				LivingEntity livingVehicle = (LivingEntity) vehicle;
				PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
				packet.getEntityModifier(event).write(0, passenger);
				List<WrappedWatchableObject> watchers = new LinkedList<>();
				watchers.add(new WrappedWatchableObject(EntityDataIndex.HEALTH.getIndex(), livingVehicle.getHealth()));
				packet.getLists(BukkitConverters.getWatchableObjectConverter()).write(0, watchers);
				try {
					ProtocolLibrary.getProtocolManager().sendServerPacket((Player) passenger, packet);
				} catch(InvocationTargetException ex) {
					ex.printStackTrace();
				}
			}
		}
	}

}
