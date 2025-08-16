package me.lasersli.antihealthindicator.packetadapters;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;

import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;

import me.lasersli.antihealthindicator.util.Version;

public class WorldSeedAdapter extends PacketAdapter {

	public WorldSeedAdapter(Plugin plugin) {
		super(plugin, PacketType.Play.Server.RESPAWN, PacketType.Play.Server.LOGIN);
	}

	@Override
	public void onPacketSending(PacketEvent event) {
		if(Version.getServerVersion().isAtLeast(Version.V1_20_2)) {
			final int index = event.getPacketType().equals(PacketType.Play.Server.LOGIN) ? 9 : 0;
			try {
				Object spawnInfo = event.getPacket().getModifier().read(index);
				Object[] params = new Object[spawnInfo.getClass().getRecordComponents().length];
				for(int i = 0; i < params.length; i++) {
					if(i == 2)
						params[2] = 0L;
					else
						params[i] = spawnInfo.getClass().getRecordComponents()[i].getAccessor().invoke(spawnInfo);
				}
				Object newSpawnInfo = getCanonicalConstructor(spawnInfo.getClass()).newInstance(params);
				event.getPacket().getModifier().write(index, newSpawnInfo);
			} catch(InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException | NoSuchMethodException ex) {
				ex.printStackTrace();
			}
		} else
			event.getPacket().getLongs().writeSafely(0, 0L);
	}

	/**
	 * From the api note of {@link Class#getRecordComponents()}. Modified so that the parameter doesn't have to be a record.
	 * 
	 * @param cls Must be a record
	 * @return The canonical constructor of the provided record
	 * @throws NoSuchMethodException If the constructor can't be found
	 */
	static Constructor<?> getCanonicalConstructor(Class<?> cls) throws NoSuchMethodException {
		Class<?>[] paramTypes = Arrays.stream(cls.getRecordComponents()).map(RecordComponent::getType).toArray(Class<?>[]::new);
		return cls.getDeclaredConstructor(paramTypes);
	}
}
