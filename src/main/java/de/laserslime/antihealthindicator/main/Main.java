package de.laserslime.antihealthindicator.main;

import java.util.Arrays;
import java.util.List;

import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;

import de.laserslime.antihealthindicator.packetadapters.AttachEntityAdapter;
import de.laserslime.antihealthindicator.packetadapters.EntityMetadataAdapter;
import de.laserslime.antihealthindicator.packetadapters.MountAdapter;
import de.laserslime.antihealthindicator.packetadapters.UpdateHealthAdapter;
import de.laserslime.antihealthindicator.packetadapters.WindowDataAdapter;
import de.laserslime.antihealthindicator.packetadapters.WorldSeedAdapter;
import de.laserslime.antihealthindicator.util.Version;

public class Main extends JavaPlugin {

	@SuppressWarnings("deprecation")
	@Override
	public void onEnable() {
		saveDefaultConfig();
		Version version = Version.getServerVersion();
		if(version == Version.UNKNOWN && !getConfig().getBoolean("allow-unsupported-versions", false)) {
			getLogger().warning("Unsupported server version detected! Plugin will be disabled to prevent unexpected issues. Please check if theres an update available that supports this version.");
			getLogger().info("You can allow unsupported versions in the config.yml AT YOUR OWN RISK.");
			getPluginLoader().disablePlugin(this);
			return;
		}

		if(getConfig().getBoolean("filters.entitydata.enabled", true)) {
			List<PacketType> types = Arrays.asList(PacketType.Play.Server.ENTITY_METADATA);
			if(version.getProtocolVersion() < Version.V1_15_0.getProtocolVersion()) {
				types.add(PacketType.Play.Server.SPAWN_ENTITY_LIVING);
				types.add(PacketType.Play.Server.NAMED_ENTITY_SPAWN);
			}
			ProtocolLibrary.getProtocolManager().addPacketListener(new EntityMetadataAdapter(this, types));
		}

		if(getConfig().getBoolean("filters.saturation.enabled", true))
			ProtocolLibrary.getProtocolManager().addPacketListener(new UpdateHealthAdapter(this));

		if(getConfig().getBoolean("filters.enchantseed.enabled", true))
			ProtocolLibrary.getProtocolManager().addPacketListener(new WindowDataAdapter(this));

		if(getConfig().getBoolean("filters.worldseed.enabled", false))
			ProtocolLibrary.getProtocolManager().addPacketListener(new WorldSeedAdapter(this));

		// Only apply mount fix if health filtering is enabled and vehicles are ignored
		if(getConfig().getBoolean("filters.entitydata.health.enabled", true) && getConfig().getBoolean("filters.entitydata.health.ignore-vehicles", true)) {
			// 1.8 uses a different packet for mounting entities
			if(version.getProtocolVersion() > Version.V1_8_4.getProtocolVersion())
				ProtocolLibrary.getProtocolManager().addPacketListener(new MountAdapter(this));
			else
				ProtocolLibrary.getProtocolManager().addPacketListener(new AttachEntityAdapter(this));
		}
	}

	@Override
	public void onDisable() {
		ProtocolLibrary.getProtocolManager().removePacketListeners(this);
	}
}
