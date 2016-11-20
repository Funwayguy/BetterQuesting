package betterquesting.api;

import org.apache.logging.log4j.Logger;
import betterquesting.api.database.ILifeDatabase;
import betterquesting.api.database.INameCache;
import betterquesting.api.database.IPartyDatabase;
import betterquesting.api.database.IQuestDatabase;
import betterquesting.api.database.IQuestLineDatabase;
import betterquesting.api.database.IQuestSettings;
import betterquesting.api.network.IPacketSender;
import betterquesting.api.registry.IPacketRegistry;
import betterquesting.api.registry.IQuestIORegistry;
import betterquesting.api.registry.IRewardRegistry;
import betterquesting.api.registry.ITaskRegistry;
import betterquesting.api.registry.IThemeRegistry;
import betterquesting.api.registry.IToolRegistry;
import betterquesting.api.utils.IGuiBuilder;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Contains all the API calls an expansion may need to make such as accessing the quest database or party information.
 * This information will be passed to the expansion when loaded by the base mod
 */
public interface IQuestingAPI
{
	public ITaskRegistry getTaskRegistry();
	public IRewardRegistry getRewardRegistry();
	public IPacketRegistry getPacketRegistry();
	
	@SideOnly(Side.CLIENT)
	public IThemeRegistry getThemeRegistry();
	
	@SideOnly(Side.CLIENT)
	public IToolRegistry getToolboxRegistry();
	
	@SideOnly(Side.CLIENT)
	public IQuestIORegistry getIORegistry();
	
	@SideOnly(Side.CLIENT)
	public IGuiBuilder getGuiBuilder();
	
	public IPacketSender getPacketSender();
	
	public IQuestLineDatabase getQuestLineDB();
	public IQuestDatabase getQuestDB();
	public IPartyDatabase getPartyDB();
	public ILifeDatabase getLifeDB();
	public INameCache getNameCache();
	
	public IQuestSettings getSettings();
	
	public Logger getLogger();
}
