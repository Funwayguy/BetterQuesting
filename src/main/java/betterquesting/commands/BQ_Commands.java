package betterquesting.commands;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import betterquesting.quests.QuestDatabase;
import betterquesting.quests.QuestInstance;
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
		return "/bq edit, /bq hardcore, /bq reset_all, /bq make_default";
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
        	return getListOfStringsMatchingLastWord(strings, new String[]{"edit", "hardcore", "reset_all", "make_default"});
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
			this.ShowUsage(sender);
			return;
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
			for(QuestInstance quest : QuestDatabase.questDB.values())
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
		} else
		{
			this.ShowUsage(sender);
		}
	}
	
	public void ShowUsage(ICommandSender sender)
	{
		sender.addChatMessage(new ChatComponentText(getCommandUsage(sender)));
	}
}
