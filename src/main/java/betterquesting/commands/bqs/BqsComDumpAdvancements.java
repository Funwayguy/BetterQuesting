package betterquesting.commands.bqs;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;

public class BqsComDumpAdvancements extends CommandBase
{
    @Override
    public String getName()
    {
        return "bqs_advancement_dump";
    }
    
    @Override
    public String getUsage(ICommandSender sender)
    {
        return "/bqs_advancement_dump";
    }
	
	@Override
    public int getRequiredPermissionLevel()
    {
        return 2;
    }
    
    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if(args.length != 0)
        {
			throw new WrongUsageException(getUsage(sender));
        }
        
        AdvancementDump.INSTANCE.dumpAdvancements(server);
    }
}
