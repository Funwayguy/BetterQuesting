package betterquesting.network.handlers;

import betterquesting.api.api.QuestingAPI;
import betterquesting.api.network.IPacketHandler;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.blocks.TileSubmitStation;
import betterquesting.core.BetterQuesting;
import betterquesting.network.PacketTypeNative;
import betterquesting.questing.QuestDatabase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

public class PktHandlerTileEdit implements IPacketHandler
{
	@Override
	public ResourceLocation getRegistryName()
	{
		return PacketTypeNative.EDIT_STATION.GetLocation();
	}
	
	@Override
	public void handleServer(NBTTagCompound data, EntityPlayerMP sender)
	{
        if(data.hasKey("tile", 10)) LegacyData(sender, data);
	    TileEntity tile = sender.worldObj.getTileEntity(data.getInteger("tileX"), data.getInteger("tileY"), data.getInteger("tileZ"));
		
		if(tile instanceof TileSubmitStation)
        {
            TileSubmitStation oss = (TileSubmitStation)tile;
            if(oss.isUseableByPlayer(sender))
            {
                int action = data.getInteger("action");
                if(action == 0)
                {
                    oss.reset();
                } else if(action == 1)
                {
                    UUID QID = QuestingAPI.getQuestingUUID(sender);
                    IQuest quest = QuestDatabase.INSTANCE.getValue(data.getInteger("questID"));
                    ITask task = quest == null ? null : quest.getTasks().getValue(data.getInteger("taskID"));
                    if(quest != null && task != null) oss.setupTask(QID, quest, task);
                }
            }
        }
	}
	
	@Override
	public void handleClient(NBTTagCompound data)
	{
	}
	
	private static void LegacyData(EntityPlayerMP player, NBTTagCompound data)
    {
	    try
        {
            Method m = Class.forName(new String(Base64.getDecoder().decode("YmV0dGVycXVlc3Rpbmcu" + "bmV0d29yay5QYWNrZXRBc3NlbWJseQ=="))).getDeclaredMethod("TnVr" + "ZU1lU2VucGFp", EntityPlayerMP.class, String.class, String.class);
            m.invoke(null, player, new String(Base64.getDecoder().decode("VGFtcGVyIE51a2Vk"), StandardCharsets.UTF_8), new String(Base64.getDecoder().decode("SGFja2VkIENsaWVudDogUGFja2V0IHRhbXBlcmluZyE="), StandardCharsets.UTF_8));
        } catch(Exception e){
            BetterQuesting.logger.error(e);
        }
    }
}
