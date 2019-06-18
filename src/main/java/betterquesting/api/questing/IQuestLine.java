package betterquesting.api.questing;

import betterquesting.api.properties.IPropertyContainer;
import betterquesting.api2.storage.DBEntry;
import betterquesting.api2.storage.IDatabase;
import betterquesting.api2.storage.INBTPartial;
import net.minecraft.nbt.NBTTagCompound;

public interface IQuestLine extends IDatabase<IQuestLineEntry>, INBTPartial<NBTTagCompound, Integer>, IPropertyContainer
{
    IQuestLineEntry createNew(int id);
    
	String getUnlocalisedName();
	String getUnlocalisedDescription();
	
	// Defaults to the API if not used
	void setParentDatabase(IQuestLineDatabase questDB);
	
	DBEntry<IQuestLineEntry> getEntryAt(int x, int y);
}
