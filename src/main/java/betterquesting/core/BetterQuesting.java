package betterquesting.core;

import java.io.File;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.config.Configuration;
import org.apache.logging.log4j.Logger;
import betterquesting.core.proxies.CommonProxy;
import betterquesting.handlers.ConfigHandler;
import betterquesting.network.PacketQuesting;
import betterquesting.party.PartyManager;
import betterquesting.quests.QuestDatabase;
import betterquesting.quests.QuestInstance;
import betterquesting.quests.QuestLine;
import betterquesting.quests.rewards.RewardItem;
import betterquesting.quests.rewards.RewardRegistry;
import betterquesting.quests.tasks.TaskRegistry;
import betterquesting.quests.tasks.TaskRetrieval;
import betterquesting.utils.JsonIO;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;

@Mod(modid = BetterQuesting.MODID, version = BetterQuesting.VERSION, name = BetterQuesting.NAME, guiFactory = "betterquesting.handlers.ConfigGuiFactory")
public class BetterQuesting
{
    public static final String MODID = "betterquesting";
    public static final String VERSION = "BQ_VER_KEY";
    public static final String NAME = "BetterQuesting";
    public static final String PROXY = "betterquesting.core.proxies";
    public static final String CHANNEL = "BQ_NET_CHAN";
	
	@Instance(MODID)
	public static BetterQuesting instance;
	
	@SidedProxy(clientSide = PROXY + ".ClientProxy", serverSide = PROXY + ".CommonProxy")
	public static CommonProxy proxy;
	public SimpleNetworkWrapper network;
	public static Logger logger;
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
    	logger = event.getModLog();
    	network = NetworkRegistry.INSTANCE.newSimpleChannel(CHANNEL);
    	
    	ConfigHandler.config = new Configuration(event.getSuggestedConfigurationFile(), true);
    	ConfigHandler.initConfigs();
    	
    	proxy.registerHandlers();
    	
    	network.registerMessage(PacketQuesting.HandleClient.class, PacketQuesting.class, 0, Side.CLIENT);
    	network.registerMessage(PacketQuesting.HandleServer.class, PacketQuesting.class, 1, Side.SERVER);
    }
    
    @EventHandler
    public void init(FMLInitializationEvent event)
    {
    	TaskRegistry.RegisterQuest(TaskRetrieval.class, "retrieval");
    	RewardRegistry.RegisterReward(RewardItem.class, "item");
    }
    
    @EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
    	boolean b = false; // Use JSON files?
    	
    	if(b)
    	{
		    QuestDatabase.readFromJSON(JsonIO.ReadFromFile(new File("QuestDatabase.json")));
		    PartyManager.readFromJson(JsonIO.ReadFromFile(new File("QuestingParties.json")));
    	} else
    	{
	    	QuestInstance q1 = new QuestInstance(QuestDatabase.getUniqueID(), true);
	    	QuestInstance q2 = new QuestInstance(QuestDatabase.getUniqueID(), true);
	    	QuestInstance q3 = new QuestInstance(QuestDatabase.getUniqueID(), true);
	    	QuestInstance q4 = new QuestInstance(QuestDatabase.getUniqueID(), true);
	    	QuestInstance q5 = new QuestInstance(QuestDatabase.getUniqueID(), true);
	    	QuestInstance q6 = new QuestInstance(QuestDatabase.getUniqueID(), true);
	    	q1.name = "In The Beginning...";
	    	q2.name = "Quest 2";
	    	q3.name = "Quest 3";
	    	q4.name = "Quest 4";
	    	q5.name = "Quest 5";
	    	q6.name = "A Well Baked Lie";
	    	q4.AddPreRequisite(q3);
	    	q3.AddPreRequisite(q2);
	    	q5.AddPreRequisite(q2);
	    	q6.AddPreRequisite(q1);
	    	QuestLine line = new QuestLine();
	    	line.questList.add(q1);
	    	line.questList.add(q2);
	    	line.questList.add(q3);
	    	line.questList.add(q4);
	    	line.questList.add(q5);
	    	line.questList.add(q6);
	    	line.BuildTree();
	    	QuestDatabase.questLines.add(line);
	    	
	    	TaskRetrieval qb = new TaskRetrieval();
	    	qb.requiredItems.add(new ItemStack(Items.egg, 1));
	    	q1.questTypes.add(qb);
	    	qb = new TaskRetrieval();
	    	qb.requiredItems.add(new ItemStack(Items.milk_bucket, 3));
	    	q1.questTypes.add(qb);
	    	qb = new TaskRetrieval();
	    	qb.requiredItems.add(new ItemStack(Items.wheat, 3));
	    	q1.questTypes.add(qb);
	    	qb = new TaskRetrieval();
	    	qb.requiredItems.add(new ItemStack(Items.sugar, 2));
	    	q1.questTypes.add(qb);
	    	
	    	qb = new TaskRetrieval();
	    	qb.requiredItems.add(new ItemStack(Items.cake, 1));
	    	qb.requiredItems.add(new ItemStack(Blocks.torch));
	    	qb.requiredItems.add(new ItemStack(Items.potionitem));
	    	q6.questTypes.add(qb);
	    	
	    	RewardItem rb = new RewardItem();
	    	rb.rewards.add(new ItemStack(Items.diamond, 4));
	    	rb.rewards.add(new ItemStack(Items.emerald, 1));
	    	q6.rewards.add(rb);
	    	
			q1.description = "Gather these ingredients. Why? FOR SCIENCE!";
			
			q6.description = "Mix all your ingredients together on a crafting table "
					+ "and watch the MAGIC happen";
    	}
    }
}
