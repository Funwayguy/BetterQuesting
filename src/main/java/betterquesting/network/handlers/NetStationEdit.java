package betterquesting.network.handlers;

import betterquesting.api.api.QuestingAPI;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api2.utils.Tuple2;
import betterquesting.blocks.TileSubmitStation;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeRegistry;
import betterquesting.questing.QuestDatabase;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.UUID;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

public class NetStationEdit {
    private static final ResourceLocation ID_NAME = new ResourceLocation("betterquesting:station_edit");

    public static void registerHandler() {
        PacketTypeRegistry.INSTANCE.registerServerHandler(ID_NAME, NetStationEdit::onServer);
    }

    @SideOnly(Side.CLIENT)
    public static void setupStation(int posX, int posY, int posZ, int questID, int taskID) {
        NBTTagCompound payload = new NBTTagCompound();
        payload.setInteger("action", 1);
        payload.setInteger("questID", questID);
        payload.setInteger("taskID", taskID);
        payload.setInteger("tilePosX", posX);
        payload.setInteger("tilePosY", posY);
        payload.setInteger("tilePosZ", posZ);
        PacketSender.INSTANCE.sendToServer(new QuestingPacket(ID_NAME, payload));
    }

    @SideOnly(Side.CLIENT)
    public static void resetStation(int posX, int posY, int posZ) {
        NBTTagCompound payload = new NBTTagCompound();
        payload.setInteger("action", 0);
        payload.setInteger("tilePosX", posX);
        payload.setInteger("tilePosY", posY);
        payload.setInteger("tilePosZ", posZ);
        PacketSender.INSTANCE.sendToServer(new QuestingPacket(ID_NAME, payload));
    }

    private static void onServer(Tuple2<NBTTagCompound, EntityPlayerMP> message) {
        NBTTagCompound data = message.getFirst();
        int px = data.getInteger("tilePosX");
        int py = data.getInteger("tilePosY");
        int pz = data.getInteger("tilePosZ");
        TileEntity tile = message.getSecond().worldObj.getTileEntity(px, py, pz);

        if (tile instanceof TileSubmitStation) {
            TileSubmitStation oss = (TileSubmitStation) tile;
            if (oss.isUseableByPlayer(message.getSecond())) {
                int action = data.getInteger("action");
                if (action == 0) {
                    oss.reset();
                } else if (action == 1) {
                    UUID QID = QuestingAPI.getQuestingUUID(message.getSecond());
                    IQuest quest = QuestDatabase.INSTANCE.getValue(data.getInteger("questID"));
                    ITask task = quest == null ? null : quest.getTasks().getValue(data.getInteger("taskID"));
                    if (quest != null && task != null) oss.setupTask(QID, quest, task);
                }
            }
        }
    }
}
