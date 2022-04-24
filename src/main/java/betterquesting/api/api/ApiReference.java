package betterquesting.api.api;

import betterquesting.api.client.importers.IImportRegistry;
import betterquesting.api.client.toolbox.IToolRegistry;
import betterquesting.api.network.IPacketRegistry;
import betterquesting.api.network.IPacketSender;
import betterquesting.api.questing.IQuestDatabase;
import betterquesting.api.questing.IQuestLineDatabase;
import betterquesting.api.questing.party.IPartyDatabase;
import betterquesting.api.questing.rewards.IReward;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api.storage.ILifeDatabase;
import betterquesting.api.storage.INameCache;
import betterquesting.api.storage.IQuestSettings;
import betterquesting.api2.client.gui.themes.IResourceReg;
import betterquesting.api2.client.gui.themes.IThemeRegistry;
import betterquesting.api2.registry.IFactoryData;
import betterquesting.api2.registry.IRegistry;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.nbt.NBTTagCompound;

public class ApiReference // Note to self: Don't make these client side only. It'll just crash servers regardless of which are used
{
    public static final ApiKey<IQuestDatabase> QUEST_DB = new ApiKey<>();
    public static final ApiKey<IQuestLineDatabase> LINE_DB = new ApiKey<>();
    public static final ApiKey<IPartyDatabase> PARTY_DB = new ApiKey<>();
    public static final ApiKey<ILifeDatabase> LIFE_DB = new ApiKey<>();

    public static final ApiKey<IRegistry<IFactoryData<ITask, NBTTagCompound>, ITask>> TASK_REG = new ApiKey<>();
    public static final ApiKey<IRegistry<IFactoryData<IReward, NBTTagCompound>, IReward>> REWARD_REG = new ApiKey<>();

    public static final ApiKey<IPacketSender> PACKET_SENDER = new ApiKey<>();
    public static final ApiKey<IPacketRegistry> PACKET_REG = new ApiKey<>();

    public static final ApiKey<IQuestSettings> SETTINGS = new ApiKey<>();
    public static final ApiKey<INameCache> NAME_CACHE = new ApiKey<>();

    public static final ApiKey<IThemeRegistry> THEME_REG = new ApiKey<>();
    public static final ApiKey<IResourceReg> RESOURCE_REG = new ApiKey<>();
    public static final ApiKey<IToolRegistry> TOOL_REG = new ApiKey<>();
    public static final ApiKey<IImportRegistry> IMPORT_REG = new ApiKey<>();

    public static final ApiKey<CreativeTabs> CREATIVE_TAB = new ApiKey<>();
}
