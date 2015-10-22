package betterquesting.handlers;

import java.io.File;
import org.apache.logging.log4j.Level;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.world.WorldEvent;
import betterquesting.client.GuiQuestLines;
import betterquesting.core.BQ_Settings;
import betterquesting.core.BetterQuesting;
import betterquesting.party.PartyManager;
import betterquesting.quests.QuestDatabase;
import betterquesting.quests.QuestInstance;
import betterquesting.quests.QuestLine;
import betterquesting.quests.rewards.RewardItem;
import betterquesting.quests.tasks.TaskRetrieval;
import betterquesting.utils.JsonIO;
import com.google.gson.JsonObject;
import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class EventHandler
{
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onKey(InputEvent.KeyInputEvent event) // Currently for debugging purposes only. Replace with proper handler later
	{
		Minecraft mc = Minecraft.getMinecraft();
		if(mc.gameSettings.keyBindJump.getIsKeyPressed() && mc.thePlayer.getHeldItem() != null && mc.thePlayer.getHeldItem().getItem() == Items.apple)
		{
			mc.displayGuiScreen(new GuiQuestLines(mc.currentScreen));
		}
	}
	
	@SubscribeEvent
	public void onLivingUpdate(LivingUpdateEvent event)
	{
		if(event.entityLiving.worldObj.isRemote)
		{
			return;
		}
		
		if(event.entityLiving instanceof EntityPlayer)
		{
			QuestDatabase.UpdateTasks((EntityPlayer)event.entityLiving);
		}
	}
	
	@SubscribeEvent
	public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event)
	{
		if(event.modID.equals(BetterQuesting.MODID))
		{
			ConfigHandler.config.save();
			ConfigHandler.initConfigs();
		}
	}
	
	@SubscribeEvent
	public void onWorldSave(WorldEvent.Save event)
	{
		if(!event.world.isRemote && BQ_Settings.curWorldDir != null)
		{
			JsonObject jsonQ = new JsonObject();
			QuestDatabase.writeToJson(jsonQ);
			JsonIO.WriteToFile(new File(BQ_Settings.curWorldDir, "QuestDatabase.json"), jsonQ);
			
			JsonObject jsonP = new JsonObject();
			PartyManager.writeToJson(jsonP);
			JsonIO.WriteToFile(new File(BQ_Settings.curWorldDir, "QuestingParties.json"), jsonP);
		}
	}
	
	@SubscribeEvent
	public void onWorldUnload(WorldEvent.Unload event)
	{
		if(!event.world.isRemote && !MinecraftServer.getServer().isServerRunning())
		{
			BQ_Settings.curWorldDir = null;
		}
	}
	
	@SubscribeEvent
	public void onWorldLoad(WorldEvent.Load event)
	{
		if(!event.world.isRemote && BQ_Settings.curWorldDir == null)
		{
			MinecraftServer server = MinecraftServer.getServer();
			
			if(BetterQuesting.proxy.isClient())
			{
				BQ_Settings.curWorldDir = server.getFile("saves/" + server.getFolderName());
			} else
			{
				BQ_Settings.curWorldDir = server.getFile(server.getFolderName());
			}
			

	    	boolean b = true; // Use JSON files? [ --- DEBUG STUFF --- ]
	    	
	    	if(b)
	    	{
			    QuestDatabase.readFromJson(JsonIO.ReadFromFile(new File(BQ_Settings.curWorldDir, "QuestDatabase.json")));
			    PartyManager.readFromJson(JsonIO.ReadFromFile(new File(BQ_Settings.curWorldDir, "QuestingParties.json")));
			    
			    BetterQuesting.logger.log(Level.INFO, "Loaded " + QuestDatabase.questDB.size() + " quest instances and " + QuestDatabase.questLines.size() + " quest lines");
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
	
	@SubscribeEvent
	public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event)
	{
		if(!event.player.worldObj.isRemote && event.player instanceof EntityPlayerMP)
		{
			QuestDatabase.SendDatabase((EntityPlayerMP)event.player);
			PartyManager.SendDatabase((EntityPlayerMP)event.player);
		}
	}
}
