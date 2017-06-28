package adv_director.network.handlers;

import org.apache.logging.log4j.Level;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.MinecraftForge;
import adv_director.api.api.QuestingAPI;
import adv_director.api.events.DatabaseEvent;
import adv_director.api.network.IPacketHandler;
import adv_director.core.AdvDirector;
import adv_director.network.PacketSender;
import adv_director.network.PacketTypeNative;
import adv_director.storage.QuestSettings;

public class PktHandlerSettings implements IPacketHandler
{
	
	@Override
	public ResourceLocation getRegistryName()
	{
		return PacketTypeNative.SETTINGS.GetLocation();
	}
	
	@Override
	public void handleServer(NBTTagCompound tag, EntityPlayerMP sender)
	{
		if(sender == null)
		{
			return;
		}
		
		boolean isOP = sender.worldObj.getMinecraftServer().getPlayerList().canSendCommands(sender.getGameProfile());
		
		if(!isOP)
		{
			AdvDirector.logger.log(Level.WARN, "Player " + sender.getName() + " (UUID:" + QuestingAPI.getQuestingUUID(sender) + ") tried to edit settings without OP permissions!");
			sender.addChatComponentMessage(new TextComponentString(TextFormatting.RED + "You need to be OP to edit quests!"));
			return; // Player is not operator. Do nothing
		}
		
		QuestSettings.INSTANCE.readPacket(tag);
		PacketSender.INSTANCE.sendToAll(QuestSettings.INSTANCE.getSyncPacket());
	}
	
	@Override
	public void handleClient(NBTTagCompound tag)
	{
		QuestSettings.INSTANCE.readPacket(tag);
		MinecraftForge.EVENT_BUS.post(new DatabaseEvent.Update());
	}
}
