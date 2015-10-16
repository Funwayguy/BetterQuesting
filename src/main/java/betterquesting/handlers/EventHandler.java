package betterquesting.handlers;

import java.io.File;
import org.apache.logging.log4j.Level;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.world.WorldEvent;
import betterquesting.client.GuiQuestLines;
import betterquesting.core.BQ_Settings;
import betterquesting.core.BetterQuesting;
import betterquesting.network.PacketQuesting;
import betterquesting.party.PartyManager;
import betterquesting.quests.QuestDatabase;
import betterquesting.utils.JsonIO;
import betterquesting.utils.NBTConverter;
import com.google.gson.JsonObject;
import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;

public class EventHandler
{
	@SubscribeEvent
	public void onJoinWorld(EntityJoinWorldEvent event)
	{
		if(BetterQuesting.proxy.isClient() && event.entity instanceof EntityItem && ((EntityItem)event.entity).getEntityItem().getItem() == Items.apple)
		{
			Minecraft mc = Minecraft.getMinecraft();
			
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
			QuestDatabase.UpdateAll((EntityPlayer)event.entityLiving);
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
			QuestDatabase.writeToJSON(jsonQ);
			JsonIO.WriteToFile(new File(BQ_Settings.curWorldDir, "QuestDatabase.json"), jsonQ);
			
			JsonObject jsonP = new JsonObject();
			PartyManager.writeToJson(jsonP);
			JsonIO.WriteToFile(new File(BQ_Settings.curWorldDir, "QuestingParties.json"), jsonP);
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
			
		    QuestDatabase.readFromJSON(JsonIO.ReadFromFile(new File(BQ_Settings.curWorldDir, "QuestDatabase.json")));
		    PartyManager.readFromJson(JsonIO.ReadFromFile(new File(BQ_Settings.curWorldDir, "QuestingParties.json")));
		}
	}
	
	@SubscribeEvent
	public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event)
	{
		if(!event.player.worldObj.isRemote && event.player instanceof EntityPlayerMP)
		{
			BetterQuesting.logger.log(Level.INFO, "Sending quest database to " + event.player.getCommandSenderName());
			NBTTagCompound tags = new NBTTagCompound();
			tags.setInteger("ID", 0);
			JsonObject json = new JsonObject();
			QuestDatabase.writeToJSON(json);
			tags.setTag("Database", NBTConverter.JSONtoNBT_Object(json, new NBTTagCompound()));
			BetterQuesting.instance.network.sendTo(new PacketQuesting(tags), (EntityPlayerMP)event.player);
		}
	}
}
