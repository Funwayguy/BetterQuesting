package betterquesting.core;

import org.apache.logging.log4j.Logger;
import betterquesting.api.IQuestingAPI;
import betterquesting.api.database.ILifeDatabase;
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
import betterquesting.client.GuiBuilder;
import betterquesting.database.QuestDatabase;
import betterquesting.database.QuestLineDatabase;
import betterquesting.importers.ImporterRegistry;
import betterquesting.lives.LifeDatabase;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeRegistry;
import betterquesting.party.PartyManager;
import betterquesting.quests.QuestSettings;
import betterquesting.registry.RewardRegistry;
import betterquesting.registry.TaskRegistry;
import betterquesting.registry.ThemeRegistry;
import betterquesting.registry.ToolboxRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public final class ParentAPI implements IQuestingAPI
{
	public static final ParentAPI API = new ParentAPI();
	
	private ParentAPI()
	{
	}
	
	@Override
	public ITaskRegistry getTaskRegistry()
	{
		return TaskRegistry.INSTANCE;
	}
	
	@Override
	public IRewardRegistry getRewardRegistry()
	{
		return RewardRegistry.INSTANCE;
	}
	
	@Override
	public IPacketRegistry getPacketRegistry()
	{
		return PacketTypeRegistry.INSTANCE;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public IThemeRegistry getThemeRegistry()
	{
		return ThemeRegistry.INSTANCE;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public IToolRegistry getToolboxRegistry()
	{
		return ToolboxRegistry.INSTANCE;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public IQuestIORegistry getIORegistry()
	{
		return ImporterRegistry.INSTANCE;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public IGuiBuilder getGuiBuilder()
	{
		return GuiBuilder.INSTANCE;
	}

	@Override
	public IPacketSender getPacketSender()
	{
		return PacketSender.INSTANCE;
	}

	@Override
	public IQuestLineDatabase getQuestLineDB()
	{
		return QuestLineDatabase.INSTANCE;
	}

	@Override
	public IQuestDatabase getQuestDB()
	{
		return QuestDatabase.INSTANCE;
	}

	@Override
	public IPartyDatabase getPartyDB()
	{
		return PartyManager.INSTANCE;
	}

	@Override
	public ILifeDatabase getLifeDB()
	{
		return LifeDatabase.INSTANCE;
	}
	
	@Override
	public IQuestSettings getSettings()
	{
		return QuestSettings.INSTANCE;
	}

	@Override
	public Logger getLogger()
	{
		return BetterQuesting.logger;
	}
	
}
