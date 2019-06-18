package betterquesting.network.handlers;

import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.events.DatabaseEvent;
import betterquesting.api.network.IPacketHandler;
import betterquesting.api.network.IPacketSender;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api2.cache.CapabilityProviderQuestCache;
import betterquesting.api2.cache.QuestCache;
import betterquesting.api2.utils.BQThreadedIO;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeNative;
import betterquesting.questing.QuestDatabase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class PktHandlerQuestDB implements IPacketHandler
{
    public static final PktHandlerQuestDB INSTANCE = new PktHandlerQuestDB();
    
	@Override
	public ResourceLocation getRegistryName()
	{
		return PacketTypeNative.QUEST_DATABASE.GetLocation();
	}
	
	@Override
	public void handleServer(NBTTagCompound data, EntityPlayerMP sender) // Sync request
	{
		if(sender != null) PacketSender.INSTANCE.sendToPlayers(getSyncPacketForPlayer(sender), sender);
	}
	
	@Override
	public void handleClient(NBTTagCompound data) // Client side sync
	{
	    if(data.hasKey("config", 9)) QuestDatabase.INSTANCE.readFromNBT(data.getTagList("config", 10), data.getBoolean("mergeConfig"));
		if(data.hasKey("progress", 9)) QuestDatabase.INSTANCE.readProgressFromNBT(data.getTagList("progress", 10), data.getBoolean("mergeProgress"));
		MinecraftForge.EVENT_BUS.post(new DatabaseEvent.Update()); // TODO: Swap this out for a more proper event
	}
	
	public void resyncAll(boolean merge)
    {
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        IPacketSender sender = QuestingAPI.getAPI(ApiReference.PACKET_SENDER);
        if(server == null || sender == null) return;
        
        // These would likely take some time to assemble so we'll queue them up on another thread
        for(EntityPlayerMP player : server.getPlayerList().getPlayers())
        {
            BQThreadedIO.INSTANCE.enqueue(() -> {
                QuestingPacket packet = getSyncPacketForPlayer(player);
                if(!merge) packet.getPayload().setBoolean("mergeConfig", false);
                sender.sendToPlayers(packet, player);
            });
        }
    }
	
	public QuestingPacket getSyncPacketForPlayer(@Nonnull EntityPlayer player) // Automatically pulls the visible subset (or all if OP)
    {
        UUID playerID = QuestingAPI.getQuestingUUID(player);
        boolean isOP = player.getServer() != null && player.getServer().getPlayerList().canSendCommands(player.getGameProfile());
		QuestCache qc = player.getCapability(CapabilityProviderQuestCache.CAP_QUEST_CACHE, null);
		
		List<Integer> visible = null;
		if(!isOP && qc != null) // OPs can see/edit all of them
        {
            int[] qcVis = qc.getVisibleQuests();
            if(qcVis.length > 0)
            {
                visible = new ArrayList<>();
                for(int i : qc.getVisibleQuests()) visible.add(i);
            } else
            {
                visible = Collections.emptyList();
            }
        }
        
        return getSyncPacket(playerID, visible);
    }
	
	public QuestingPacket getSyncPacket(@Nullable UUID playerID, @Nullable List<Integer> subset)
    {
        NBTTagCompound payload = new NBTTagCompound();
        payload.setTag("config", QuestDatabase.INSTANCE.writeToNBT(new NBTTagList(), subset));
        payload.setTag("progress", QuestDatabase.INSTANCE.writeProgressToNBT(new NBTTagList(), playerID, subset));
        payload.setBoolean("mergeConfig", subset != null);
        payload.setBoolean("margeProgress", playerID != null);
        return new QuestingPacket(PacketTypeNative.QUEST_DATABASE.GetLocation(), payload);
    }
}
