package betterquesting.lives;

import java.util.Date;
import net.minecraft.client.gui.GuiGameOver;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.UserListBansEntry;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.event.entity.EntityEvent.EntityConstructing;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import org.apache.logging.log4j.Level;
import betterquesting.client.gui.GuiGameOverBQ;
import betterquesting.core.BetterQuesting;
import betterquesting.network.PacketAssembly;
import betterquesting.network.PacketTypeRegistry.BQPacketType;
import betterquesting.party.PartyInstance;
import betterquesting.party.PartyManager;
import betterquesting.quests.QuestDatabase;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class LifeManager
{
	public static int defLives = 3;
	public static int maxLives = 10;
	
	/**
	 * Returns the amount of lives this player currently has to spare. Gets the party's lives if life share is enabled
	 */
	public static int getLives(EntityPlayer player)
	{
		PartyInstance party = PartyManager.GetParty(player.getUniqueID());
		
		if(party == null || !party.lifeShare)
		{
			BQ_LifeTracker tracker = BQ_LifeTracker.get(player);
			
			if(tracker == null)
			{
				BetterQuesting.logger.log(Level.WARN, "Unable to get life data for " + player.getCommandSenderName());
				return 1; // Likely an error occurred, don't kill off the player because of it
			} else
			{
				return tracker.lives;
			}
		} else
		{
			return party.lives;
		}
	}
	
	/**
	 * Sets the player's life count to the given value. Applies to party if life share is enabled
	 */
	public static void setLives(EntityPlayer player, int value)
	{
		PartyInstance party = PartyManager.GetParty(player.getUniqueID());
		
		if(party == null || !party.lifeShare)
		{
			BQ_LifeTracker tracker = BQ_LifeTracker.get(player);
			
			if(tracker != null)
			{
				tracker.lives = Math.max(0, value);
				SyncLives(player);
			}
		} else
		{
			party.lives = Math.max(0, value);
			PartyManager.UpdateClients();
		}
	}
	
	/**
	 * Changes the player's life count by the given value. Applies to party if life share is enabled
	 */
	public static void AddRemoveLives(EntityPlayer player, int value)
	{
		if(value == 0) // No change, no update
		{
			return;
		}
		
		PartyInstance party = PartyManager.GetParty(player.getUniqueID());
		
		if(party == null || !party.lifeShare)
		{
			BQ_LifeTracker tracker = BQ_LifeTracker.get(player);
			
			if(tracker != null)
			{
				tracker.lives = Math.max(0, tracker.lives + value);
				SyncLives(player);
			}
		} else
		{
			party.lives = Math.max(0, party.lives + value);
			PartyManager.UpdateClients();
		}
	}
	
	public static void SyncLives(EntityPlayer player)
	{
		if(player == null || player.worldObj.isRemote || !(player instanceof EntityPlayerMP))
		{
			return; // Can't sync null players, from client side or via non MP player
		}
		
		BQ_LifeTracker tracker = BQ_LifeTracker.get(player);
		
		if(tracker == null)
		{
			BetterQuesting.logger.log(Level.WARN, "Unable to find tracker for " + player.getCommandSenderName() + " to sync");
			return;
		}
		
		NBTTagCompound tags = new NBTTagCompound();
		NBTTagCompound data = new NBTTagCompound();
		tracker.saveNBTData(data);
		tags.setTag("data", data);
		PacketAssembly.SendTo(BQPacketType.LIFE_SYNC.GetLocation(), tags, (EntityPlayerMP)player);
	}
	
	@SubscribeEvent
	public void onEntityConstructing(EntityConstructing event)
	{
		if(event.entity instanceof EntityPlayer && BQ_LifeTracker.get((EntityPlayer)event.entity) == null)
		{
			BQ_LifeTracker.register((EntityPlayer)event.entity);
		}
	}
	
	@SubscribeEvent
	public void onLivingDeath(LivingDeathEvent event)
	{
		if(event.entityLiving.worldObj.isRemote || !QuestDatabase.bqHardcore)
		{
			return;
		}
		
		if(event.entityLiving instanceof EntityPlayer)
		{
			AddRemoveLives((EntityPlayer)event.entityLiving, -1);
		}
	}
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onGuiOpen(GuiOpenEvent event)
	{
		if(QuestDatabase.bqHardcore && event.gui != null && event.gui.getClass() == GuiGameOver.class && !(event.gui instanceof GuiGameOverBQ))
		{
			event.gui = new GuiGameOverBQ();
		}
	}
	
	@SubscribeEvent
	public void onPlayerClone(PlayerEvent.Clone event)
	{
		try
		{
			NBTTagCompound oldTags = new NBTTagCompound();
			BQ_LifeTracker.get(event.original).saveNBTData(oldTags);
			BQ_LifeTracker.get(event.entityPlayer).loadNBTData(oldTags);
		} catch(Exception e)
		{
			BetterQuesting.logger.log(Level.ERROR, "Failed to persist life data", e);
		}
	}
	
	@SubscribeEvent
	public void onPlayerRespawn(PlayerRespawnEvent event)
	{
		if(QuestDatabase.bqHardcore && event.player instanceof EntityPlayerMP && !((EntityPlayerMP)event.player).playerConqueredTheEnd)
		{
			EntityPlayerMP mpPlayer = (EntityPlayerMP)event.player;
			
			int lives = getLives(mpPlayer);
			
			if(lives <= 0)
			{
				MinecraftServer server = MinecraftServer.getServer();
				
				if(server == null)
				{
					return;
				}
	            
	            if (server.isSinglePlayer() && mpPlayer.getCommandSenderName().equals(server.getServerOwner()))
                {
                    mpPlayer.playerNetServerHandler.kickPlayerFromServer("You have died. Game over, man, it\'s game over!");
                    server.deleteWorldAndStopServer();
                }
                else
                {
                    UserListBansEntry userlistbansentry = new UserListBansEntry(mpPlayer.getGameProfile(), (Date)null, "(You just lost the game)", (Date)null, "Death in Hardcore");
                    server.getConfigurationManager().func_152608_h().func_152687_a(userlistbansentry);
                    mpPlayer.playerNetServerHandler.kickPlayerFromServer("You have died. Game over, man, it\'s game over!");
                }
			} else
			{
				if(lives == 1)
				{
					mpPlayer.addChatComponentMessage(new ChatComponentText("This is your last life!"));
				} else
				{
					mpPlayer.addChatComponentMessage(new ChatComponentText(lives + " lives remaining!"));
				}
			}
		}
	}
}
