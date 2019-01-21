package betterquesting.commands;

import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.IQuestLine;
import betterquesting.api.utils.JsonHelper;
import betterquesting.api.utils.NBTConverter;
import betterquesting.questing.QuestDatabase;
import betterquesting.questing.QuestInstance;
import betterquesting.questing.QuestLineDatabase;
import betterquesting.questing.QuestLineEntry;
import com.google.gson.JsonObject;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

import java.io.File;

public class BQ_CommandDebug extends CommandBase
{
	@Override
	public String getName()
	{
		return "bq_debug";
	}
	
	@Override
	public String getUsage(ICommandSender sender)
	{
		return "TO BE USED IN DEV ONLY";
	}
	
	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
	{
	    final int amount = 1000;
	    final int remove = 0;
	    
	    sender.sendMessage(new TextComponentString("Generating " + amount + " quests..."));
        
        QuestDatabase.INSTANCE.reset();
        QuestLineDatabase.INSTANCE.reset();
        
        QuestInstance lastQuest = null;
        IQuestLine questLine = null;
        
        for(int i = 0; i < amount; i++)
        {
            QuestInstance quest = new QuestInstance();
            quest.setProperty(NativeProps.NAME, "Quest #" + i);
            
            if(lastQuest != null)
            {
                quest.getPrerequisites().add(lastQuest);
            }
            
            QuestDatabase.INSTANCE.add(i, quest);
            
            if(i % 1000 == 0)
            {
                questLine = QuestLineDatabase.INSTANCE.createNew(i / 100);
                questLine.getProperties().setProperty(NativeProps.NAME, "Quest Line #" + (i / 100));
            }
            if(questLine != null) questLine.add(i, new QuestLineEntry((i % 20) * 48, (i / 20) * 48, 24));
            
            lastQuest = quest;
        }
	    
	    sender.sendMessage(new TextComponentString("Deleting " + remove + " quests..."));
        
        for(int i = 0; i < amount && i < remove; i++)
        {
            QuestDatabase.INSTANCE.removeID(i);
        }
        
       sender.sendMessage(new TextComponentString("Writing " + QuestDatabase.INSTANCE.size() + " quests to file"));
        
	    final File file = server.getFile("debug_test.json");
     
	    NBTTagCompound tagBase = new NBTTagCompound();
	    tagBase.setTag("questDatabase", QuestDatabase.INSTANCE.writeToNBT(new NBTTagList(), null));
	    tagBase.setTag("questLines", QuestLineDatabase.INSTANCE.writeToNBT(new NBTTagList(), null));
        JsonHelper.WriteToFile(file, NBTConverter.NBTtoJSON_Compound(tagBase, new JsonObject(), true));
	    
       sender.sendMessage(new TextComponentString("Reading from file..."));
        
        NBTTagCompound tag = NBTConverter.JSONtoNBT_Object(JsonHelper.ReadFromFile(file), new NBTTagCompound(), true);
        QuestDatabase.INSTANCE.readFromNBT(tag.getTagList("questDatabase", 10), false);
        QuestLineDatabase.INSTANCE.readFromNBT(tag.getTagList("questLines", 10), false);
        
        sender.sendMessage(new TextComponentString("Restored " + QuestDatabase.INSTANCE.size() + " quests from file"));
	}
}
