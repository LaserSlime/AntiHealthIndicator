package de.laserslime.antihealthindicator.packetadapters;

import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.InternalStructure;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;

public class WorldSeedAdapter extends PacketAdapter {

	public WorldSeedAdapter(Plugin plugin) {
		super(plugin, PacketType.Play.Server.RESPAWN, PacketType.Play.Server.LOGIN);
	}

	@Override
	public void onPacketSending(PacketEvent event) {
		PacketContainer packet = event.getPacket();
		int structureSize = packet.getStructures().size();
		if(structureSize == 0) {
			plugin.getLogger().warning("Can not write hashed seed at respawn for player " + event.getPlayer().getName() + ".");
			return;
		}
		InternalStructure structureModifier = packet.getStructures().read(structureSize - 1);
		structureModifier.getLongs().write(0, randomizeHashedSeed(structureModifier.getLongs().read(0)));
	}

	public long randomizeHashedSeed(long hashedSeed) {
		int length = Long.toString(hashedSeed).length();
		if(length > 18)
			length = 18;
		long min = (long) Math.pow(10, length - 1);
		long max = (long) (Math.pow(10, length) - 1);
		return ThreadLocalRandom.current().nextLong(min, max + 1);
	}
}
