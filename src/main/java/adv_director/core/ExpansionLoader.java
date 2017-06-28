package adv_director.core;

import java.util.ArrayList;
import java.util.List;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Level;
import adv_director.api.api.ApiReference;
import adv_director.api.api.IQuestExpansion;
import adv_director.api.api.QuestExpansion;
import adv_director.api.api.QuestingAPI;
import adv_director.client.GuiBuilder;
import adv_director.client.importers.ImporterRegistry;
import adv_director.client.themes.ThemeRegistry;
import adv_director.client.toolbox.ToolboxRegistry;
import adv_director.network.PacketSender;
import adv_director.network.PacketTypeRegistry;
import adv_director.questing.QuestDatabase;
import adv_director.questing.QuestLineDatabase;
import adv_director.questing.party.PartyManager;
import adv_director.questing.rewards.RewardRegistry;
import adv_director.questing.tasks.TaskRegistry;
import adv_director.storage.LifeDatabase;
import adv_director.storage.NameCache;
import adv_director.storage.QuestSettings;

public class ExpansionLoader
{
	public static final ExpansionLoader INSTANCE = new ExpansionLoader();
	
	private final ArrayList<IQuestExpansion> expansions = new ArrayList<IQuestExpansion>();
	
	private ExpansionLoader()
	{
	}
	
	public void loadExpansions(ASMDataTable asmData)
	{
		expansions.clear();
		
		for(ASMDataTable.ASMData data : asmData.getAll(QuestExpansion.class.getCanonicalName()))
		{
			try
			{
				Class<? extends IQuestExpansion> expClass = Class.forName(data.getClassName()).asSubclass(IQuestExpansion.class);
				expansions.add(expClass.newInstance());
			} catch(Exception e)
			{
				AdvDirector.logger.log(Level.INFO, "Unable to load BetterQuesting expansion: ", e);
			}
		}
	}
	
	public List<IQuestExpansion> getAllExpansions()
	{
		return expansions;
	}
	
	public void initCommonAPIs()
	{
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
		
		QuestingAPI.registerAPI(ApiReference.CREATIVE_TAB, AdvDirector.tabQuesting);
	}
	
	@SideOnly(Side.CLIENT)
	public void initClientAPIs()
	{
		QuestingAPI.registerAPI(ApiReference.THEME_REG, ThemeRegistry.INSTANCE);
		QuestingAPI.registerAPI(ApiReference.GUI_HELPER, GuiBuilder.INSTANCE);
		QuestingAPI.registerAPI(ApiReference.TOOL_REG, ToolboxRegistry.INSTANCE);
		QuestingAPI.registerAPI(ApiReference.IMPORT_REG, ImporterRegistry.INSTANCE);
	}
}
