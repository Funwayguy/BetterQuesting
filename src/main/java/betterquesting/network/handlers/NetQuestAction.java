package betterquesting.network.handlers;

import betterquesting.api.network.QuestingPacket;
import betterquesting.api.questing.IQuest;
import betterquesting.api2.storage.DBEntry;
import betterquesting.core.BetterQuesting;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeRegistry;
import betterquesting.questing.QuestDatabase;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.Level;

import javax.annotation.Nonnull;
import java.util.List;

public class NetQuestAction
{
    private static final ResourceLocation ID_NAME = new ResourceLocation("betterquesting:quest_action");
    
    public static void registerHandler()
    {
        PacketTypeRegistry.INSTANCE.registerServerHandler(ID_NAME, NetQuestAction::onServer);
    }
    
    @OnlyIn(Dist.CLIENT)
    public static void requestClaim(@Nonnull int[] questIDs)
    {
        if(questIDs.length <= 0) return;
        CompoundNBT payload = new CompoundNBT();
        payload.putInt("action", 0);
        payload.putIntArray("questIDs", questIDs);
        PacketSender.INSTANCE.sendToServer(new QuestingPacket(ID_NAME, payload));
    }
    
    @OnlyIn(Dist.CLIENT)
    public static void requestDetect(@Nonnull int[] questIDs)
    {
        if(questIDs.length <= 0) return;
        CompoundNBT payload = new CompoundNBT();
        payload.putInt("action", 1);
        payload.putIntArray("questIDs", questIDs);
        PacketSender.INSTANCE.sendToServer(new QuestingPacket(ID_NAME, payload));
    }
    
    private static void onServer(Tuple<CompoundNBT, ServerPlayerEntity> message)
    {
		int action = !message.getA().contains("action", 99) ? -1 : message.getA().getInt("action");
		
		switch(action)
        {
            case 0:
            {
                claimQuest(message.getA().getIntArray("questIDs"), message.getB());
                break;
            }
            case 1:
            {
                detectQuest(message.getA().getIntArray("questIDs"), message.getB());
                break;
            }
            default:
            {
                BetterQuesting.logger.log(Level.ERROR, "Invalid quest user action '" + action + "'. Full payload:\n" + message.getA().toString());
            }
        }
    }
    
    public static void claimQuest(int[] questIDs, ServerPlayerEntity player)
    {
        List<DBEntry<IQuest>> qLists = QuestDatabase.INSTANCE.bulkLookup(questIDs);
        
        for(DBEntry<IQuest> entry : qLists)
        {
            if(!entry.getValue().canClaim(player)) continue;
            entry.getValue().claimReward(player);
        }
    }
    
    public static void detectQuest(int[] questIDs, ServerPlayerEntity player)
    {
        List<DBEntry<IQuest>> qLists = QuestDatabase.INSTANCE.bulkLookup(questIDs);
        
        for(DBEntry<IQuest> entry : qLists)
        {
            entry.getValue().detect(player);
        }
    }
}
