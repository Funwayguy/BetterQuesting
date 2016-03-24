package betterquesting.lives;

import net.minecraft.nbt.NBTTagCompound;

public class LifeTrackerDefault implements IHardcoreLives
{
	@Override
	public int getLives()
	{
		return 3;
	}
	
	@Override
	public void setLives(int num)
	{
	}

	@Override
	public NBTTagCompound writeToNBT()
	{
		NBTTagCompound tags = new NBTTagCompound();
		tags.setInteger("lives", 3);
		return tags;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
	}
}
