package de.laserslime.antihealthindicator.util;

import org.bukkit.Bukkit;

public enum Version {

	// If anyone knows a way to get the protocol version without garbage like this or hacky nms reflections, feel free to pr ;)
	V1_8_4("1.8.8-R0.1-SNAPSHOT", 47), V1_9_0("1.9-R0.1-SNAPSHOT", 107), V1_9_1("1.9.2-R0.1-SNAPSHOT", 108), V1_9_2("1.9.2-R0.1-SNAPSHOT", 109), V1_9_3("1.9.4-R0.1-SNAPSHOT", 110),
	V1_10_0("1.10.2-R0.1-SNAPSHOT", 210), V1_11_0("1.11-R0.1-SNAPSHOT", 315), V1_11_2("1.11.2-R0.1-SNAPSHOT", 316), V1_12_0("1.12-R0.1-SNAPSHOT", 335), V1_12_1("1.12.1-R0.1-SNAPSHOT", 338),
	V1_12_2("1.12.2-R0.1-SNAPSHOT", 340), V1_13_0("1.13-R0.1-SNAPSHOT", 393), V1_13_1("1.13.1-R0.1-SNAPSHOT", 401), V1_13_2("1.13.2-R0.1-SNAPSHOT", 404), V1_14_0("1.14-R0.1-SNAPSHOT", 477),
	V1_14_1("1.14.1-R0.1-SNAPSHOT", 480), V1_14_2("1.14.2-R0.1-SNAPSHOT", 485), V1_14_3("1.14.3-R0.1-SNAPSHOT", 490), V1_14_4("1.14.4-R0.1-SNAPSHOT", 498), V1_15_0("1.15-R0.1-SNAPSHOT", 573),
	V1_15_1("1.15.1-R0.1-SNAPSHOT", 575), V1_15_2("1.15.2-R0.1-SNAPSHOT", 578), V1_16_0("1.16.1-R0.1-SNAPSHOT", 735), V1_16_1("1.16.1-R0.1-SNAPSHOT", 736), V1_16_2("1.16.2-R0.1-SNAPSHOT", 751),
	V1_16_3("1.16.3-R0.1-SNAPSHOT", 753), V1_16_4("1.16.4-R0.1-SNAPSHOT", 754), V1_16_5("1.16.5-R0.1-SNAPSHOT", 754), V1_17_0("1.17-R0.1-SNAPSHOT", 755), V1_17_1("1.17.1-R0.1-SNAPSHOT", 756),
	V1_18_0("1.18-R0.1-SNAPSHOT", 757), V1_18_1("1.18.1-R0.1-SNAPSHOT", 757), V1_18_2("1.18.2-R0.1-SNAPSHOT", 758), V1_19("1.19-R0.1-SNAPSHOT", 759), V1_19_1("1.19.1-R0.1-SNAPSHOT", 760),
	V1_19_2("1.19.2-R0.1-SNAPSHOT", 760), V1_19_3("1.19.3-R0.1-SNAPSHOT", 761), V1_19_4("1.19.4-R0.1-SNAPSHOT", 762), V1_20_0("1.20-R0.1-SNAPSHOT", 763), V1_20_1("1.20.1-R0.1-SNAPSHOT", 763),
	V1_20_2("1.20.2-R0.1-SNAPSHOT", 764), V1_20_3("1.20.3-R0.1-SNAPSHOT", 765), V1_20_4("1.20.4-R0.1-SNAPSHOT", 765), V1_20_5("1.20.5-R0.1-SNAPSHOT", 766), V1_20_6("1.20.6-R0.1-SNAPSHOT", 766),
	V1_21_0("1.21-R0.1-SNAPSHOT", 767), UNKNOWN("", Integer.MAX_VALUE);

	private String bukkitVersion;
	private int protocolVersion;

	private Version(String bukkitVersion, int protocolVersion) {
		this.bukkitVersion = bukkitVersion;
		this.protocolVersion = protocolVersion;
	}

	public boolean isBefore(Version version) {
		return protocolVersion < version.protocolVersion;
	}

	public boolean isAfter(Version version) {
		return protocolVersion > version.protocolVersion;
	}

	public boolean isAtMost(Version version) {
		return protocolVersion <= version.protocolVersion;
	}

	public boolean isAtLeast(Version version) {
		return protocolVersion >= version.protocolVersion;
	}

	public static Version getServerVersion() {
		for(Version currentVersion : Version.values()) {
			if(currentVersion.bukkitVersion.equals(Bukkit.getBukkitVersion()))
				return currentVersion;
		}
		return UNKNOWN;
	}
}
