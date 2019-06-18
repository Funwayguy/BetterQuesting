package betterquesting.network.handlers;

import betterquesting.api.api.QuestingAPI;
import betterquesting.api.events.DatabaseEvent;
import betterquesting.api.network.IPacketHandler;
import betterquesting.api.network.QuestingPacket;
import betterquesting.core.BetterQuesting;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeNative;
import betterquesting.storage.QuestSettings;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.MinecraftForge;
import org.apache.logging.log4j.Level;

public class PktHandlerSettings implements IPacketHandler
{
    public static final PktHandlerSettings INSTANCE = new PktHandlerSettings();
    
	@Override
	public ResourceLocation getRegistryName()
	{
		return PacketTypeNative.SETTINGS.GetLocation();
	}
	
	@Override
	public void handleServer(NBTTagCompound tag, EntityPlayerMP sender)
	{
		if(sender == null) return;
		boolean isOP = sender.world.getMinecraftServer().getPlayerList().canSendCommands(sender.getGameProfile());
		
		if(!isOP)
		{
			BetterQuesting.logger.log(Level.WARN, "Player " + sender.getName() + " (UUID:" + QuestingAPI.getQuestingUUID(sender) + ") tried to edit settings without OP permissions!");
			sender.sendStatusMessage(new TextComponentString(TextFormatting.RED + "You need to be OP to edit quests!"), false);
			return; // Player is not operator. Do nothing
		}
		
		QuestSettings.INSTANCE.readFromNBT(tag.getCompoundTag("data"));
		PacketSender.INSTANCE.sendToAll(PktHandlerSettings.INSTANCE.getSyncPacket());
	}
	
	@Override
	public void handleClient(NBTTagCompound tag)
	{
		QuestSettings.INSTANCE.readFromNBT(tag.getCompoundTag("data"));
		MinecraftForge.EVENT_BUS.post(new DatabaseEvent.Update());
	}
	
	public QuestingPacket getSyncPacket()
    {
        NBTTagCompound tags = new NBTTagCompound();
		tags.setTag("data", QuestSettings.INSTANCE.writeToNBT(new NBTTagCompound()));
		return new QuestingPacket(PacketTypeNative.SETTINGS.GetLocation(), tags);
    }
}
