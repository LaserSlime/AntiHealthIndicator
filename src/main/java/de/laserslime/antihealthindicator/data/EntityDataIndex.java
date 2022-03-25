package de.laserslime.antihealthindicator.data;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import de.laserslime.antihealthindicator.util.Version;

public class EntityDataIndex {

	private int index;
	private Class<?> typeClass;
	private Class<?> entityClass;

	public static final EntityDataIndex AIR_TICKS;
	public static final EntityDataIndex HEALTH;
	public static final EntityDataIndex ABSORPTION;
	public static final EntityDataIndex XP;

	public EntityDataIndex(int index, Class<?> typeClass, Class<?> entityClass) {
		this.index = index;
		this.typeClass = typeClass;
		this.entityClass = entityClass;
	}

	static {
		// Cool spaghetti code ik, but it's probably the best way in terms of performance and ram usage
		int serverProtocol = Version.getServerVersion().getProtocolVersion();
		AIR_TICKS = new EntityDataIndex(1, Integer.class, Entity.class);//This stays the same for all versions
		
		if(serverProtocol >= Version.V1_17_0.getProtocolVersion())
			HEALTH = new EntityDataIndex(9, Float.class, LivingEntity.class);
		else if(serverProtocol >= Version.V1_14_0.getProtocolVersion())
			HEALTH = new EntityDataIndex(8, Float.class, LivingEntity.class);
		else if(serverProtocol >= Version.V1_10_0.getProtocolVersion())
			HEALTH = new EntityDataIndex(7, Float.class, LivingEntity.class);
		else
			HEALTH = new EntityDataIndex(6, Float.class, LivingEntity.class);

		if(serverProtocol >= Version.V1_17_0.getProtocolVersion())
			ABSORPTION = new EntityDataIndex(15, Float.class, Player.class);
		else if(serverProtocol >= Version.V1_15_0.getProtocolVersion())
			ABSORPTION = new EntityDataIndex(14, Float.class, Player.class);
		else if(serverProtocol >= Version.V1_14_0.getProtocolVersion())
			ABSORPTION = new EntityDataIndex(13, Float.class, Player.class);
		else if(serverProtocol >= Version.V1_10_0.getProtocolVersion())
			ABSORPTION = new EntityDataIndex(11, Float.class, Player.class);
		else if(serverProtocol >= Version.V1_9_0.getProtocolVersion())
			ABSORPTION = new EntityDataIndex(10, Float.class, Player.class);
		else
			ABSORPTION = new EntityDataIndex(17, Float.class, Player.class);

		if(serverProtocol >= Version.V1_17_0.getProtocolVersion())
			XP = new EntityDataIndex(16, Integer.class, Player.class);
		else if(serverProtocol >= Version.V1_15_0.getProtocolVersion())
			XP = new EntityDataIndex(15, Integer.class, Player.class);
		else if(serverProtocol >= Version.V1_14_0.getProtocolVersion())
			XP = new EntityDataIndex(14, Integer.class, Player.class);
		else if(serverProtocol >= Version.V1_10_0.getProtocolVersion())
			XP = new EntityDataIndex(12, Integer.class, Player.class);
		else if(serverProtocol >= Version.V1_9_0.getProtocolVersion())
			XP = new EntityDataIndex(11, Integer.class, Player.class);
		else
			XP = new EntityDataIndex(18, Integer.class, Player.class);
	}

	public boolean match(Class<?> entityClass, int index) {
		return this.index == index && this.entityClass.isAssignableFrom(entityClass);
	}

	public int getIndex() {
		return index;
	}

	public Class<?> getTypeClass() {
		return typeClass;
	}

	public Class<?> getEntityClass() {
		return entityClass;
	}
}
