package betterquesting.api.questing;

import betterquesting.api2.storage.INBTSaveLoad;
import net.minecraft.nbt.NBTTagCompound;

public interface IQuestLineEntry extends INBTSaveLoad<NBTTagCompound>
{
	int getSize();
	int getPosX();
	int getPosY();
	
	void setPosition(int posX, int posY);
	void setSize(int size);
}
