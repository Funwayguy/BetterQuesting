package betterquesting.network.handlers;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.Level;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.network.IPacketHandler;
import betterquesting.api.network.PacketTypeNative;
import betterquesting.api.utils.JsonHelper;
import betterquesting.api.utils.NBTConverter;
import betterquesting.core.BetterQuesting;
import betterquesting.quests.QuestDatabase;
import betterquesting.quests.QuestInstance;
import betterquesting.quests.QuestLine;
import betterquesting.quests.QuestLineDatabase;
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
		
		if(!MinecraftServer.getServer().getConfigurationManager().func_152596_g(sender.getGameProfile()))
		{
			BetterQuesting.logger.log(Level.WARN, "Player " + sender.getCommandSenderName() + " (UUID:" + sender.getUniqueID() + ") tried to edit quest lines without OP permissions!");
			sender.addChatComponentMessage(new ChatComponentText(EnumChatFormatting.RED + "You need to be OP to edit quests!"));
			return; // Player is not operator. Do nothing
		}
		
		int action = !data.hasKey("action")? -1 : data.getInteger("action");
		
		if(action < 0)
		{
			BetterQuesting.logger.log(Level.ERROR, sender.getCommandSenderName() + " tried to perform invalid quest edit action: " + action);
			return;
		}
		
		if(action == 0) // Add new QuestLine
		{
			QuestLineDatabase.INSTANCE.add(new QuestLine(), QuestLineDatabase.INSTANCE.nextID());
		} else if(action == 1) // Add new QuestInstance
		{
			QuestDatabase.INSTANCE.add(new QuestInstance(), QuestDatabase.INSTANCE.nextID());
		} else if(action == 2) // Edit quest lines
		{
			JsonObject json = NBTConverter.NBTtoJSON_Compound(data.getCompoundTag("Data"), new JsonObject());
			QuestLineDatabase.INSTANCE.readFromJson(JsonHelper.GetArray(json, "questLines"), EnumSaveType.CONFIG);
		}
		
		QuestDatabase.INSTANCE.syncAll(); // Update all clients with new quest data
	}
	
	@Override
	public void handleClient(NBTTagCompound data)
	{
	}
}
