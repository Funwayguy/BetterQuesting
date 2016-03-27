package betterquesting.lives;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

// This is the replaced version IEEP
public class LifeCapability implements IHardcoreLives, ICapabilitySerializable<NBTTagCompound>
{
	int lives = 3;
	
	public LifeCapability()
	{
		lives = LifeManager.defLives;
	}
	
	@Override
	public int getLives()
	{
		return lives;
	}
	
	@Override
	public void setLives(int num)
	{
		lives = num;
	}
	
	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing)
	{
		return LifeManager.LIFE_CAP != null && capability == LifeManager.LIFE_CAP;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <T> T getCapability(Capability<T> capability, EnumFacing facing)
	{
		if(LifeManager.LIFE_CAP != null && capability == LifeManager.LIFE_CAP)
		{
			return (T)this;
		}
		
		return null;
	}

	@Override
	public NBTTagCompound serializeNBT()
	{
		return this.writeToNBT();
	}

	@Override
	public void deserializeNBT(NBTTagCompound nbt)
	{
		this.readFromNBT(nbt);
	}

	@Override
	public NBTTagCompound writeToNBT()
	{
		NBTTagCompound tags = new NBTTagCompound();
		tags.setInteger("lives", lives);
		return tags;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		lives = nbt.hasKey("lives")? nbt.getInteger("lives") : LifeManager.defLives;
	}
}
