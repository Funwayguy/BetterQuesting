package adv_director.commands;

import adv_director.rw2.api.d_script.ExpressionParser;
import adv_director.rw2.api.d_script.IExpression;
import adv_director.rw2.api.d_script.ScriptScope;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

public class BQ_CommandDebug extends CommandBase
{
	@Override
	public String getCommandName()
	{
		return "bq_debug";
	}
	
	@Override
	public String getCommandUsage(ICommandSender sender)
	{
		return "TO BE USED IN DEV ONLY";
	}
	
	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
	{
		String equation = "";
		
		if(args == null || args.length <= 0)
		{
			return;
		}
		
		equation += args[0];
		
		for(int i = 1; i < args.length; i++)
		{
			equation += " " + args[i];
		}
		
		try
		{
			sender.addChatMessage(new TextComponentString("Equation: " + equation));
			IExpression<Object> exp = ExpressionParser.parse(equation, Object.class);
			sender.addChatMessage(new TextComponentString("Result: " + exp.eval(new ScriptScope()).toString()));
		} catch(Exception e)
		{
			e.printStackTrace();
			throw new CommandException("An error occured while parsing equation");
		}
	}
}
