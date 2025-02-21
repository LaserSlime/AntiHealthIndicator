package me.lasersli.antihealthindicator.packetadapters;

import org.bukkit.event.inventory.InventoryType;
import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;

public class WindowDataAdapter extends PacketAdapter {

	public WindowDataAdapter(Plugin plugin) {
		super(plugin, PacketType.Play.Server.WINDOW_DATA);
	}

	@Override
	public void onPacketSending(PacketEvent event) {
		int action = event.getPacket().getIntegers().readSafely(0);
		// Action id 3 is the enchanting seed. Seems to be the same across all versions
		if(event.getPlayer().getOpenInventory().getType().equals(InventoryType.ENCHANTING) && action == 3)
			event.setCancelled(true);
	}
}
