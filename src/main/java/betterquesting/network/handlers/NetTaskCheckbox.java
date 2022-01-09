package betterquesting.network.handlers;

import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api2.cache.CapabilityProviderQuestCache;
import betterquesting.api2.cache.QuestCache;
import betterquesting.questing.tasks.TaskCheckbox;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class NetTaskCheckbox
{
	private static final ResourceLocation ID_NAME = new ResourceLocation("bq_standard:task_checkbox");
	
	public static void registerHandler()
    {
        QuestingAPI.getAPI(ApiReference.PACKET_REG).registerServerHandler(ID_NAME, NetTaskCheckbox::onServer);
    }
    
    @SideOnly(Side.CLIENT)
    public static void requestClick(int questID, int taskID)
    {
        NBTTagCompound payload = new NBTTagCompound();
        payload.setInteger("questID", questID);
        payload.setInteger("taskID", taskID);
        QuestingAPI.getAPI(ApiReference.PACKET_SENDER).sendToServer(new QuestingPacket(ID_NAME, payload));
    }
    
	private static void onServer(Tuple<NBTTagCompound, EntityPlayerMP> message)
	{
	    NBTTagCompound data = message.getFirst();
	    EntityPlayerMP sender = message.getSecond();
	    
		int qId = !data.hasKey("questID", 99)? -1 : data.getInteger("questID");
		int tId = !data.hasKey("taskID", 99)? -1 : data.getInteger("taskID");
		
		if(qId >= 0 && tId >= 0)
		{
            QuestCache qc = sender.getCapability(CapabilityProviderQuestCache.CAP_QUEST_CACHE, null);
            IQuest quest = QuestingAPI.getAPI(ApiReference.QUEST_DB).getValue(qId);
            ITask task = quest == null ? null : quest.getTasks().getValue(tId);
            
            if(task instanceof TaskCheckbox)
            {
                task.setComplete(QuestingAPI.getQuestingUUID(sender));
                if(qc != null) qc.markQuestDirty(qId);
            }
		}
	}
}
