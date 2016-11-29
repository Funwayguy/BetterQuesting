package betterquesting.api.api;

import betterquesting.api.client.gui.misc.IGuiHelper;
import betterquesting.api.client.importers.IImportRegistry;
import betterquesting.api.client.themes.IThemeRegistry;
import betterquesting.api.client.toolbox.IToolRegistry;
import betterquesting.api.network.IPacketRegistry;
import betterquesting.api.network.IPacketSender;
import betterquesting.api.questing.IQuestDatabase;
import betterquesting.api.questing.IQuestLineDatabase;
import betterquesting.api.questing.party.IPartyDatabase;
import betterquesting.api.questing.rewards.IRewardRegistry;
import betterquesting.api.questing.tasks.ITaskRegistry;
import betterquesting.api.storage.ILifeDatabase;
import betterquesting.api.storage.INameCache;
import betterquesting.api.storage.IQuestSettings;

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
	
	public static final ApiKey<IThemeRegistry> THEME_REG = new ApiKey<IThemeRegistry>();
	public static final ApiKey<IGuiHelper> GUI_HELPER = new ApiKey<IGuiHelper>();
	public static final ApiKey<IToolRegistry> TOOL_REG = new ApiKey<IToolRegistry>();
	public static final ApiKey<IImportRegistry> IMPORT_REG = new ApiKey<IImportRegistry>();
}
