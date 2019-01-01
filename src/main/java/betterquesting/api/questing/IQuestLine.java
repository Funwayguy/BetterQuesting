package betterquesting.api.questing;

import betterquesting.api.misc.IDataSync;
import betterquesting.api.properties.IPropertyContainer;
import betterquesting.api2.storage.DBEntry;
import betterquesting.api2.storage.IDatabase;
import betterquesting.api2.storage.INBTSaveLoad;
import net.minecraft.nbt.NBTTagCompound;

public interface IQuestLine extends IDataSync, IDatabase<IQuestLineEntry>, INBTSaveLoad<NBTTagCompound>
{
	String getUnlocalisedName();
	String getUnlocalisedDescription();
	
	// Defaults to the API if not used
	void setParentDatabase(IQuestLineDatabase questDB);
	
	IPropertyContainer getProperties();
	
	DBEntry<IQuestLineEntry> getEntryAt(int x, int y);
}
