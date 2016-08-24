package betterquesting.commands.admin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentTranslation;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.utils.JsonHelper;
import betterquesting.api.utils.JsonIO;
import betterquesting.commands.QuestCommandBase;
import betterquesting.core.BQ_Settings;
import betterquesting.network.PacketSender;
import betterquesting.quests.QuestDatabase;
import betterquesting.quests.QuestLineDatabase;
import com.google.gson.JsonArray;
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
			JsonObject base = new JsonObject();
			base.add("questDatabase", QuestDatabase.INSTANCE.writeToJson(new JsonArray(), EnumSaveType.CONFIG));
			base.add("questLines", QuestLineDatabase.INSTANCE.writeToJson(new JsonArray(), EnumSaveType.CONFIG));
			JsonIO.WriteToFile(new File(MinecraftServer.getServer().getFile("config/betterquesting/"), "DefaultQuests.json"), base);
			sender.addChatMessage(new ChatComponentTranslation("betterquesting.cmd.default.save"));
		} else if(args[1].equalsIgnoreCase("load"))
		{
			JsonArray jsonP = QuestDatabase.INSTANCE.writeToJson(new JsonArray(), EnumSaveType.PROGRESS);
	    	File f1 = new File(BQ_Settings.defaultDir, "DefaultQuests.json");
			JsonObject j1 = new JsonObject();
			
			if(f1.exists())
			{
				j1 = JsonIO.ReadFromFile(f1);
				QuestDatabase.INSTANCE.readFromJson(JsonHelper.GetArray(j1, "questDatabase"), EnumSaveType.CONFIG);
				QuestLineDatabase.INSTANCE.readFromJson(JsonHelper.GetArray(j1, "questLines"), EnumSaveType.CONFIG);
				QuestDatabase.INSTANCE.readFromJson(jsonP, EnumSaveType.PROGRESS);
				sender.addChatMessage(new ChatComponentTranslation("betterquesting.cmd.default.load"));
				PacketSender.INSTANCE.sendToAll(QuestDatabase.INSTANCE.getSyncPacket());
				PacketSender.INSTANCE.sendToAll(QuestLineDatabase.INSTANCE.getSyncPacket());
			} else
			{
				sender.addChatMessage(new ChatComponentTranslation("betterquesting.cmd.default.none"));
			}
		} else
		{
			throw getException(command);
		}
	}
}
