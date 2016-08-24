package betterquesting.network.handlers;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.Level;
import betterquesting.api.enums.EnumPacketAction;
import betterquesting.api.network.IPacketHandler;
import betterquesting.api.network.PacketTypeNative;
import betterquesting.api.quests.IQuestLineContainer;
import betterquesting.core.BetterQuesting;
import betterquesting.network.PacketSender;
import betterquesting.quests.QuestLine;
import betterquesting.quests.QuestLineDatabase;

public class PktHandlerLineEdit implements IPacketHandler
{
	@Override
	public ResourceLocation getRegistryName()
	{
		return PacketTypeNative.LINE_EDIT.GetLocation();
	}
	
	@Override
	public void handleServer(NBTTagCompound data, EntityPlayerMP sender)
	{
		if(sender == null)
		{
			return;
		}
		
		boolean isOP = MinecraftServer.getServer().getConfigurationManager().func_152596_g(sender.getGameProfile());
		
		if(!isOP)
		{
			BetterQuesting.logger.log(Level.WARN, "Player " + sender.getCommandSenderName() + " (UUID:" + sender.getUniqueID() + ") tried to edit quest lines without OP permissions!");
			sender.addChatComponentMessage(new ChatComponentText(EnumChatFormatting.RED + "You need to be OP to edit quests!"));
			return; // Player is not operator. Do nothing
		}
		
		int aID = !data.hasKey("action")? -1 : data.getInteger("action");
		int lID = !data.hasKey("lineID")? -1 : data.getInteger("lineID");
		IQuestLineContainer questLine = QuestLineDatabase.INSTANCE.getValue(lID);
		
		if(aID < 0 || aID >= EnumPacketAction.values().length)
		{
			return;
		}
		
		EnumPacketAction action = EnumPacketAction.values()[aID];
		
		if(action == EnumPacketAction.ADD) 
		{
			QuestLineDatabase.INSTANCE.add(new QuestLine(), QuestLineDatabase.INSTANCE.nextID());
			PacketSender.INSTANCE.sendToAll(QuestLineDatabase.INSTANCE.getSyncPacket());
			return;
		} else if(action == EnumPacketAction.EDIT && questLine != null) // Edit quest lines
		{
			questLine.readPacket(data);
			PacketSender.INSTANCE.sendToAll(questLine.getSyncPacket());
			return;
		} else if(action == EnumPacketAction.REMOVE && questLine != null)
		{
			QuestLineDatabase.INSTANCE.remove(lID);
			PacketSender.INSTANCE.sendToAll(QuestLineDatabase.INSTANCE.getSyncPacket());
			return;
		}
	}
	
	@Override
	public void handleClient(NBTTagCompound data)
	{
	}
}
