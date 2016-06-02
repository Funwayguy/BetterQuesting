package betterquesting.commands.admin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import betterquesting.commands.QuestCommandBase;
import betterquesting.core.BQ_Settings;
import betterquesting.quests.QuestDatabase;
import betterquesting.utils.JsonIO;
import com.google.gson.JsonObject;

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
	
	public List<String> autoComplete(MinecraftServer server, ICommandSender sender, String[] args)
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
	public void runCommand(MinecraftServer server, CommandBase command, ICommandSender sender, String[] args) throws CommandException
	{
		if(args[1].equalsIgnoreCase("save"))
		{
			JsonObject jsonQ = new JsonObject();
			QuestDatabase.writeToJson(jsonQ);
			JsonIO.WriteToFile(new File(server.getFile("config/betterquesting/"), "DefaultQuests.json"), jsonQ);
			sender.addChatMessage(new TextComponentString("Quest database set as global default"));
		} else if(args[1].equalsIgnoreCase("load"))
		{
			JsonObject jsonP = new JsonObject();
			QuestDatabase.writeToJson_Progression(jsonP);
	    	File f1 = new File(BQ_Settings.defaultDir, "DefaultQuests.json");
			JsonObject j1 = new JsonObject();
			
			if(f1.exists())
			{
				j1 = JsonIO.ReadFromFile(f1);
				QuestDatabase.readFromJson(j1);
				QuestDatabase.readFromJson_Progression(jsonP);
				sender.addChatMessage(new TextComponentString("Reloaded default quest database"));
				QuestDatabase.UpdateClients();
			} else
			{
				sender.addChatMessage(new TextComponentString("No default currently set"));
			}
		} else
		{
			throw getException(command);
		}
	}
}
