package betterquesting.network.handlers;

import betterquesting.api.api.QuestingAPI;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.blocks.TileSubmitStation;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeRegistry;
import betterquesting.questing.QuestDatabase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.UUID;

public class NetStationEdit
{
    private static final ResourceLocation ID_NAME = new ResourceLocation("betterquesting:station_edit");
    
    public static void registerHandler()
    {
        PacketTypeRegistry.INSTANCE.registerServerHandler(ID_NAME, NetStationEdit::onServer);
    }
    
    @SideOnly(Side.CLIENT)
    public static void setupStation(BlockPos pos, int questID, int taskID)
    {
        NBTTagCompound payload = new NBTTagCompound();
        payload.setInteger("action", 1);
        payload.setInteger("questID", questID);
        payload.setInteger("task", taskID);
        payload.setLong("tilePos", pos.toLong());
        PacketSender.INSTANCE.sendToServer(new QuestingPacket(ID_NAME, payload));
    }
    
    @SideOnly(Side.CLIENT)
    public static void resetStation(BlockPos pos)
    {
        NBTTagCompound payload = new NBTTagCompound();
        payload.setInteger("action", 0);
        payload.setLong("tilePos", pos.toLong());
        PacketSender.INSTANCE.sendToServer(new QuestingPacket(ID_NAME, payload));
    }
    
    private static void onServer(Tuple<NBTTagCompound, EntityPlayerMP> message)
    {
        NBTTagCompound data = message.getFirst();
        BlockPos pos = BlockPos.fromLong(data.getLong("tilePos"));
	    TileEntity tile = message.getSecond().world.getTileEntity(pos);
		
		if(tile instanceof TileSubmitStation)
        {
            TileSubmitStation oss = (TileSubmitStation)tile;
            if(oss.isUsableByPlayer(message.getSecond()))
            {
                int action = data.getInteger("action");
                if(action == 0)
                {
                    oss.reset();
                } else if(action == 1)
                {
                    UUID QID = QuestingAPI.getQuestingUUID(message.getSecond());
                    IQuest quest = QuestDatabase.INSTANCE.getValue(data.getInteger("questID"));
                    ITask task = quest == null ? null : quest.getTasks().getValue(data.getInteger("taskID"));
                    if(quest != null && task != null) oss.setupTask(QID, quest, task);
                }
            }
        }
    }
}
