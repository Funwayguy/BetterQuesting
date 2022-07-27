package betterquesting.network.handlers;

import betterquesting.api.api.QuestingAPI;
import betterquesting.api.events.DatabaseEvent;
import betterquesting.api.events.DatabaseEvent.DBType;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.questing.IQuest;
import betterquesting.api2.storage.DBEntry;
import betterquesting.api2.utils.BQThreadedIO;
import betterquesting.api2.utils.Tuple2;
import betterquesting.core.BetterQuesting;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeRegistry;
import betterquesting.questing.QuestDatabase;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;

public class NetQuestSync {
    private static final ResourceLocation ID_NAME = new ResourceLocation("betterquesting:quest_sync");

    public static void registerHandler() {
        PacketTypeRegistry.INSTANCE.registerServerHandler(ID_NAME, NetQuestSync::onServer);

        if (BetterQuesting.proxy.isClient()) {
            PacketTypeRegistry.INSTANCE.registerClientHandler(ID_NAME, NetQuestSync::onClient);
        }
    }

    public static void quickSync(int questID, boolean config, boolean progress) {
        if (!config && !progress) return;

        int[] IDs = questID < 0 ? null : new int[] {questID};

        if (config) sendSync(null, IDs, true, false); // We're not sending progress in this pass.

        if (progress) // Send everyone's individual progression
        {
            MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
            if (server == null) return;

            for (Object player : server.getConfigurationManager().playerEntityList) {
                sendSync((EntityPlayerMP) player, IDs, false, true, true); // Progression only this pass
            }
        }
    }

    public static void sendSync(
            @Nullable EntityPlayerMP player, @Nullable int[] questIDs, boolean config, boolean progress) {
        sendSync(player, questIDs, config, progress, false);
    }

    public static void sendSync(
            @Nullable EntityPlayerMP player,
            @Nullable int[] questIDs,
            boolean config,
            boolean progress,
            boolean resetCompletion) {
        if ((!config && !progress) || (questIDs != null && questIDs.length <= 0)) return;

        // Offload this to another thread as it could take a while to build
        BQThreadedIO.INSTANCE.enqueue(() -> {
            NBTTagList dataList = new NBTTagList();
            final List<DBEntry<IQuest>> questSubset = questIDs == null
                    ? QuestDatabase.INSTANCE.getEntries()
                    : QuestDatabase.INSTANCE.bulkLookup(questIDs);
            final List<UUID> pidList =
                    player == null ? null : Collections.singletonList(QuestingAPI.getQuestingUUID(player));

            for (DBEntry<IQuest> entry : questSubset) {
                NBTTagCompound tag = new NBTTagCompound();

                if (config) tag.setTag("config", entry.getValue().writeToNBT(new NBTTagCompound()));
                if (progress)
                    tag.setTag("progress", entry.getValue().writeProgressToNBT(new NBTTagCompound(), pidList));
                tag.setInteger("questID", entry.getID());
                dataList.appendTag(tag);
            }

            NBTTagCompound payload = new NBTTagCompound();
            payload.setBoolean("merge", !config || questIDs != null);
            payload.setBoolean("resetCompletion", resetCompletion);
            payload.setTag("data", dataList);

            if (player == null) {
                PacketSender.INSTANCE.sendToAll(new QuestingPacket(ID_NAME, payload));
            } else {
                PacketSender.INSTANCE.sendToPlayers(new QuestingPacket(ID_NAME, payload), player);
            }
        });
    }

    // Asks the server to send specific quest data over
    @SideOnly(Side.CLIENT)
    public static void requestSync(@Nullable int[] questIDs, boolean configs, boolean progress) {
        NBTTagCompound payload = new NBTTagCompound();
        if (questIDs != null) payload.setIntArray("requestIDs", questIDs);
        payload.setBoolean("getConfig", configs);
        payload.setBoolean("getProgress", progress);
        PacketSender.INSTANCE.sendToServer(new QuestingPacket(ID_NAME, payload));
    }

    private static void onServer(Tuple2<NBTTagCompound, EntityPlayerMP> message) {
        NBTTagCompound payload = message.getFirst();
        int[] reqIDs = !payload.hasKey("requestIDs", 11) ? null : payload.getIntArray("requestIDs");
        sendSync(message.getSecond(), reqIDs, payload.getBoolean("getConfig"), payload.getBoolean("getProgress"));
    }

    @SideOnly(Side.CLIENT)
    private static void onClient(NBTTagCompound message) {
        NBTTagList data = message.getTagList("data", 10);
        boolean merge = message.getBoolean("merge");
        boolean resetCompletion = message.getBoolean("resetCompletion");
        if (!merge) QuestDatabase.INSTANCE.reset();

        for (int i = 0; i < data.tagCount(); i++) {
            NBTTagCompound tag = data.getCompoundTagAt(i);
            if (!tag.hasKey("questID", 99)) continue;
            int questID = tag.getInteger("questID");

            IQuest quest = QuestDatabase.INSTANCE.getValue(questID);

            if (tag.hasKey("config", 10)) {
                if (quest == null) quest = QuestDatabase.INSTANCE.createNew(questID);
                quest.readFromNBT(tag.getCompoundTag("config"));
            }

            if (tag.hasKey("progress", 10) && quest != null) {
                // TODO: Fix this properly
                // If there we're not running the LAN server off this client then we overwrite always
                quest.readProgressFromNBT(
                        tag.getCompoundTag("progress"),
                        !resetCompletion && (merge || Minecraft.getMinecraft().isIntegratedServerRunning()));
            }
        }

        MinecraftForge.EVENT_BUS.post(new DatabaseEvent.Update(DBType.QUEST));
    }
}
