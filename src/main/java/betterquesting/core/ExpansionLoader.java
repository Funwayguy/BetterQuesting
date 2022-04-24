package betterquesting.core;

import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.client.importers.ImporterRegistry;
import betterquesting.client.themes.ResourceRegistry;
import betterquesting.client.themes.ThemeRegistry;
import betterquesting.client.toolbox.ToolboxRegistry;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeRegistry;
import betterquesting.questing.QuestDatabase;
import betterquesting.questing.QuestLineDatabase;
import betterquesting.questing.party.PartyManager;
import betterquesting.questing.rewards.RewardRegistry;
import betterquesting.questing.tasks.TaskRegistry;
import betterquesting.storage.LifeDatabase;
import betterquesting.storage.NameCache;
import betterquesting.storage.QuestSettings;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ExpansionLoader {
    public static final ExpansionLoader INSTANCE = new ExpansionLoader();

    public void initCommonAPIs() {
        QuestingAPI.registerAPI(ApiReference.QUEST_DB, QuestDatabase.INSTANCE);
        QuestingAPI.registerAPI(ApiReference.LINE_DB, QuestLineDatabase.INSTANCE);
        QuestingAPI.registerAPI(ApiReference.PARTY_DB, PartyManager.INSTANCE);
        QuestingAPI.registerAPI(ApiReference.LIFE_DB, LifeDatabase.INSTANCE);

        QuestingAPI.registerAPI(ApiReference.TASK_REG, TaskRegistry.INSTANCE);
        QuestingAPI.registerAPI(ApiReference.REWARD_REG, RewardRegistry.INSTANCE);

        QuestingAPI.registerAPI(ApiReference.PACKET_SENDER, PacketSender.INSTANCE);
        QuestingAPI.registerAPI(ApiReference.PACKET_REG, PacketTypeRegistry.INSTANCE);

        QuestingAPI.registerAPI(ApiReference.SETTINGS, QuestSettings.INSTANCE);
        QuestingAPI.registerAPI(ApiReference.NAME_CACHE, NameCache.INSTANCE);

        QuestingAPI.registerAPI(ApiReference.CREATIVE_TAB, BetterQuesting.tabQuesting);
    }

    @SideOnly(Side.CLIENT)
    public void initClientAPIs() {
        QuestingAPI.registerAPI(ApiReference.THEME_REG, ThemeRegistry.INSTANCE);
        QuestingAPI.registerAPI(ApiReference.RESOURCE_REG, ResourceRegistry.INSTANCE);
        QuestingAPI.registerAPI(ApiReference.TOOL_REG, ToolboxRegistry.INSTANCE);
        QuestingAPI.registerAPI(ApiReference.IMPORT_REG, ImporterRegistry.INSTANCE);
    }
}
