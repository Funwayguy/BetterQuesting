package betterquesting.handlers;

import java.io.File;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.IIcon;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.world.WorldEvent;
import org.apache.logging.log4j.Level;
import betterquesting.client.BQ_Keybindings;
import betterquesting.client.gui.GuiHome;
import betterquesting.client.themes.ThemeRegistry;
import betterquesting.core.BQ_Settings;
import betterquesting.core.BetterQuesting;
import betterquesting.party.PartyManager;
import betterquesting.quests.QuestDatabase;
import betterquesting.utils.JsonIO;
import com.google.gson.JsonObject;
import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Event handling for standard quests and core BetterQuesting functionality
 */
public class EventHandler
{
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onKey(InputEvent.KeyInputEvent event) // Currently for debugging purposes only. Replace with proper handler later
	{
		Minecraft mc = Minecraft.getMinecraft();
		
		if(BQ_Keybindings.openQuests.isPressed())
		{
			mc.displayGuiScreen(new GuiHome(mc.currentScreen));
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
		if(!event.world.isRemote && BQ_Settings.curWorldDir != null && event.world.provider.dimensionId == 0)
		{
			JsonObject jsonQ = new JsonObject();
			QuestDatabase.writeToJson(jsonQ);
			JsonIO.WriteToFile(new File(BQ_Settings.curWorldDir, "QuestDatabase.json"), jsonQ);
			
			JsonObject jsonP = new JsonObject();
			PartyManager.writeToJson(jsonP);
			JsonIO.WriteToFile(new File(BQ_Settings.curWorldDir, "QuestingParties.json"), jsonP);
			
			JsonObject jsonPr = new JsonObject();
			QuestDatabase.writeToJson_Progression(jsonPr);
			JsonIO.WriteToFile(new File(BQ_Settings.curWorldDir, "QuestProgress.json"), jsonPr);
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
		if(event.world.isRemote || BQ_Settings.curWorldDir != null)
		{
			return;
		}
		
		MinecraftServer server = MinecraftServer.getServer();
		
		if(BetterQuesting.proxy.isClient())
		{
			BQ_Settings.curWorldDir = server.getFile("saves/" + server.getFolderName());
		} else
		{
			BQ_Settings.curWorldDir = server.getFile(server.getFolderName());
		}
    	
		// Load Questing Data
    	File f1 = new File(BQ_Settings.curWorldDir, "QuestDatabase.json");
		JsonObject j1 = new JsonObject();
		
		if(f1.exists())
		{
			j1 = JsonIO.ReadFromFile(f1);
		} else
		{
			f1 = new File(BQ_Settings.defaultDir, "DefaultQuests.json");
			
			if(f1.exists())
			{
				j1 = JsonIO.ReadFromFile(f1);
			}
		}
		
		QuestDatabase.readFromJson(j1);
    	
		// Load Progression
    	File f2 = new File(BQ_Settings.curWorldDir, "QuestProgress.json");
		JsonObject j2 = new JsonObject();
		
		if(f2.exists())
		{
			j2 = JsonIO.ReadFromFile(f2);
		}
		
		QuestDatabase.readFromJson_Progression(j2);
		
		// Load Questing Parties
	    File f3 = new File(BQ_Settings.curWorldDir, "QuestingParties.json");
	    JsonObject j3 = new JsonObject();
	    
	    if(f3.exists())
	    {
	    	j3 = JsonIO.ReadFromFile(f3);
	    }
	    
	    PartyManager.readFromJson(j3);
	    
	    BetterQuesting.logger.log(Level.INFO, "Loaded " + QuestDatabase.questDB.size() + " quest instances and " + QuestDatabase.questLines.size() + " quest lines");
	}
	
	@SubscribeEvent
	public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event)
	{
		if(!event.player.worldObj.isRemote && event.player instanceof EntityPlayerMP)
		{
			PartyManager.UpdateNameCache(false);
			QuestDatabase.SendDatabase((EntityPlayerMP)event.player);
			PartyManager.SendDatabase((EntityPlayerMP)event.player);
		}
	}
	
	@SubscribeEvent
	public void onLivingDeath(LivingDeathEvent event)
	{
		if(event.entityLiving.worldObj.isRemote)
		{
			return;
		}
	}
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onTextureStitch(TextureStitchEvent.Pre event)
	{
		if(event.map.getTextureType() == 0)
		{
			IIcon icon = event.map.registerIcon("betterquesting:fluid_placeholder");
			BetterQuesting.fluidPlaceholder.setIcons(icon);
		}
	}
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onGuiOpen(GuiOpenEvent event)
	{
		// Hook for theme GUI replacements
		event.gui = ThemeRegistry.curTheme().getGui(event.gui);
	}
}
