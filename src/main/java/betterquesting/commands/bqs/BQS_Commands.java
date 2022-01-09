package betterquesting.commands.bqs;

import betterquesting.api.utils.JsonHelper;
import betterquesting.api.utils.NBTConverter;
import betterquesting.network.handlers.NetLootSync;
import betterquesting.questing.rewards.loot.LootRegistry;
import com.google.gson.JsonObject;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

import java.io.File;

public class BQS_Commands extends CommandBase
{
	@Override
	public String getName()
	{
		return "bqs_loot";
	}

	@Override
	public String getUsage(ICommandSender sender)
	{
		return "/bqs_loot default [save|load], /bqs_loot delete [all|<loot_id>]";
	}
	
	@Override
    public int getRequiredPermissionLevel()
    {
        return 2;
    }

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
	{
		if(args.length != 2)
		{
			throw new WrongUsageException(getUsage(sender));
		}
		
		if(args[0].equalsIgnoreCase("default"))
		{
			if(args[1].equalsIgnoreCase("save"))
			{
				NBTTagCompound jsonQ = new NBTTagCompound();
				LootRegistry.INSTANCE.writeToNBT(jsonQ, null);
				JsonHelper.WriteToFile(new File(server.getFile("config/betterquesting/"), "DefaultLoot.json"), NBTConverter.NBTtoJSON_Compound(jsonQ, new JsonObject(), true));
				sender.sendMessage(new TextComponentString("Loot database set as global default"));
			} else if(args[1].equalsIgnoreCase("load"))
			{
		    	File f1 = new File("config/betterquesting/DefaultLoot.json");
				NBTTagCompound j1;
				
				if(f1.exists())
				{
					j1 = NBTConverter.JSONtoNBT_Object(JsonHelper.ReadFromFile(f1), new NBTTagCompound(), true);
					LootRegistry.INSTANCE.readFromNBT(j1, false);
                    NetLootSync.sendSync(null);
					sender.sendMessage(new TextComponentString("Reloaded default loot database"));
				} else
				{
					sender.sendMessage(new TextComponentString(TextFormatting.RED + "No default loot currently set"));
				}
			} else
			{
				throw new WrongUsageException(getUsage(sender));
			}
		} else if(args[0].equalsIgnoreCase("delete"))
		{
			if(args[1].equalsIgnoreCase("all"))
			{
				LootRegistry.INSTANCE.reset();
                NetLootSync.sendSync(null);
				sender.sendMessage(new TextComponentString("Deleted all loot groups"));
			} else
			{
				try
				{
					int idx = Integer.parseInt(args[1]);
					if(LootRegistry.INSTANCE.removeID(idx))
                    {
                        NetLootSync.sendSync(null);
                        sender.sendMessage(new TextComponentString("Deleted loot group with ID " + idx));
                    } else
                    {
                        sender.sendMessage(new TextComponentString("Unable to find loot group with ID " + idx));
                    }
				} catch(Exception e)
				{
					throw new WrongUsageException(getUsage(sender));
				}
			}
		} else
		{
			throw new WrongUsageException(getUsage(sender));
		}
	}
}
