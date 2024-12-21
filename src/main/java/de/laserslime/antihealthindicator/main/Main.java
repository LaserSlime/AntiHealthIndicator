package de.laserslime.antihealthindicator.main;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.IronGolem;
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
					if(!(entity instanceof LivingEntity) || (receiver.getVehicle() == entity && getConfig().getBoolean("filters.entitydata.health.ignore-vehicles", true)))
						return data;
					// Yes health is sent as a float, even tho it's stored as a double https://wiki.vg/Entity_metadata#Living_Entity
					float health = (float) data;
					if(health <= 0f)
						return data;

					if(entity instanceof Wolf wolf && wolf.isTamed() && getConfig().getBoolean("filters.entitydata.health.ignore-tamed-dogs", true))
						return data;

					if(version.isAtLeast(Version.V1_15_0) && entity instanceof IronGolem golem && getConfig().getBoolean("filters.entitydata.health.show-irongolem-cracks", true)) {
						double maxhealth = (float) golem.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
						double step = 25 / maxhealth * 100; // New cracks form for every 25% of health lost
						double roundedHealth = (float) mFloor(health, step); // Round down to closest step
						return (float) clamp(roundedHealth, step - 1, maxhealth - step); // Clamp to keep it above 0 and not above the health where the first crack spawns
					}

					if((entity instanceof EnderDragon && getConfig().getBoolean("filters.entitydata.health.ignore-enderdragon", true))
							|| (entity instanceof Wither && getConfig().getBoolean("filters.entitydata.health.ignore-wither", true)))
						return data;
					return null;
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

	private double mFloor(double value, double factor) {
		return Math.floor(value / factor) * factor;
	}

	// Copied from Java source (this was only added to java.util.Math class in Java 21 wtf????)
	private double clamp(double value, double min, double max) {
		// This unusual condition allows keeping only one branch
		// on common path when min < max and neither of them is NaN.
		// If min == max, we should additionally check for +0.0/-0.0 case,
		// so we're still visiting the if statement.
		if(!(min < max)) { // min greater than, equal to, or unordered with respect to max; NaN values are unordered
			if(Double.isNaN(min)) {
				throw new IllegalArgumentException("min is NaN");
			}
			if(Double.isNaN(max)) {
				throw new IllegalArgumentException("max is NaN");
			}
			if(Double.compare(min, max) > 0) {
				throw new IllegalArgumentException(min + " > " + max);
			}
			// Fall-through if min and max are exactly equal (or min = -0.0 and max = +0.0)
			// and none of them is NaN
		}
		return Math.min(max, Math.max(value, min));
	}
}
