package de.laserslime.antihealthindicator.data;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import de.laserslime.antihealthindicator.util.Version;

public class EntityDataIndex {

	private int index;
	private Class<?> entityClass;

	public static final EntityDataIndex AIR_TICKS;
	public static final EntityDataIndex HEALTH;
	public static final EntityDataIndex ABSORPTION;
	public static final EntityDataIndex XP;

	public EntityDataIndex(int index, Class<?> entityClass) {
		this.index = index;
		this.entityClass = entityClass;
	}

	// Apologies for the yandere dev code, but I don't want all of this data to permanently use memory.
	static {
		Version serverVersion = Version.getServerVersion();

		// This stays the same for all versions
		AIR_TICKS = new EntityDataIndex(1, Entity.class);

		if(serverVersion.isAtLeast(Version.V1_17_0))
			HEALTH = new EntityDataIndex(9, LivingEntity.class);
		else if(serverVersion.isAtLeast(Version.V1_14_0))
			HEALTH = new EntityDataIndex(8, LivingEntity.class);
		else if(serverVersion.isAtLeast(Version.V1_10_0))
			HEALTH = new EntityDataIndex(7, LivingEntity.class);
		else
			HEALTH = new EntityDataIndex(6, LivingEntity.class);

		if(serverVersion.isAtLeast(Version.V1_17_0))
			ABSORPTION = new EntityDataIndex(15, Player.class);
		else if(serverVersion.isAtLeast(Version.V1_15_0))
			ABSORPTION = new EntityDataIndex(14, Player.class);
		else if(serverVersion.isAtLeast(Version.V1_14_0))
			ABSORPTION = new EntityDataIndex(13, Player.class);
		else if(serverVersion.isAtLeast(Version.V1_10_0))
			ABSORPTION = new EntityDataIndex(11, Player.class);
		else if(serverVersion.isAtLeast(Version.V1_9_0))
			ABSORPTION = new EntityDataIndex(10, Player.class);
		else
			ABSORPTION = new EntityDataIndex(17, Player.class);

		if(serverVersion.isAtLeast(Version.V1_17_0))
			XP = new EntityDataIndex(16, Player.class);
		else if(serverVersion.isAtLeast(Version.V1_15_0))
			XP = new EntityDataIndex(15, Player.class);
		else if(serverVersion.isAtLeast(Version.V1_14_0))
			XP = new EntityDataIndex(14, Player.class);
		else if(serverVersion.isAtLeast(Version.V1_10_0))
			XP = new EntityDataIndex(12, Player.class);
		else if(serverVersion.isAtLeast(Version.V1_9_0))
			XP = new EntityDataIndex(11, Player.class);
		else
			XP = new EntityDataIndex(18, Player.class);
	}

	public boolean match(Class<?> entityClass, int index) {
		return this.index == index && this.entityClass.isAssignableFrom(entityClass);
	}

	public int getIndex() {
		return index;
	}

	public Class<?> getEntityClass() {
		return entityClass;
	}
}
