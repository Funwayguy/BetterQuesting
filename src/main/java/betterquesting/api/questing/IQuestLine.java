package betterquesting.api.questing;

import betterquesting.api2.storage.IDatabaseNBT;
import net.minecraft.nbt.NBTTagCompound;
import betterquesting.api.misc.IDataSync;
import betterquesting.api.properties.IPropertyContainer;

public interface IQuestLine extends IDataSync, IDatabaseNBT<IQuestLineEntry, NBTTagCompound>
{
	String getUnlocalisedName();
	String getUnlocalisedDescription();
	
	// Defaults to the API if not used
	void setParentDatabase(IQuestLineDatabase questDB);
	
	IPropertyContainer getProperties();
	
	int getQuestAt(int x, int y);
	
	IQuestLineEntry createNewEntry(int id);
}
