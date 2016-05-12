package betterquesting.network;

import java.util.HashMap;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.Level;
import betterquesting.core.BetterQuesting;
import betterquesting.network.handlers.PktHandler;
import betterquesting.network.handlers.PktHandlerClaim;
import betterquesting.network.handlers.PktHandlerDetect;
import betterquesting.network.handlers.PktHandlerLineEdit;
import betterquesting.network.handlers.PktHandlerLives;
import betterquesting.network.handlers.PktHandlerNotification;
import betterquesting.network.handlers.PktHandlerPartyAction;
import betterquesting.network.handlers.PktHandlerPartyDB;
import betterquesting.network.handlers.PktHandlerQuestDB;
import betterquesting.network.handlers.PktHandlerQuestEdit;
import betterquesting.network.handlers.PktHandlerQuestSync;
import betterquesting.network.handlers.PktHandlerTileEdit;

public class PacketTypeRegistry
{
	static HashMap<String, PktHandler> pktHandlers = new HashMap<String, PktHandler>();
	
	public static void RegisterType(PktHandler handler, ResourceLocation registryName)
	{
		try
		{
			if(handler == null)
			{
				throw new NullPointerException("Tried to register null packet handler");
			} else if(registryName == null)
			{
				throw new IllegalArgumentException("Tried to register a packet handler with a null name");
			}
			
			if(pktHandlers.containsKey(registryName.toString()) || pktHandlers.containsValue(handler))
			{
				throw new IllegalStateException("Cannot register dupliate packet handlerType type '" + registryName.toString() + "'");
			}
			
			pktHandlers.put(registryName.toString(), handler);
		} catch(Exception e)
		{
			BetterQuesting.logger.log(Level.ERROR, "An error occured while trying to register packet handler", e);
		}
	}
	
	public static PktHandler GetHandler(String name)
	{
		if(name == null)
		{
			return null;
		}
		
		return pktHandlers.get(name);
	}
	
	public static void RegisterNativeHandlers()
	{
		RegisterType(new PktHandlerQuestDB(), BQPacketType.QUEST_DATABASE.GetLocation());
		RegisterType(new PktHandlerPartyDB(), BQPacketType.PARTY_DATABASE.GetLocation());
		RegisterType(new PktHandlerQuestSync(), BQPacketType.QUEST_SYNC.GetLocation());
		RegisterType(new PktHandlerQuestEdit(), BQPacketType.QUEST_EDIT.GetLocation());
		RegisterType(new PktHandlerLineEdit(), BQPacketType.LINE_EDIT.GetLocation());
		RegisterType(new PktHandlerDetect(), BQPacketType.DETECT.GetLocation());
		RegisterType(new PktHandlerClaim(), BQPacketType.CLAIM.GetLocation());
		RegisterType(new PktHandlerPartyAction(), BQPacketType.PARTY_ACTION.GetLocation());
		RegisterType(new PktHandlerLives(), BQPacketType.LIFE_SYNC.GetLocation());
		RegisterType(new PktHandlerNotification(), BQPacketType.NOTIFICATION.GetLocation());
		RegisterType(new PktHandlerTileEdit(), BQPacketType.EDIT_STATION.GetLocation());
	}
	
	public enum BQPacketType
	{
		QUEST_DATABASE,
		PARTY_DATABASE,
		QUEST_SYNC,
		QUEST_EDIT,
		LINE_EDIT,
		DETECT,
		CLAIM,
		PARTY_ACTION,
		LIFE_SYNC,
		EDIT_STATION,
		NOTIFICATION;
		
		public ResourceLocation GetLocation()
		{
			return new ResourceLocation(BetterQuesting.MODID + ":" + this.toString().toLowerCase());
		}
		
		public String GetName()
		{
			return BetterQuesting.MODID + ":" + this.toString().toLowerCase();
		}
	}
}
