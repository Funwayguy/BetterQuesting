package betterquesting.network;

import java.util.HashMap;
import net.minecraft.util.ResourceLocation;
import betterquesting.api.network.IPacketHandler;
import betterquesting.api.network.IPacketRegistry;
import betterquesting.network.handlers.PktHandlerClaim;
import betterquesting.network.handlers.PktHandlerDetect;
import betterquesting.network.handlers.PktHandlerImport;
import betterquesting.network.handlers.PktHandlerLineDB;
import betterquesting.network.handlers.PktHandlerLineEdit;
import betterquesting.network.handlers.PktHandlerLineSync;
import betterquesting.network.handlers.PktHandlerLives;
import betterquesting.network.handlers.PktHandlerNameCache;
import betterquesting.network.handlers.PktHandlerNotification;
import betterquesting.network.handlers.PktHandlerPartyAction;
import betterquesting.network.handlers.PktHandlerPartyDB;
import betterquesting.network.handlers.PktHandlerPartySync;
import betterquesting.network.handlers.PktHandlerQuestDB;
import betterquesting.network.handlers.PktHandlerQuestEdit;
import betterquesting.network.handlers.PktHandlerQuestSync;
import betterquesting.network.handlers.PktHandlerTileEdit;

public class PacketTypeRegistry implements IPacketRegistry
{
	public static final PacketTypeRegistry INSTANCE = new PacketTypeRegistry();
	
	private final HashMap<ResourceLocation, IPacketHandler> pktHandlers = new HashMap<ResourceLocation, IPacketHandler>();
	
	private PacketTypeRegistry()
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
