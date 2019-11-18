package betterquesting.commands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

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
	    /*if(!(sender instanceof EntityPlayer)) return;
	    
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
	    sender.sendMessage(new TextComponentString("SPD: " + Math.floor(spdBase.getAttributeValue() * 100) + " (+" + spdDiff + ")"));*/
	    
	    /*File fileIn = new File("D:/Jon Stuff/Github/Repositories/BetterQuesting - 1.12/jars", "fix_me.lang");
	    File fileOut = new File("D:/Jon Stuff/Github/Repositories/BetterQuesting - 1.12/jars", "ru_RU.lang");
	    
	    try
        {
            if(fileOut.exists())
            {
                fileOut.delete();
            } else if(fileOut.getParentFile() != null)
            {
                fileOut.getParentFile().mkdirs();
            }
            
            fileOut.createNewFile();
        } catch(Exception e)
        {
            e.printStackTrace();
            return;
        }
	    
	    try(FileOutputStream fos = new FileOutputStream(fileOut); OutputStreamWriter fw = new OutputStreamWriter(fos, StandardCharsets.UTF_8))
        {
            Stream<String> linesIn = Files.lines(fileIn.toPath());
            Iterator<String> lineIter = linesIn.iterator();
            
            while(lineIter.hasNext())
            {
                String line = lineIter.next();
                if(line == null || line.startsWith("#")) continue;
                
                String[] split = line.split("=", 2);
                
                fw.write(split.length < 2 ? "\n" : (split[0].trim() + "=" + split[1].trim() + "\n"));
            }
        } catch(Exception e)
        {
            e.printStackTrace();
        }*/
    }
}
