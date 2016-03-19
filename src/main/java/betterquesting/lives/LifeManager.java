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
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Level;
import betterquesting.client.gui.GuiGameOverBQ;
import betterquesting.core.BetterQuesting;
import betterquesting.network.PacketQuesting.PacketDataType;
import betterquesting.party.PartyInstance;
import betterquesting.party.PartyManager;
import betterquesting.quests.QuestDatabase;

public class LifeManager
{
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
				BetterQuesting.logger.log(Level.WARN, "Unable to get life data for " + player.getName());
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
				BetterQuesting.logger.log(Level.INFO, "Player Lives = " + tracker.lives + " + " + value);
				tracker.lives = Math.max(0, tracker.lives + value);
				BetterQuesting.logger.log(Level.INFO, "New value = " + tracker.lives);
				SyncLives(player);
			}
		} else
		{
			BetterQuesting.logger.log(Level.INFO, "Party Lives = " + party.lives + " + " + value);
			party.lives = Math.max(0, party.lives + value);
			BetterQuesting.logger.log(Level.INFO, "New value = " + party.lives);
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
			BetterQuesting.logger.log(Level.WARN, "Unable to find tracker for " + player.getName() + " to sync");
			return;
		}
		
		NBTTagCompound tags = new NBTTagCompound();
		NBTTagCompound data = new NBTTagCompound();
		tracker.saveNBTData(data);
		tags.setTag("data", data);
		BetterQuesting.instance.network.sendTo(PacketDataType.LIFE_SYNC.makePacket(tags), (EntityPlayerMP)player);
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
	            
	            if (server.isSinglePlayer() && mpPlayer.getName().equals(server.getServerOwner()))
                {
                    mpPlayer.playerNetServerHandler.kickPlayerFromServer("You have died. Game over, man, it\'s game over!");
                    server.deleteWorldAndStopServer();
                }
                else
                {
                    UserListBansEntry userlistbansentry = new UserListBansEntry(mpPlayer.getGameProfile(), (Date)null, "(You just lost the game)", (Date)null, "Death in Hardcore");
                    server.getConfigurationManager().getBannedPlayers().addEntry(userlistbansentry);
                    mpPlayer.playerNetServerHandler.kickPlayerFromServer("You have died. Game over, man, it\'s game over!");
                }
			} else
			{
				if(lives == 1)
				{
					mpPlayer.addChatComponentMessage(new ChatComponentText("This is your last life!"));
				} else
				{
					mpPlayer.addChatComponentMessage(new ChatComponentText(lives + " live(s) remaining!"));
				}
			}
		}
	}
}
