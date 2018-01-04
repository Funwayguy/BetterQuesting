package betterquesting.api.questing;

import net.minecraft.nbt.NBTTagCompound;
import betterquesting.api.misc.INBTSaveLoad;

public interface IQuestLineEntry extends INBTSaveLoad<NBTTagCompound>
{
	public int getSize();
	public int getPosX();
	public int getPosY();
	
	public void setPosition(int posX, int posY);
	public void setSize(int size);
}
