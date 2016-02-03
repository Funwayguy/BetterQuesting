package betterquesting.commands;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import betterquesting.core.BQ_Settings;
import betterquesting.quests.QuestDatabase;
import betterquesting.quests.QuestInstance;
import betterquesting.quests.QuestLine;
import betterquesting.utils.JsonIO;
import com.google.gson.JsonObject;

public class BQ_Commands extends CommandBase
{
	@Override
	public String getCommandName()
	{
		return "bq";
	}

	@Override
	public String getCommandUsage(ICommandSender sender)
	{
		return "/bq edit, /bq hardcore, /bq reset_all, /bq make_default, /bq load_default, /bq delete_all, /bq reload";
	}

    /**
     * Adds the strings available in this command to the given list of tab completion options.
     */
	@SuppressWarnings("unchecked")
	@Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] strings)
    {
		if(strings.length == 1)
		{
        	return getListOfStringsMatchingLastWord(strings, new String[]{"edit", "hardcore", "reset_all", "make_default", "load_default", "delete_all", "reload"});
		}
		
		return new ArrayList<String>();
    }
	
	@Override
    public int getRequiredPermissionLevel()
    {
        return 2;
    }

	@Override
	public void processCommand(ICommandSender sender, String[] entries)
	{
		if(entries.length != 1)
		{
			throw new WrongUsageException(this.getCommandUsage(sender));
		}
		
		if(entries[0].equalsIgnoreCase("edit")) // Missed a spot?
		{
			QuestDatabase.editMode = !QuestDatabase.editMode;
			QuestDatabase.UpdateClients();
			sender.addChatMessage(new ChatComponentText("Edit mode " + (QuestDatabase.editMode? "enabled" : "disabled")));
		} else if(entries[0].equalsIgnoreCase("hardcore")) // Time to get #REKT
		{
			QuestDatabase.bqHardcore = !QuestDatabase.bqHardcore;
			QuestDatabase.UpdateClients();
			sender.addChatMessage(new ChatComponentText("Hardcore mode " + (QuestDatabase.bqHardcore? "enabled" : "disabled")));
		} else if(entries[0].equalsIgnoreCase("reset_all")) // Recommended command before publishing
		{
			for(QuestInstance quest : new ArrayList<QuestInstance>(QuestDatabase.questDB.values()))
			{
				quest.ResetQuest();
			}
			
			QuestDatabase.UpdateClients();
			sender.addChatMessage(new ChatComponentText("All questing cleared"));
			
		} else if(entries[0].equalsIgnoreCase("make_default")) // Save currently loaded questline as the default
		{
			JsonObject jsonQ = new JsonObject();
			QuestDatabase.writeToJson(jsonQ);
			JsonIO.WriteToFile(new File(MinecraftServer.getServer().getFile("config/betterquesting/"), "DefaultQuests.json"), jsonQ);
			sender.addChatMessage(new ChatComponentText("Quest database set as global default"));
		} else if(entries[0].equalsIgnoreCase("load_default"))
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
		} else if(entries[0].equalsIgnoreCase("delete_all"))
		{
			QuestDatabase.questDB = new HashMap<Integer,QuestInstance>();
			QuestDatabase.questLines = new ArrayList<QuestLine>();
			QuestDatabase.UpdateClients();
		    
			sender.addChatMessage(new ChatComponentText("Deleted all quests and quest lines"));
		} else if(entries[0].equalsIgnoreCase("reload"))
		{
			if(BQ_Settings.curWorldDir == null)
			{
				sender.addChatMessage(new ChatComponentText("ERROR: Save directory isn't initialised"));
				return;
			}
	    	
	    	File f1 = new File(BQ_Settings.curWorldDir, "QuestDatabase.json");
			JsonObject j1 = new JsonObject();
			
			if(f1.exists())
			{
				j1 = JsonIO.ReadFromFile(f1);
			} else
			{
				f1 = new File(BQ_Settings.defaultDir, "DefaultQuests.json");
				
				if(f1.exists())
				{
					j1 = JsonIO.ReadFromFile(f1);
				}
			}
			
			QuestDatabase.readFromJson(j1);
			QuestDatabase.UpdateClients();
		    
			sender.addChatMessage(new ChatComponentText("Reloaded " + QuestDatabase.questDB.size() + " quest instances and " + QuestDatabase.questLines.size() + " quest lines from file"));
		} else
		{
			throw new WrongUsageException(this.getCommandUsage(sender));
		}
	}
}
