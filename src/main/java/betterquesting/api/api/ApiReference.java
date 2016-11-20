package betterquesting.api.api;

import betterquesting.api.client.gui.IGuiHelper;
import betterquesting.api.client.themes.IThemeRegistry;
import betterquesting.api.io.IQuestIORegistry;
import betterquesting.api.network.IPacketRegistry;
import betterquesting.api.network.IPacketSender;
import betterquesting.api.questing.IQuestDatabase;
import betterquesting.api.questing.IQuestLineDatabase;
import betterquesting.api.questing.party.IPartyDatabase;
import betterquesting.api.questing.rewards.IRewardRegistry;
import betterquesting.api.questing.tasks.ITaskRegistry;
import betterquesting.api.registry.ILifeDatabase;
import betterquesting.api.registry.INameCache;
import betterquesting.api.registry.IQuestSettings;
import betterquesting.api.toolbox.IToolRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ApiReference
{
	public static final ApiKey<IQuestDatabase> QUEST_DB = new ApiKey<IQuestDatabase>();
	public static final ApiKey<IQuestLineDatabase> LINE_DB = new ApiKey<IQuestLineDatabase>();
	public static final ApiKey<IPartyDatabase> PARTY_DB = new ApiKey<IPartyDatabase>();
	public static final ApiKey<ILifeDatabase> LIFE_DB = new ApiKey<ILifeDatabase>();
	
	public static final ApiKey<ITaskRegistry> TASK_REG = new ApiKey<ITaskRegistry>();
	public static final ApiKey<IRewardRegistry> REWARD_REG = new ApiKey<IRewardRegistry>();
	
	public static final ApiKey<IPacketSender> PACKET_SENDER = new ApiKey<IPacketSender>();
	public static final ApiKey<IPacketRegistry> PACKET_REG = new ApiKey<IPacketRegistry>();
	
	public static final ApiKey<IQuestSettings> SETTINGS = new ApiKey<IQuestSettings>();
	public static final ApiKey<INameCache> NAME_CACHE = new ApiKey<INameCache>();
	
	@SideOnly(Side.CLIENT)
	public static final ApiKey<IThemeRegistry> THEME_REG = new ApiKey<IThemeRegistry>();
	@SideOnly(Side.CLIENT)
	public static final ApiKey<IGuiHelper> GUI_HELPER = new ApiKey<IGuiHelper>();
	@SideOnly(Side.CLIENT)
	public static final ApiKey<IToolRegistry> TOOL_REG = new ApiKey<IToolRegistry>();
	@SideOnly(Side.CLIENT)
	public static final ApiKey<IQuestIORegistry> IO_REG = new ApiKey<IQuestIORegistry>();
}
