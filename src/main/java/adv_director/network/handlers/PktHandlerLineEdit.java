package adv_director.network.handlers;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import org.apache.logging.log4j.Level;
import adv_director.api.api.QuestingAPI;
import adv_director.api.enums.EnumPacketAction;
import adv_director.api.enums.EnumSaveType;
import adv_director.api.network.IPacketHandler;
import adv_director.api.questing.IQuestLine;
import adv_director.api.utils.JsonHelper;
import adv_director.api.utils.NBTConverter;
import adv_director.core.AdvDirector;
import adv_director.network.PacketSender;
import adv_director.network.PacketTypeNative;
import adv_director.questing.QuestLine;
import adv_director.questing.QuestLineDatabase;
import com.google.gson.JsonObject;

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
		
		boolean isOP = sender.worldObj.getMinecraftServer().getPlayerList().canSendCommands(sender.getGameProfile());
		
		if(!isOP)
		{
			AdvDirector.logger.log(Level.WARN, "Player " + sender.getName() + " (UUID:" + QuestingAPI.getQuestingUUID(sender) + ") tried to edit quest lines without OP permissions!");
			sender.addChatComponentMessage(new TextComponentString(TextFormatting.RED + "You need to be OP to edit quests!"));
			return; // Player is not operator. Do nothing
		}
		
		int aID = !data.hasKey("action")? -1 : data.getInteger("action");
		int lID = !data.hasKey("lineID")? -1 : data.getInteger("lineID");
		int idx = !data.hasKey("order")? -1 : data.getInteger("order");
		IQuestLine questLine = QuestLineDatabase.INSTANCE.getValue(lID);
		
		if(aID < 0 || aID >= EnumPacketAction.values().length)
		{
			return;
		}
		
		EnumPacketAction action = EnumPacketAction.values()[aID];
		
		if(action == EnumPacketAction.ADD) 
		{
			IQuestLine nq = new QuestLine();
			int nID = QuestLineDatabase.INSTANCE.nextKey();
			
			if(data.hasKey("data") && lID >= 0)
			{
				nID = lID;
				
				JsonObject base = NBTConverter.NBTtoJSON_Compound(data.getCompoundTag("data"), new JsonObject());
				nq.readFromJson(JsonHelper.GetObject(base, "line"), EnumSaveType.CONFIG);
			}
			
			QuestLineDatabase.INSTANCE.add(nq, nID);
			PacketSender.INSTANCE.sendToAll(nq.getSyncPacket());
			return;
		} else if(action == EnumPacketAction.EDIT && questLine != null) // Edit quest lines
		{
			questLine.readPacket(data);
			
			if(idx >= 0 && QuestLineDatabase.INSTANCE.getOrderIndex(lID) != idx)
			{
				QuestLineDatabase.INSTANCE.setOrderIndex(lID, idx);
				PacketSender.INSTANCE.sendToAll(QuestLineDatabase.INSTANCE.getSyncPacket());
			} else
			{
				PacketSender.INSTANCE.sendToAll(questLine.getSyncPacket());
			}
			return;
		} else if(action == EnumPacketAction.REMOVE && questLine != null)
		{
			QuestLineDatabase.INSTANCE.removeKey(lID);
			PacketSender.INSTANCE.sendToAll(QuestLineDatabase.INSTANCE.getSyncPacket());
			return;
		}
	}
	
	@Override
	public void handleClient(NBTTagCompound data)
	{
	}
}
