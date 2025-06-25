package me.lasersli.antihealthindicator.packetadapters;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.GameRule;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers.ClientCommand;

public class AntiAutoRespawnAdapter extends PacketAdapter implements Listener {

	private Map<UUID, Long> deathTimes = new HashMap<>();

	public AntiAutoRespawnAdapter(Plugin plugin) {
		super(plugin, ListenerPriority.NORMAL, PacketType.Play.Client.CLIENT_COMMAND);
	}

	@Override
	public void onPacketReceiving(PacketEvent event) {
		if(event.getPlayer().getWorld().getGameRuleValue(GameRule.DO_IMMEDIATE_RESPAWN))
			return;
		ClientCommand cmd = event.getPacket().getClientCommands().readSafely(0);
		if(cmd.equals(ClientCommand.PERFORM_RESPAWN) && System.currentTimeMillis() - deathTimes.getOrDefault(event.getPlayer().getUniqueId(), 0L) < 1000)
			event.setCancelled(true);
	}

	@EventHandler
	public void handlePlayerDeath(PlayerDeathEvent e) {
		deathTimes.put(e.getEntity().getUniqueId(), System.currentTimeMillis());
	}

	@EventHandler
	public void handlePlayerQuit(PlayerQuitEvent e) {
		deathTimes.remove(e.getPlayer().getUniqueId());
	}
}
