package de.laserslime.antihealthindicator.entitydata;

import de.laserslime.antihealthindicator.util.Version;

public class EntityDataIndexes {

	public static final int AIR_TICKS;
	public static final int HEALTH;
	public static final int ABSORPTION;
	public static final int XP;

	static {
		Version serverVersion = Version.getServerVersion();

		// This stays the same for all versions
		AIR_TICKS = 1;

		if(serverVersion.isAtLeast(Version.V1_17_0))
			HEALTH = 9;
		else if(serverVersion.isAtLeast(Version.V1_14_0))
			HEALTH = 8;
		else if(serverVersion.isAtLeast(Version.V1_10_0))
			HEALTH = 7;
		else
			HEALTH = 6;

		if(serverVersion.isAtLeast(Version.V1_17_0))
			ABSORPTION = 15;
		else if(serverVersion.isAtLeast(Version.V1_15_0))
			ABSORPTION = 14;
		else if(serverVersion.isAtLeast(Version.V1_14_0))
			ABSORPTION = 13;
		else if(serverVersion.isAtLeast(Version.V1_10_0))
			ABSORPTION = 11;
		else if(serverVersion.isAtLeast(Version.V1_9_0))
			ABSORPTION = 10;
		else
			ABSORPTION = 17;

		if(serverVersion.isAtLeast(Version.V1_17_0))
			XP = 16;
		else if(serverVersion.isAtLeast(Version.V1_15_0))
			XP = 15;
		else if(serverVersion.isAtLeast(Version.V1_14_0))
			XP = 14;
		else if(serverVersion.isAtLeast(Version.V1_10_0))
			XP = 12;
		else if(serverVersion.isAtLeast(Version.V1_9_0))
			XP = 11;
		else
			XP = 18;
	}
}
