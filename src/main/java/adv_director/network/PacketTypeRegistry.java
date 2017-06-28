package adv_director.network;

import java.util.HashMap;
import net.minecraft.util.ResourceLocation;
import adv_director.api.network.IPacketHandler;
import adv_director.api.network.IPacketRegistry;
import adv_director.network.handlers.PktHandlerClaim;
import adv_director.network.handlers.PktHandlerDetect;
import adv_director.network.handlers.PktHandlerImport;
import adv_director.network.handlers.PktHandlerLineDB;
import adv_director.network.handlers.PktHandlerLineEdit;
import adv_director.network.handlers.PktHandlerLineSync;
import adv_director.network.handlers.PktHandlerLives;
import adv_director.network.handlers.PktHandlerNameCache;
import adv_director.network.handlers.PktHandlerNotification;
import adv_director.network.handlers.PktHandlerPartyAction;
import adv_director.network.handlers.PktHandlerPartyDB;
import adv_director.network.handlers.PktHandlerPartySync;
import adv_director.network.handlers.PktHandlerQuestDB;
import adv_director.network.handlers.PktHandlerQuestEdit;
import adv_director.network.handlers.PktHandlerQuestSync;
import adv_director.network.handlers.PktHandlerSettings;
import adv_director.network.handlers.PktHandlerTileEdit;

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
		registerHandler(new PktHandlerSettings());
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
