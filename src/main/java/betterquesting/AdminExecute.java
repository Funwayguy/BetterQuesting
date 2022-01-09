package betterquesting;

import net.minecraft.command.CommandResultStats.Type;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

/**
 * Elevates the player's privileges to OP level for use in command rewards
 */
public class AdminExecute implements ICommandSender
{
	private final EntityPlayer player;
	
	public AdminExecute(EntityPlayer player)
	{
		this.player = player;
	}
 
	@Nonnull
	@Override
	public String getName()
	{
		return player.getName();
	}
 
	@Nonnull
	@Override
	public ITextComponent getDisplayName()
	{
		return player.getDisplayName();
	}

	@Override
	public void sendMessage(ITextComponent p_145747_1_)
	{
		player.sendMessage(p_145747_1_);
	}

	@Override
	public boolean canUseCommand(int p_70003_1_, @Nonnull String p_70003_2_)
	{
		return true;
	}
 
	@Nonnull
	@Override
	public BlockPos getPosition()
	{
		return player.getPosition();
	}
 
	@Nonnull
	@Override
	public World getEntityWorld()
	{
		return player.getEntityWorld();
	}
	
	@Nonnull
	@Override
	public Vec3d getPositionVector()
	{
		return player.getPositionVector();
	}
	
	@Override
	public Entity getCommandSenderEntity()
	{
		return player.getCommandSenderEntity();
	}

	@Override
	public boolean sendCommandFeedback()
	{
		return player.sendCommandFeedback();
	}

	@Override
	public void setCommandStat(Type type, int amount)
	{
		player.setCommandStat(type, amount);
	}

	@Override
	public MinecraftServer getServer()
	{
		return player.getServer();
	}
}
