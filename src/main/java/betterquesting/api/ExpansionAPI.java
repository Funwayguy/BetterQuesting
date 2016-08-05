package betterquesting.api;

import org.apache.logging.log4j.Logger;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import betterquesting.api.database.ILifeDatabase;
import betterquesting.api.database.IPartyDatabase;
import betterquesting.api.database.IQuestDatabase;
import betterquesting.api.database.IQuestLineDatabase;
import betterquesting.api.database.IQuestProperties;
import betterquesting.api.network.IPacketSender;
import betterquesting.api.registry.IPacketRegistry;
import betterquesting.api.registry.IQuestIORegistry;
import betterquesting.api.registry.IRewardRegistry;
import betterquesting.api.registry.ITaskRegistry;
import betterquesting.api.registry.IThemeRegistry;
import betterquesting.api.registry.IToolRegistry;
import betterquesting.api.utils.IGuiBuilder;
import betterquesting.api.utils.IMakePlaceholder;

/**
 * API reference for BetterQuesting expansions.
 * Will be initialized when BetterQuesting is loaded.<br>
 * <b>WARNING:</b> Attempting to make more than one copy of this reference WILL throw an error
 */
public final class ExpansionAPI implements IQuestingAPI
{
	public static ExpansionAPI INSTANCE;
	
	/**
	 * Convenience method for checking whether BetterQuesting has loaded and made its API accessible yet.
	 */
	public static boolean isReady()
	{
		return INSTANCE != null;
	}
	
	private final IQuestingAPI API;
	
	public ExpansionAPI(IQuestingAPI parentApi)
	{
		this.API = parentApi;
		
		if(INSTANCE == null)
		{
			INSTANCE = this;
		} else
		{
			throw new IllegalStateException("Cannot instatiate ExpansionAPI more than once!");
		}
	}
	
	@Override
	public ITaskRegistry getTaskRegistry()
	{
		return API.getTaskRegistry();
	}
	
	@Override
	public IRewardRegistry getRewardRegistry()
	{
		return API.getRewardRegistry();
	}
	
	@Override
	public IPacketRegistry getPacketRegistry()
	{
		return API.getPacketRegistry();
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public IThemeRegistry getThemeRegistry()
	{
		return API.getThemeRegistry();
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public IToolRegistry getToolboxRegistry()
	{
		return API.getToolboxRegistry();
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public IQuestIORegistry getIORegistry()
	{
		return API.getIORegistry();
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public IGuiBuilder getGuiBuilder()
	{
		return API.getGuiBuilder();
	}
	
	@Override
	public IPacketSender getPacketSender()
	{
		return API.getPacketSender();
	}
	
	@Override
	public IMakePlaceholder getPlaceholderMaker()
	{
		return API.getPlaceholderMaker();
	}
	
	@Override
	public IQuestLineDatabase getQuestLineDB()
	{
		return API.getQuestLineDB();
	}
	
	@Override
	public IQuestDatabase getQuestDB()
	{
		return API.getQuestDB();
	}
	
	@Override
	public IPartyDatabase getPartyDB()
	{
		return API.getPartyDB();
	}
	
	@Override
	public ILifeDatabase getLifeDB()
	{
		return API.getLifeDB();
	}
	
	@Override
	public IQuestProperties getProperties()
	{
		return API.getProperties();
	}
	
	@Override
	public Logger getLogger()
	{
		return API.getLogger();
	}
}
