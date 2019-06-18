package betterquesting.network.handlers;

import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.events.DatabaseEvent;
import betterquesting.api.network.IPacketHandler;
import betterquesting.api.network.IPacketSender;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.questing.IQuest;
import betterquesting.api2.cache.QuestCache;
import betterquesting.api2.storage.DBEntry;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeNative;
import betterquesting.questing.QuestDatabase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public class PktHandlerQuestSync implements IPacketHandler
{
    public static final PktHandlerQuestSync INSTANCE = new PktHandlerQuestSync();
    
	@Override
	public ResourceLocation getRegistryName()
	{
		return PacketTypeNative.QUEST_SYNC.GetLocation();
	}
	
	@Override
	public void handleServer(NBTTagCompound data, EntityPlayerMP sender) // Sync request
	{
	    if(sender == null || data == null) return;
	    int questID = !data.hasKey("questID", 99)? -1 : data.getInteger("questID");
	    IQuest quest = QuestDatabase.INSTANCE.getValue(questID);
	    if(quest == null) return;
	    
        PacketSender.INSTANCE.sendToPlayers(getSyncPacket(QuestingAPI.getQuestingUUID(sender), new DBEntry<>(questID, quest)), sender);
	}
	
	@Override
	public void handleClient(NBTTagCompound data) // Sync response
	{
		int questID = !data.hasKey("questID", 99)? -1 : data.getInteger("questID");
		IQuest quest = QuestDatabase.INSTANCE.getValue(questID);
		if(quest == null) quest = QuestDatabase.INSTANCE.createNew(questID); // Server says this quest exists
		
		if(data.hasKey("config", 10)) quest.readFromNBT(data.getCompoundTag("config"));
		if(data.hasKey("progress", 10)) quest.readProgressFromNBT(data.getCompoundTag("progress"), data.getBoolean("merge"));
		
		MinecraftForge.EVENT_BUS.post(new DatabaseEvent.Update()); // TODO: Swap this out for a more proper event
	}
    
	public QuestingPacket getSyncPacket(@Nullable UUID playerID, @Nonnull DBEntry<IQuest> quest)
    {
        NBTTagCompound payload = new NBTTagCompound();
        payload.setTag("config", quest.getValue().writeToNBT(new NBTTagCompound()));
        payload.setTag("progress", quest.getValue().writeProgressToNBT(new NBTTagCompound(), playerID, null));
        payload.setBoolean("merge", playerID != null);
        payload.setInteger("questID", quest.getID());
        return new QuestingPacket(PacketTypeNative.QUEST_SYNC.GetLocation(), payload);
    }
    
    // Sends a sync packet to every player (personalised to keep individual packet size ow)
    public void resyncAll(@Nonnull DBEntry<IQuest> quest)
    {
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        IPacketSender sender = QuestingAPI.getAPI(ApiReference.PACKET_SENDER);
        if(server == null || sender == null) return;
        
        for(EntityPlayerMP player : server.getPlayerList().getPlayers())
        {
            UUID uuid = QuestingAPI.getQuestingUUID(player);
            if(!QuestCache.isQuestShown(quest.getValue(), uuid, player)) continue;
            sender.sendToPlayers(getSyncPacket(uuid, quest), player);
        }
    }
}
