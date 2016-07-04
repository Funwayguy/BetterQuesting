package betterquesting.lives;

import net.minecraft.client.gui.GuiGameOver;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.GameType;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Level;
import betterquesting.client.gui.GuiGameOverBQ;
import betterquesting.core.BetterQuesting;
import betterquesting.network.PacketAssembly;
import betterquesting.network.PacketTypeRegistry.BQPacketType;
import betterquesting.party.PartyInstance;
import betterquesting.party.PartyManager;
import betterquesting.quests.QuestDatabase;

public class LifeManager
{
	public static int defLives = 3;
	public static int maxLives = 10;
	
	@CapabilityInject(IHardcoreLives.class)
	public static final Capability<IHardcoreLives> LIFE_CAP = null;
	public static final ResourceLocation LIFE_ID = new ResourceLocation(BetterQuesting.MODID + ":BQ_LIVES");
	
	/**
	 * Returns the amount of lives this player currently has to spare. Gets the party's lives if life share is enabled
	 */
	public static int getLives(EntityPlayer player)
	{
		PartyInstance party = PartyManager.GetParty(player.getUniqueID());
		
		if(party == null || !party.lifeShare)
		{
			IHardcoreLives tracker = player.getCapability(LIFE_CAP, null);
			
			if(tracker == null)
			{
				BetterQuesting.logger.log(Level.WARN, "Unable to get life data for " + player.getName());
				return 1; // Likely an error occurred, don't kill off the player because of it
			} else
			{
				return tracker.getLives();
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
			IHardcoreLives tracker = player.getCapability(LIFE_CAP, null);
			
			if(tracker != null)
			{
				tracker.setLives(Math.max(0, value));
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
			IHardcoreLives tracker = player.getCapability(LIFE_CAP, null);
			
			if(tracker != null)
			{
				tracker.setLives(Math.max(0, tracker.getLives() + value));
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
		
		IHardcoreLives tracker = player.getCapability(LIFE_CAP, null);
		
		if(tracker == null)
		{
			BetterQuesting.logger.log(Level.WARN, "Unable to find tracker for " + player.getName() + " to sync");
			return;
		}
		
		NBTTagCompound tags = new NBTTagCompound();
		tags.setTag("data", tracker.writeToNBT());
		PacketAssembly.SendTo(BQPacketType.LIFE_SYNC.GetLocation(), tags, (EntityPlayerMP)player);
	}
	
	@SubscribeEvent
	public void getCapabilities(AttachCapabilitiesEvent.Entity event)
	{
		event.addCapability(LIFE_ID, new LifeCapability());
	}
	
	@SubscribeEvent
	public void onLivingDeath(LivingDeathEvent event)
	{
		if(event.getEntityLiving().worldObj.isRemote || !QuestDatabase.bqHardcore)
		{
			return;
		}
		
		if(event.getEntityLiving() instanceof EntityPlayer)
		{
			AddRemoveLives((EntityPlayer)event.getEntityLiving(), -1);
		}
	}
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onGuiOpen(GuiOpenEvent event)
	{
		if(QuestDatabase.bqHardcore && event.getGui() != null && event.getGui().getClass() == GuiGameOver.class && !(event.getGui() instanceof GuiGameOverBQ))
		{
			ITextComponent cod = ObfuscationReflectionHelper.getPrivateValue(GuiGameOver.class, (GuiGameOver)event.getGui(), "field_184871_f");
			event.setGui(new GuiGameOverBQ(cod));
		}
	}
	
	@SubscribeEvent
	public void onPlayerClone(PlayerEvent.Clone event)
	{
		try
		{
			NBTTagCompound nbt = event.getOriginal().getCapability(LIFE_CAP, null).writeToNBT();
			event.getEntityPlayer().getCapability(LIFE_CAP, null).readFromNBT(nbt);
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
				MinecraftServer server = mpPlayer.getServer();
				
				if(server == null)
				{
					return;
				}
	            
	            mpPlayer.setGameType(GameType.SPECTATOR);
	            mpPlayer.getServerWorld().getGameRules().setOrCreateGameRule("spectatorsGenerateChunks", "false");
			} else
			{
				if(lives == 1)
				{
					mpPlayer.addChatComponentMessage(new TextComponentString("This is your last life!"));
				} else
				{
					mpPlayer.addChatComponentMessage(new TextComponentString(lives + " lives remaining!"));
				}
			}
		}
	}
}
