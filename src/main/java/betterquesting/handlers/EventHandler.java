package betterquesting.handlers;

import betterquesting.api.api.QuestingAPI;
import betterquesting.api.client.gui.misc.INeedsRefresh;
import betterquesting.api.events.DatabaseEvent;
import betterquesting.api.placeholders.FluidPlaceholder;
import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.party.IParty;
import betterquesting.api.storage.BQ_Settings;
import betterquesting.api2.cache.CapabilityProviderQuestCache;
import betterquesting.api2.cache.QuestCache.QResetTime;
import betterquesting.api2.client.gui.GuiScreenTest;
import betterquesting.api2.storage.DBEntry;
import betterquesting.client.BQ_Keybindings;
import betterquesting.client.gui2.GuiHome;
import betterquesting.core.BetterQuesting;
import betterquesting.network.PacketSender;
import betterquesting.questing.QuestDatabase;
import betterquesting.questing.QuestLineDatabase;
import betterquesting.questing.party.PartyManager;
import betterquesting.storage.LifeDatabase;
import betterquesting.storage.NameCache;
import betterquesting.storage.QuestSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.GameType;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.Clone;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Event handling for standard quests and core BetterQuesting functionality
 */
public class EventHandler
{
	public static final EventHandler INSTANCE = new EventHandler();
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onKey(InputEvent.KeyInputEvent event)
	{
		Minecraft mc = Minecraft.getMinecraft();
		
		if(BQ_Keybindings.openQuests.isPressed())
		{
			if(mc.player.isSneaking() && mc.player.getName().equalsIgnoreCase("Funwayguy"))
			{
				mc.displayGuiScreen(new GuiScreenTest(mc.currentScreen));
			} else
			{
				if(BQ_Settings.useBookmark && GuiHome.bookmark != null)
				{
					mc.displayGuiScreen(GuiHome.bookmark);
				} else
				{
					mc.displayGuiScreen(new GuiHome(mc.currentScreen));
				}
			}
		}
	}
	
	@SubscribeEvent
    public void onCapabilityPlayer(AttachCapabilitiesEvent<Entity> event)
    {
        if(!(event.getObject() instanceof EntityPlayer)) return;
        event.addCapability(CapabilityProviderQuestCache.LOC_QUEST_CACHE, new CapabilityProviderQuestCache());
        System.out.println("Capability QUEST_CACHE attached to player");
    }
    
    @SubscribeEvent
    public void onPlayerClone(Clone event)
    {
        betterquesting.api2.cache.QuestCache oCache = event.getOriginal().getCapability(CapabilityProviderQuestCache.CAP_QUEST_CACHE, null);
        betterquesting.api2.cache.QuestCache nCache = event.getEntityPlayer().getCapability(CapabilityProviderQuestCache.CAP_QUEST_CACHE, null);
        
        if(oCache != null && nCache != null) nCache.deserializeNBT(oCache.serializeNBT());
    }
	
	@SubscribeEvent
	public void onLivingUpdate(LivingUpdateEvent event)
	{
		if(event.getEntityLiving().world.isRemote)
		{
			return;
		}
		
		if(event.getEntityLiving() instanceof EntityPlayer)
		{
			if(event.getEntityLiving().ticksExisted%20 != 0 || QuestSettings.INSTANCE.getProperty(NativeProps.EDIT_MODE)) return; // Only triggers once per second
			
			EntityPlayer player = (EntityPlayer)event.getEntityLiving();
            betterquesting.api2.cache.QuestCache qc = player.getCapability(CapabilityProviderQuestCache.CAP_QUEST_CACHE, null);
            
            if(qc == null) return;
            
            List<DBEntry<IQuest>> activeQuests = QuestDatabase.INSTANCE.bulkLookup(qc.getActiveQuests()); // TODO: Replace with quests marked dirty when functionality is implemented
            List<DBEntry<IQuest>> pendingAutoClaims = QuestDatabase.INSTANCE.bulkLookup(qc.getPendingAutoClaims());
            QResetTime[] pendingResets = qc.getScheduledResets();
            
            List<DBEntry<IQuest>> syncMe = new ArrayList<>();
			
			UUID uuid = QuestingAPI.getQuestingUUID(player);
			boolean refreshCache = false;
			
			if(player.ticksExisted%100 == 0) // Passive quest state check every 5 seconds
            {
                for(DBEntry<IQuest> quest : activeQuests)
                {
                    quest.getValue().update(player);
                    
                    if(quest.getValue().isComplete(uuid))
                    {
                        refreshCache = true;
                        if(!syncMe.contains(quest)) syncMe.add(quest);
                    }
                }
            }
            
            if(player.getServer() != null) // Repeatable quest resets
            {
                for(QResetTime rTime : pendingResets)
                {
                    if(player.getServer().getWorld(0).getTotalWorldTime() >= rTime.time)
                    {
                        IQuest entry = QuestDatabase.INSTANCE.getValue(rTime.questID);
                        
                        if(entry.getProperty(NativeProps.GLOBAL))
                        {
                            entry.resetAll(false);
                        } else
                        {
                            entry.resetUser(uuid, false);
                        }
                        
                        refreshCache = true;
                        DBEntry<IQuest> dbe = new DBEntry<>(rTime.questID, entry);
                        if(!syncMe.contains(dbe)) syncMe.add(dbe);
                    }
                }
            }
            
            for(DBEntry<IQuest> entry : pendingAutoClaims) // Auto claims
            {
                if(entry.getValue().canClaim(player))
                {
                    entry.getValue().claimReward(player);
                    refreshCache = true;
                    if(!syncMe.contains(entry)) syncMe.add(entry);
                }
            }
            
            if(refreshCache || player.ticksExisted % 200 == 0)
            {
                qc.updateCache(player);
            }
            
            // TODO: Check partial data writes from here when fully implemented
            for(DBEntry<IQuest> entry : syncMe)
            {
                if(entry.getValue().getProperty(NativeProps.GLOBAL))
                {
                    PacketSender.INSTANCE.sendToAll(entry.getValue().getSyncPacket());
                } else if(player instanceof EntityPlayerMP)
                {
                    IParty party = PartyManager.INSTANCE.getUserParty(uuid);
                    
                    if(party != null && player.getServer() != null)
                    {
                        for(UUID memID : party.getMembers()) // Send to party only
                        {
                            EntityPlayerMP memPlayer = player.getServer().getPlayerList().getPlayerByUUID(memID);
                            if(memPlayer != null) PacketSender.INSTANCE.sendToPlayer(entry.getValue().getSyncPacket(), memPlayer);
                        }
                    } else
                    {
                        PacketSender.INSTANCE.sendToPlayer(entry.getValue().getSyncPacket(), (EntityPlayerMP)player);
                    }
                }
            }
		}
	}
	
	@SubscribeEvent
	public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event)
	{
		if(event.getModID().equals(BetterQuesting.MODID))
		{
			ConfigHandler.config.save();
			ConfigHandler.initConfigs();
		}
	}
	
	@SubscribeEvent
	public void onWorldSave(WorldEvent.Save event)
	{
		if(!event.getWorld().isRemote && BQ_Settings.curWorldDir != null && event.getWorld().provider.getDimension() == 0)
		{
			SaveLoadHandler.INSTANCE.saveDatabases();
		}
	}
	
	@SubscribeEvent
	public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event)
	{
		if(event.player.world.isRemote || event.player.getServer() == null || !(event.player instanceof EntityPlayerMP))
		{
			return;
		} else if(BetterQuesting.proxy.isClient() && !event.player.getServer().isDedicatedServer() && event.player.getServer().getServerOwner().equals(event.player.getGameProfile().getName()))
		{
			return;
		}
		
		EntityPlayerMP mpPlayer = (EntityPlayerMP)event.player;
		
		NameCache.INSTANCE.updateNames(event.player.getServer());
		
		PacketSender.INSTANCE.sendToPlayer(QuestSettings.INSTANCE.getSyncPacket(), mpPlayer);
		PacketSender.INSTANCE.sendToPlayer(QuestDatabase.INSTANCE.getSyncPacket(), mpPlayer);
		PacketSender.INSTANCE.sendToPlayer(QuestLineDatabase.INSTANCE.getSyncPacket(), mpPlayer);
		PacketSender.INSTANCE.sendToPlayer(LifeDatabase.INSTANCE.getSyncPacket(), mpPlayer);
		PacketSender.INSTANCE.sendToPlayer(PartyManager.INSTANCE.getSyncPacket(), mpPlayer);
	}
	
	@SubscribeEvent
	public void onPlayerRespawn(PlayerRespawnEvent event)
	{
		if(QuestSettings.INSTANCE.getProperty(NativeProps.HARDCORE) && event.player instanceof EntityPlayerMP && !((EntityPlayerMP)event.player).queuedEndExit)
		{
			EntityPlayerMP mpPlayer = (EntityPlayerMP)event.player;
			
			IParty party = PartyManager.INSTANCE.getUserParty(QuestingAPI.getQuestingUUID(mpPlayer));
			int lives = (party == null || !party.getProperties().getProperty(NativeProps.PARTY_LIVES))? LifeDatabase.INSTANCE.getLives(QuestingAPI.getQuestingUUID(mpPlayer)) : LifeDatabase.INSTANCE.getLives(party);
			
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
					mpPlayer.sendStatusMessage(new TextComponentString("This is your last life!"), true);
				} else
				{
					mpPlayer.sendStatusMessage(new TextComponentString(lives + " lives remaining!"), true);
				}
			}
		}
	}
	
	@SubscribeEvent
	public void onLivingDeath(LivingDeathEvent event)
	{
		if(event.getEntityLiving().world.isRemote || !QuestSettings.INSTANCE.getProperty(NativeProps.HARDCORE))
		{
			return;
		}
		
		if(event.getEntityLiving() instanceof EntityPlayer)
		{
			UUID uuid = QuestingAPI.getQuestingUUID(((EntityPlayer)event.getEntityLiving()));
			IParty party = PartyManager.INSTANCE.getUserParty(uuid);
			
			if(party == null || !party.getProperties().getProperty(NativeProps.PARTY_LIVES))
			{
				int lives = LifeDatabase.INSTANCE.getLives(uuid);
				LifeDatabase.INSTANCE.setLives(uuid, lives - 1);
			} else
			{
				int lives = LifeDatabase.INSTANCE.getLives(party);
				LifeDatabase.INSTANCE.setLives(party, lives - 1);
			}
		}
	}
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onTextureStitch(TextureStitchEvent.Pre event)
	{
		if(event.getMap() == Minecraft.getMinecraft().getTextureMapBlocks())
		{
			event.getMap().registerSprite(FluidPlaceholder.fluidPlaceholder.getStill());
		}
	}
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onDataUpdated(DatabaseEvent.Update event)
	{
		GuiScreen screen = Minecraft.getMinecraft().currentScreen;
		
		if(screen instanceof INeedsRefresh)
		{
			((INeedsRefresh)screen).refreshGui();
		}
	}
	
	@SubscribeEvent
	public void onCommand(CommandEvent event)
	{
		MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
		
		if(server != null && (event.getCommand().getName().equalsIgnoreCase("op") || event.getCommand().getName().equalsIgnoreCase("deop")))
		{
			NameCache.INSTANCE.updateNames(server);
		}
	}
}
