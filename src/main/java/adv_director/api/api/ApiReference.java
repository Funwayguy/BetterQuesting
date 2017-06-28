package adv_director.api.api;

import net.minecraft.creativetab.CreativeTabs;
import adv_director.api.client.gui.misc.IGuiHelper;
import adv_director.api.client.importers.IImportRegistry;
import adv_director.api.client.themes.IThemeRegistry;
import adv_director.api.client.toolbox.IToolRegistry;
import adv_director.api.network.IPacketRegistry;
import adv_director.api.network.IPacketSender;
import adv_director.api.questing.IQuestDatabase;
import adv_director.api.questing.IQuestLineDatabase;
import adv_director.api.questing.party.IPartyDatabase;
import adv_director.api.questing.rewards.IRewardRegistry;
import adv_director.api.questing.tasks.ITaskRegistry;
import adv_director.api.storage.ILifeDatabase;
import adv_director.api.storage.INameCache;
import adv_director.api.storage.IQuestSettings;

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
	
	public static final ApiKey<CreativeTabs> CREATIVE_TAB = new ApiKey<CreativeTabs>();
}
