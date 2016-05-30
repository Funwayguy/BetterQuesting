package betterquesting.commands.admin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.JsonObject;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import betterquesting.commands.QuestCommandBase;
import betterquesting.core.BQ_Settings;
import betterquesting.quests.QuestDatabase;
import betterquesting.utils.JsonIO;

public class QuestCommandDefaults extends QuestCommandBase
{
	public String getUsageSuffix()
	{
		return "[save|load]";
	}
	
	public boolean validArgs(String[] args)
	{
		return args.length == 2;
	}
	
	@SuppressWarnings("unchecked")
	public List<String> autoComplete(ICommandSender sender, String[] args)
	{
		ArrayList<String> list = new ArrayList<String>();
		
		if(args.length == 2)
		{
			return CommandBase.getListOfStringsMatchingLastWord(args, new String[]{"save","load"});
		}
		
		return list;
	}
	
	@Override
	public String getCommand()
	{
		return "default";
	}
	
	@Override
	public void runCommand(CommandBase command, ICommandSender sender, String[] args)
	{
		if(args[1].equalsIgnoreCase("save"))
		{
			JsonObject jsonQ = new JsonObject();
			QuestDatabase.writeToJson(jsonQ);
			JsonIO.WriteToFile(new File(MinecraftServer.getServer().getFile("config/betterquesting/"), "DefaultQuests.json"), jsonQ);
			sender.addChatMessage(new ChatComponentText("Quest database set as global default"));
		} else if(args[1].equalsIgnoreCase("load"))
		{
	    	File f1 = new File(BQ_Settings.defaultDir, "DefaultQuests.json");
			JsonObject j1 = new JsonObject();
			
			if(f1.exists())
			{
				j1 = JsonIO.ReadFromFile(f1);
				QuestDatabase.readFromJson(j1);
				sender.addChatMessage(new ChatComponentText("Reloaded default quest database"));
			} else
			{
				sender.addChatMessage(new ChatComponentText("No default currently set"));
			}
		} else
		{
			throw getException(command);
		}
	}
}
