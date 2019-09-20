package betterquesting.commands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

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
	    if(!(sender instanceof EntityPlayer)) return;
	    
	    EntityPlayer player = (EntityPlayer)sender;
	    
	    IAttributeInstance hpBase = player.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH);
	    int hpDiff = (int)Math.floor(hpBase.getAttributeValue() - hpBase.getBaseValue());
	    IAttributeInstance atkBase = player.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
	    int atkDiff = (int)Math.floor(atkBase.getAttributeValue() - atkBase.getBaseValue());
	    IAttributeInstance defBase = player.getEntityAttribute(SharedMonsterAttributes.ARMOR);
	    int defDiff = (int)Math.floor(defBase.getAttributeValue() - defBase.getBaseValue());
	    IAttributeInstance spdBase = player.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
	    int spdDiff = (int)Math.floor((spdBase.getAttributeValue() - spdBase.getBaseValue()) * 100D);
	    
	    sender.sendMessage(new TextComponentString("HP: " + hpBase.getAttributeValue() + " (+" + hpDiff + ")"));
	    sender.sendMessage(new TextComponentString("ATK: " + atkBase.getAttributeValue() + " (+" + atkDiff + ")"));
	    sender.sendMessage(new TextComponentString("DEF: " + defBase.getAttributeValue() + " (+" + defDiff + ")"));
	    sender.sendMessage(new TextComponentString("SPD: " + Math.floor(spdBase.getAttributeValue() * 100) + " (+" + spdDiff + ")"));
    }
}
