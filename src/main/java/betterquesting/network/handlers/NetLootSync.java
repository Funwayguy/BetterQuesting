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

import javax.annotation.Nullable;

public class NetLootSync
{
	private static final ResourceLocation ID_NAME = new ResourceLocation("bq_standard:loot_database");
	
	public static void registerHandler()
    {
        QuestingAPI.getAPI(ApiReference.PACKET_REG).registerServerHandler(ID_NAME, NetLootSync::onServer);
    
        if(BetterQuesting.proxy.isClient())
        {
            QuestingAPI.getAPI(ApiReference.PACKET_REG).registerClientHandler(ID_NAME, NetLootSync::onClient);
        }
    }
    
    @SideOnly(Side.CLIENT)
    public static void requestEdit(NBTTagCompound data)
    {
        NBTTagCompound payload = new NBTTagCompound();
        payload.setTag("data", data);
        QuestingAPI.getAPI(ApiReference.PACKET_SENDER).sendToServer(new QuestingPacket(ID_NAME, payload));
    }
    
    public static void sendSync(@Nullable EntityPlayerMP player)
    {
        NBTTagCompound payload = new NBTTagCompound();
        payload.setTag("data", LootRegistry.INSTANCE.writeToNBT(new NBTTagCompound(), null));
        
        if(player == null)
        {
            QuestingAPI.getAPI(ApiReference.PACKET_SENDER).sendToAll(new QuestingPacket(ID_NAME, payload));
        } else
        {
            QuestingAPI.getAPI(ApiReference.PACKET_SENDER).sendToPlayers(new QuestingPacket(ID_NAME, payload), player);
        }
    }
    
	private static void onServer(Tuple<NBTTagCompound, EntityPlayerMP> message)
	{
	    EntityPlayerMP sender = message.getSecond();
	    NBTTagCompound data = message.getFirst();
	    
	    if(sender.getServer() == null) return;
		if(!sender.getServer().getPlayerList().canSendCommands(sender.getGameProfile()))
		{
            BetterQuesting.logger.log(Level.WARN, "Player " + sender.getName() + " (UUID:" + QuestingAPI.getQuestingUUID(sender) + ") tried to edit loot chests without OP permissions!");
			sender.sendStatusMessage(new TextComponentString(TextFormatting.RED + "You need to be OP to edit loot!"), true);
			return; // Player is not operator. Do nothing
		}

        BetterQuesting.logger.log(Level.INFO, "Player " + sender.getName() + " edited loot chests");
		
		LootRegistry.INSTANCE.readFromNBT(data.getCompoundTag("data"), false);
		sendSync(null);
	}
	
	@SideOnly(Side.CLIENT)
	private static void onClient(NBTTagCompound message)
	{
		LootRegistry.INSTANCE.readFromNBT(message.getCompoundTag("data"), false);
		LootRegistry.INSTANCE.updateUI = true;
	}
}
