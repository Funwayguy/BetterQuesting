package betterquesting.api.questing;

import betterquesting.api2.storage.INBTSaveLoad;
import net.minecraft.nbt.CompoundNBT;

public interface IQuestLineEntry extends INBTSaveLoad<CompoundNBT>
{
    @Deprecated
	int getSize();
    
    int getSizeX();
    int getSizeY();
	int getPosX();
	int getPosY();
	
	void setPosition(int posX, int posY);
	
	@Deprecated
	void setSize(int size);
	
	void setSize(int sizeX, int sizeY);
}
