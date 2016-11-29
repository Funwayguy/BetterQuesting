package betterquesting.commands.admin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.utils.JsonHelper;
import betterquesting.commands.QuestCommandBase;
import betterquesting.core.BQ_Settings;
import betterquesting.core.BetterQuesting;
import betterquesting.network.PacketSender;
import betterquesting.questing.QuestDatabase;
import betterquesting.questing.QuestLineDatabase;
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
			JsonObject base = new JsonObject();
			base.add("questDatabase", QuestDatabase.INSTANCE.writeToJson(new JsonArray(), EnumSaveType.CONFIG));
			base.add("questLines", QuestLineDatabase.INSTANCE.writeToJson(new JsonArray(), EnumSaveType.CONFIG));
			base.addProperty("format", BetterQuesting.FORMAT);
			JsonHelper.WriteToFile(new File(server.getFile("config/betterquesting/"), "DefaultQuests.json"), base);
			sender.addChatMessage(new TextComponentTranslation("betterquesting.cmd.default.save"));
		} else if(args[1].equalsIgnoreCase("load"))
		{
			JsonArray jsonP = QuestDatabase.INSTANCE.writeToJson(new JsonArray(), EnumSaveType.PROGRESS);
	    	File f1 = new File(BQ_Settings.defaultDir, "DefaultQuests.json");
			JsonObject j1 = new JsonObject();
			
			if(f1.exists())
			{
				j1 = JsonHelper.ReadFromFile(f1);
				QuestDatabase.INSTANCE.readFromJson(JsonHelper.GetArray(j1, "questDatabase"), EnumSaveType.CONFIG);
				QuestLineDatabase.INSTANCE.readFromJson(JsonHelper.GetArray(j1, "questLines"), EnumSaveType.CONFIG);
				QuestDatabase.INSTANCE.readFromJson(jsonP, EnumSaveType.PROGRESS);
				sender.addChatMessage(new TextComponentTranslation("betterquesting.cmd.default.load"));
				PacketSender.INSTANCE.sendToAll(QuestDatabase.INSTANCE.getSyncPacket());
				PacketSender.INSTANCE.sendToAll(QuestLineDatabase.INSTANCE.getSyncPacket());
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
