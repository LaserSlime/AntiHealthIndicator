package de.laserslime.antihealthindicator.packetadapters;

import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;

public class WorldSeedAdapter extends PacketAdapter {

	public WorldSeedAdapter(Plugin plugin) {
		super(plugin, PacketType.Play.Server.RESPAWN, PacketType.Play.Server.LOGIN);
	}

	@Override
	public void onPacketSending(PacketEvent event) {
		event.getPacket().getLongs().writeSafely(0, 0l);
	}
}
