package betterquesting.network;

import betterquesting.api.network.IPacketHandler;
import betterquesting.api.network.IPacketRegistry;
import betterquesting.core.BetterQuesting;
import betterquesting.network.handlers.*;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;

public class PacketTypeRegistry implements IPacketRegistry {
	public static final PacketTypeRegistry INSTANCE = new PacketTypeRegistry();

	private final HashMap<ResourceLocation, IPacketHandler> pktHandlers = new HashMap<ResourceLocation, IPacketHandler>();

	private PacketTypeRegistry()
	{
	}

	public void init()
	{
		registerHandler(new PktHandlerQuestDB());
		registerHandler(new PktHandlerQuestSync());
		registerHandler(new PktHandlerQuestEdit());

		registerHandler(new PktHandlerLineDB());
		registerHandler(new PktHandlerLineEdit());
		registerHandler(new PktHandlerLineSync());

		registerHandler(new PktHandlerPartyDB());
		registerHandler(new PktHandlerPartyAction());
		registerHandler(new PktHandlerPartySync());

		registerHandler(new PktHandlerDetect());
		registerHandler(new PktHandlerClaim());

		registerHandler(new PktHandlerLives());
		registerHandler(new PktHandlerNotification());
		registerHandler(new PktHandlerTileEdit());
		registerHandler(new PktHandlerNameCache());
		registerHandler(new PktHandlerImport());
		registerHandler(new PktHandlerSettings());
		
		if(BetterQuesting.proxy.isClient())
        {
            registerHandler(new PktHandlerCacheSync());
        }
	}
	
	@Override
	public void registerHandler(IPacketHandler handler)
	{
		if(handler == null)
		{
			throw new NullPointerException("Tried to register null packet handler");
		} else if(handler.getRegistryName() == null)
		{
			throw new IllegalArgumentException("Tried to register a packet handler with a null name: " + handler.getClass());
		} else if(pktHandlers.containsKey(handler.getRegistryName()) || pktHandlers.containsValue(handler))
		{
			throw new IllegalArgumentException("Cannot register dupliate packet handler: " + handler.getRegistryName());
		}
		
		pktHandlers.put(handler.getRegistryName(), handler);
	}
	
	@Override
	public IPacketHandler getPacketHandler(ResourceLocation name)
	{
		return pktHandlers.get(name);
	}
}
