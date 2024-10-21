package de.laserslime.antihealthindicator.main;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wither;
import org.bukkit.entity.Wolf;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.ProtocolLibrary;

import de.laserslime.antihealthindicator.entitydata.EntityDataFilter;
import de.laserslime.antihealthindicator.entitydata.EntityDataIndexes;
import de.laserslime.antihealthindicator.packetadapters.AttachEntityAdapter;
import de.laserslime.antihealthindicator.packetadapters.EntityMetadataAdapter;
import de.laserslime.antihealthindicator.packetadapters.EntityMetadataAdapterAditional;
import de.laserslime.antihealthindicator.packetadapters.MountAdapter;
import de.laserslime.antihealthindicator.packetadapters.UpdateHealthAdapter;
import de.laserslime.antihealthindicator.packetadapters.WindowDataAdapter;
import de.laserslime.antihealthindicator.packetadapters.WorldSeedAdapter;
import de.laserslime.antihealthindicator.util.Version;

public class Main extends JavaPlugin {

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
			Map<Integer, EntityDataFilter> filters = new HashMap<>(4);
			if(getConfig().getBoolean("filters.entitydata.airticks.enabled", false))
				filters.put(EntityDataIndexes.AIR_TICKS, (entity, receiver, data) -> null);

			if(getConfig().getBoolean("filters.entitydata.health.enabled", true)) {
				filters.put(EntityDataIndexes.HEALTH, (entity, receiver, data) -> {
					if(!(entity instanceof LivingEntity) || (receiver.getVehicle() == entity && getConfig().getBoolean("filters.entitydata.health.ignore-vehicles", true)) || (float) data <= 0f)
						return data;

					if(entity instanceof Wolf && getConfig().getBoolean("filters.entitydata.health.ignore-tamed-dogs", true)) {
						Wolf wolf = (Wolf) entity;
						if(!wolf.isTamed())
							return null;
					} else if(!(entity instanceof EnderDragon && getConfig().getBoolean("filters.entitydata.health.ignore-enderdragon", true))
							&& !(entity instanceof Wither && getConfig().getBoolean("filters.entitydata.health.ignore-wither", true)))
						return null;
					return data;
				});
			}

			if(getConfig().getBoolean("filters.entitydata.health.enabled", true)) {
				filters.put(EntityDataIndexes.ABSORPTION, (entity, receiver, data) -> {
					if(entity instanceof Player && !receiver.equals(entity))
						return null;
					return data;
				});
			}

			if(getConfig().getBoolean("filters.entitydata.xp.enabled", true)) {
				filters.put(EntityDataIndexes.XP, (entity, receiver, data) -> {
					if(entity instanceof Player)
						return null;
					return data;
				});
			}

			ProtocolLibrary.getProtocolManager().addPacketListener(new EntityMetadataAdapter(this, filters));
			if(version.isBefore(Version.V1_15_0))
				ProtocolLibrary.getProtocolManager().addPacketListener(new EntityMetadataAdapterAditional(this, filters));
		}

		if(getConfig().getBoolean("filters.saturation.enabled", true))
			ProtocolLibrary.getProtocolManager().addPacketListener(new UpdateHealthAdapter(this));

		if(getConfig().getBoolean("filters.enchantseed.enabled", true))
			ProtocolLibrary.getProtocolManager().addPacketListener(new WindowDataAdapter(this));

		if(getConfig().getBoolean("filters.worldseed.enabled", false))
			ProtocolLibrary.getProtocolManager().addPacketListener(new WorldSeedAdapter(this));

		// The mount fix ensures that players can see the health of their vehicle, without it having to take damage first to update the health bar.
		// This is achieved with an extra packet sent when a player gets on a vehicle.
		// This only applies if health filtering and ignoring vehicles is enabled.
		if(getConfig().getBoolean("filters.entitydata.health.enabled", true) && getConfig().getBoolean("filters.entitydata.health.ignore-vehicles", true)) {
			// 1.8 uses a different packet for mounting entities
			if(version.isAfter(Version.V1_8_4))
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
