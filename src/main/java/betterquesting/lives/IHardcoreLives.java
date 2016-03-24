package betterquesting.lives;

import net.minecraft.nbt.NBTTagCompound;

public interface IHardcoreLives
{
	public int getLives();
	public void setLives(int num);
	public NBTTagCompound writeToNBT();
	public void readFromNBT(NBTTagCompound nbt);
}
