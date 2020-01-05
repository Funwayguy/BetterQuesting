package betterquesting.handlers;

import betterquesting.client.gui2.GuiQuestHelp;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class GuiHandler implements IGuiHandler
{
	@Override
	public Object getServerGuiElement(int ID, PlayerEntity player, World world, int x, int y, int z)
	{
		TileEntity tile = world.getTileEntity(new BlockPos(x, y, z));
		
		/*if(ID == 0 && tile instanceof TileSubmitStation)
		{
			return new ContainerSubmitStation(player.inventory, (TileSubmitStation)tile);
		}*/
		
		return null;
	}
	
	@Override
	public Object getClientGuiElement(int ID, PlayerEntity player, World world, int x, int y, int z)
	{
		TileEntity tile = world.getTileEntity(new BlockPos(x, y, z));
		
		/*if(ID == 0 && tile instanceof TileSubmitStation)
		{
            return new GuiSubmitStation(null, player.inventory, (TileSubmitStation)tile);
		} else */if(ID == 1)
		{
			return new GuiQuestHelp(null);
		}
		
		return null;
	}
	
}
