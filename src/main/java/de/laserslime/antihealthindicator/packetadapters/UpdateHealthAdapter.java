package de.laserslime.antihealthindicator.packetadapters;

import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;

public class UpdateHealthAdapter extends PacketAdapter {

	public UpdateHealthAdapter(Plugin plugin) {
		super(plugin, PacketType.Play.Server.UPDATE_HEALTH);
	}

	@Override
	public void onPacketSending(PacketEvent event) {
		float saturation = event.getPacket().getFloat().readSafely(1);
		// If saturation is 0 the hunger bars will start shaking. To keep this vanilla behaviour we don't
		// rewrite it if it's 0
		if(saturation > 0) event.getPacket().getFloat().writeSafely(1, Float.NaN); // A value of nan will trick some indicators into not displaying
																					// anything
	}
}
