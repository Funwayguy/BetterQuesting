package betterquesting.network.handlers;

import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.events.DatabaseEvent;
import betterquesting.api.events.DatabaseEvent.DBType;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.rewards.IReward;
import betterquesting.core.BetterQuesting;
import betterquesting.questing.rewards.RewardChoice;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

public class NetRewardChoice
{
	private static final ResourceLocation ID_NAME = new ResourceLocation("bq_standard:choice_reward");
	
	public static void registerHandler()
    {
        QuestingAPI.getAPI(ApiReference.PACKET_REG).registerServerHandler(ID_NAME, NetRewardChoice::onServer);
    
        if(BetterQuesting.proxy.isClient())
        {
            QuestingAPI.getAPI(ApiReference.PACKET_REG).registerClientHandler(ID_NAME, NetRewardChoice::onClient);
        }
    }
    
    @SideOnly(Side.CLIENT)
    public static void requestChoice(int questID, int rewardID, int index)
    {
        NBTTagCompound payload = new NBTTagCompound();
        payload.setInteger("questID", questID);
        payload.setInteger("rewardID", rewardID);
        payload.setInteger("selection", index);
        QuestingAPI.getAPI(ApiReference.PACKET_SENDER).sendToServer(new QuestingPacket(ID_NAME, payload));
    }
    
    public static void sendChoice(@Nonnull EntityPlayerMP player, int questID, int rewardID, int index)
    {
        NBTTagCompound payload = new NBTTagCompound();
        payload.setInteger("questID", questID);
        payload.setInteger("rewardID", rewardID);
        payload.setInteger("selection", index);
        QuestingAPI.getAPI(ApiReference.PACKET_SENDER).sendToPlayers(new QuestingPacket(ID_NAME, payload), player);
    }
	
	private static void onServer(Tuple<NBTTagCompound, EntityPlayerMP> message)
	{
	    EntityPlayerMP sender = message.getSecond();
	    NBTTagCompound tag = message.getFirst();
	    
		int qID = tag.hasKey("questID")? tag.getInteger("questID") : -1;
		int rID = tag.hasKey("rewardID")? tag.getInteger("rewardID") : -1;
		int sel = tag.hasKey("selection")? tag.getInteger("selection") : -1;
		
		if(qID < 0 || rID < 0) return;
		
		IQuest quest = QuestingAPI.getAPI(ApiReference.QUEST_DB).getValue(qID);
		IReward reward = quest == null? null : quest.getRewards().getValue(rID);
		
		if(reward instanceof RewardChoice)
		{
			RewardChoice rChoice = (RewardChoice)reward;
			rChoice.setSelection(QuestingAPI.getQuestingUUID(sender), sel);
			sendChoice(sender, qID, rID, sel);
		}
	}
	
	@SideOnly(Side.CLIENT)
	private static void onClient(NBTTagCompound message)
	{
		EntityPlayerSP player = Minecraft.getMinecraft().player;
		
		int qID = message.hasKey("questID", 99)? message.getInteger("questID") : -1;
		int rID = message.hasKey("rewardID", 99)? message.getInteger("rewardID") : -1;
		int sel = message.hasKey("selection", 99)? message.getInteger("selection") : -1;
		
		if(qID < 0 || rID < 0) return;
		
		IQuest quest = QuestingAPI.getAPI(ApiReference.QUEST_DB).getValue(qID);
		IReward reward = quest == null? null : quest.getRewards().getValue(rID);
		
		if(reward instanceof RewardChoice)
		{
		    ((RewardChoice)reward).setSelection(QuestingAPI.getQuestingUUID(player), sel);
		    MinecraftForge.EVENT_BUS.post(new DatabaseEvent.Update(DBType.QUEST));
            //MinecraftForge.EVENT_BUS.post(new Update());
        }
	}
}
