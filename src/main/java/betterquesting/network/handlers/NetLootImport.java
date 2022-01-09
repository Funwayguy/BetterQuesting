package betterquesting.network.handlers;

import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.network.QuestingPacket;
import betterquesting.core.BetterQuesting;
import betterquesting.questing.rewards.loot.LootRegistry;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Level;

public class NetLootImport
{
	private static final ResourceLocation ID_NAME = new ResourceLocation("bq_standard:loot_import");
	
	public static void registerHandler()
    {
        QuestingAPI.getAPI(ApiReference.PACKET_REG).registerServerHandler(ID_NAME, NetLootImport::onServer);
    }
    
    // TODO: Rework this for partial importing/editing
    
    @SideOnly(Side.CLIENT)
    public static void importLoot(NBTTagCompound data)
    {
        NBTTagCompound payload = new NBTTagCompound();
        payload.setTag("data", data);
        QuestingAPI.getAPI(ApiReference.PACKET_SENDER).sendToServer(new QuestingPacket(ID_NAME, payload));
    }
    
	private static void onServer(Tuple<NBTTagCompound, EntityPlayerMP> message)
	{
	    EntityPlayerMP sender = message.getSecond();
	    NBTTagCompound tag = message.getFirst();
	    
		if(sender.getServer() == null) return;
		
		if(!sender.getServer().getPlayerList().canSendCommands(sender.getGameProfile()))
		{
            BetterQuesting.logger.log(Level.WARN, "Player " + sender.getName() + " (UUID:" + QuestingAPI.getQuestingUUID(sender) + ") tried to import loot without OP permissions!");
			sender.sendMessage(new TextComponentString(TextFormatting.RED + "You need to be OP to edit loot!"));
			return; // Player is not operator. Do nothing
		}
		
		LootRegistry.INSTANCE.readFromNBT(tag.getCompoundTag("data"), false);
		NetLootSync.sendSync(null);
	}
}
