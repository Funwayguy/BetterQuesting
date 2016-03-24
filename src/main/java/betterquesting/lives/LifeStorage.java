package betterquesting.lives;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;

public class LifeStorage implements IStorage<IHardcoreLives>
{
	@Override
	public NBTBase writeNBT(Capability<IHardcoreLives> capability, IHardcoreLives instance, EnumFacing side)
	{
		return instance.writeToNBT();
	}

	@Override
	public void readNBT(Capability<IHardcoreLives> capability, IHardcoreLives instance, EnumFacing side, NBTBase nbt)
	{
		if(!(nbt instanceof NBTTagCompound))
		{
			return;
		}
		
		instance.readFromNBT((NBTTagCompound)nbt);
	}
}
