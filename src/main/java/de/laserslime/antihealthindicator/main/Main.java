package de.laserslime.antihealthindicator.main;

import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.ProtocolLibrary;

import de.laserslime.antihealthindicator.packetadapters.EntityMetadataAdapter;
import de.laserslime.antihealthindicator.packetadapters.MountAdapter;
import de.laserslime.antihealthindicator.packetadapters.UpdateHealthAdapter;
import de.laserslime.antihealthindicator.packetadapters.WindowDataAdapter;
import de.laserslime.antihealthindicator.packetadapters.WorldSeedAdapter;
import de.laserslime.antihealthindicator.util.Version;

public class Main extends JavaPlugin {

	@Override
	public void onEnable() {
		saveDefaultConfig();
		if(Version.getServerVersion() == null && !getConfig().getBoolean("allow-unsupported-versions", false)) {
			getLogger().info("Unsupported server version detected! Plugin will be disabled to prevent unexpected issues. Please check if theres an update available that supports this version.");
			getLogger().info("You can allow unsupported versions in the config.yml AT YOUR OWN RISK.");
			getPluginLoader().disablePlugin(this);
			return;
		}

		if(getConfig().getBoolean("filters.entitydata.enabled", true))
			ProtocolLibrary.getProtocolManager().addPacketListener(new EntityMetadataAdapter(this));

		if(getConfig().getBoolean("filters.saturation.enabled", true))
			ProtocolLibrary.getProtocolManager().addPacketListener(new UpdateHealthAdapter(this));

		if(getConfig().getBoolean("filters.enchantseed.enabled", true))
			ProtocolLibrary.getProtocolManager().addPacketListener(new WindowDataAdapter(this));

		if(getConfig().getBoolean("filters.worldseed.enabled", false))
			ProtocolLibrary.getProtocolManager().addPacketListener(new WorldSeedAdapter(this));
		
		ProtocolLibrary.getProtocolManager().addPacketListener(new MountAdapter(this));
	}

	@Override
	public void onDisable() {
		ProtocolLibrary.getProtocolManager().removePacketListeners(this);
	}
}
