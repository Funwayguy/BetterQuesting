package betterquesting.commands.admin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.storage.BQ_Settings;
import betterquesting.api.utils.JsonHelper;
import betterquesting.api.utils.NBTConverter;
import betterquesting.commands.QuestCommandBase;
import betterquesting.core.BetterQuesting;
import betterquesting.network.PacketSender;
import betterquesting.questing.QuestDatabase;
import betterquesting.questing.QuestLineDatabase;
import betterquesting.storage.QuestSettings;
import com.google.gson.JsonObject;

public class QuestCommandDefaults extends QuestCommandBase
{
	@Override
	public String getUsageSuffix()
	{
		return "[save|load|set] [file_name]";
	}
	
	@Override
	public boolean validArgs(String[] args)
	{
		return args.length == 2 || args.length == 3;
	}
	
	@Override
	public List<String> autoComplete(MinecraftServer server, ICommandSender sender, String[] args)
	{
		ArrayList<String> list = new ArrayList<String>();
		
		if(args.length == 2)
		{
			return CommandBase.getListOfStringsMatchingLastWord(args, new String[]{"save","load", "set"});
		} else if(args.length == 3)
		{
			list.add("DefaultQuests");
		}
		
		return list;
	}
	
	@Override
	public String getCommand()
	{
		return "default";
	}
	
	@Override
	public void runCommand(MinecraftServer server, CommandBase command, ICommandSender sender, String[] args) throws CommandException
	{
		File qFile;
		
		if(args.length == 3 && !args[2].equalsIgnoreCase("DefaultQuests"))
		{
			qFile = new File(BQ_Settings.defaultDir, "saved_quests/" + args[2] + ".json");
		} else
		{
			qFile = new File(BQ_Settings.defaultDir, "DefaultQuests.json");
		}
		
		if(args[1].equalsIgnoreCase("save"))
		{
			NBTTagCompound base = new NBTTagCompound();
			base.setTag("questSettings", QuestSettings.INSTANCE.writeToNBT(new NBTTagCompound(), EnumSaveType.CONFIG));
			base.setTag("questDatabase", QuestDatabase.INSTANCE.writeToNBT(new NBTTagList(), EnumSaveType.CONFIG));
			base.setTag("questLines", QuestLineDatabase.INSTANCE.writeToNBT(new NBTTagList(), EnumSaveType.CONFIG));
			base.setString("format", BetterQuesting.FORMAT);
			JsonHelper.WriteToFile(qFile, NBTConverter.NBTtoJSON_Compound(base, new JsonObject(), true));
			
			if(args.length == 3 && !args[2].equalsIgnoreCase("DefaultQuests"))
			{
				sender.addChatMessage(new TextComponentTranslation("betterquesting.cmd.default.save2", args[2] + ".json"));
			} else
			{
				sender.addChatMessage(new TextComponentTranslation("betterquesting.cmd.default.save"));
			}
		} else if(args[1].equalsIgnoreCase("load"))
		{
			if(qFile.exists())
			{
				NBTTagList jsonP = QuestDatabase.INSTANCE.writeToNBT(new NBTTagList(), EnumSaveType.PROGRESS);
				NBTTagCompound j1 = NBTConverter.JSONtoNBT_Object(JsonHelper.ReadFromFile(qFile), new NBTTagCompound(), true);
				QuestSettings.INSTANCE.readFromNBT(j1.getCompoundTag("questSettings"), EnumSaveType.CONFIG);
				QuestDatabase.INSTANCE.readFromNBT(j1.getTagList("questDatabase", 10), EnumSaveType.CONFIG);
				QuestLineDatabase.INSTANCE.readFromNBT(j1.getTagList("questLines", 10), EnumSaveType.CONFIG);
				QuestDatabase.INSTANCE.readFromNBT(jsonP, EnumSaveType.PROGRESS);
				
				if(args.length == 3 && !args[2].equalsIgnoreCase("DefaultQuests"))
				{
					sender.addChatMessage(new TextComponentTranslation("betterquesting.cmd.default.load2", args[2] + ".json"));
				} else
				{
					sender.addChatMessage(new TextComponentTranslation("betterquesting.cmd.default.load"));
				}
				
				PacketSender.INSTANCE.sendToAll(QuestDatabase.INSTANCE.getSyncPacket());
				PacketSender.INSTANCE.sendToAll(QuestLineDatabase.INSTANCE.getSyncPacket());
			} else
			{
				sender.addChatMessage(new TextComponentTranslation("betterquesting.cmd.default.none"));
			}
		} else if(args[1].equalsIgnoreCase("set") && args.length == 3)
		{
			if(qFile.exists() && !args[2].equalsIgnoreCase("DefaultQuests"))
			{
				File defFile = new File(BQ_Settings.defaultDir, "DefaultQuests.json");
				
				if(defFile.exists())
				{
					defFile.delete();
				}
				
				JsonHelper.CopyPaste(qFile, defFile);
				
				sender.addChatMessage(new TextComponentTranslation("betterquesting.cmd.default.set", args[2]));
			} else
			{
				sender.addChatMessage(new TextComponentTranslation("betterquesting.cmd.default.none"));
			}
		} else
		{
			throw getException(command);
		}
	}
}
