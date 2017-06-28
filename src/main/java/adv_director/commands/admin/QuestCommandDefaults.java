package adv_director.commands.admin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;
import adv_director.api.enums.EnumSaveType;
import adv_director.api.storage.BQ_Settings;
import adv_director.api.utils.JsonHelper;
import adv_director.commands.QuestCommandBase;
import adv_director.core.AdvDirector;
import adv_director.network.PacketSender;
import adv_director.questing.QuestDatabase;
import adv_director.questing.QuestLineDatabase;
import adv_director.storage.QuestSettings;
import com.google.gson.JsonArray;
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
			JsonObject base = new JsonObject();
			base.add("questSettings", QuestSettings.INSTANCE.writeToJson(new JsonObject(), EnumSaveType.CONFIG));
			base.add("questDatabase", QuestDatabase.INSTANCE.writeToJson(new JsonArray(), EnumSaveType.CONFIG));
			base.add("questLines", QuestLineDatabase.INSTANCE.writeToJson(new JsonArray(), EnumSaveType.CONFIG));
			base.addProperty("format", AdvDirector.FORMAT);
			JsonHelper.WriteToFile(qFile, base);
			
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
				JsonArray jsonP = QuestDatabase.INSTANCE.writeToJson(new JsonArray(), EnumSaveType.PROGRESS);
				JsonObject j1 = JsonHelper.ReadFromFile(qFile);
				QuestSettings.INSTANCE.readFromJson(JsonHelper.GetObject(j1, "questSettings"), EnumSaveType.CONFIG);
				QuestDatabase.INSTANCE.readFromJson(JsonHelper.GetArray(j1, "questDatabase"), EnumSaveType.CONFIG);
				QuestLineDatabase.INSTANCE.readFromJson(JsonHelper.GetArray(j1, "questLines"), EnumSaveType.CONFIG);
				QuestDatabase.INSTANCE.readFromJson(jsonP, EnumSaveType.PROGRESS);
				
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
