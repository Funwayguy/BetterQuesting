package betterquesting.api.questing;

import net.minecraft.nbt.NBTTagCompound;
import betterquesting.api.misc.IDataSync;
import betterquesting.api.misc.INBTSaveLoad;
import betterquesting.api.properties.IPropertyContainer;
import betterquesting.api.storage.IRegStorageBase;

public interface IQuestLine extends IDataSync, INBTSaveLoad<NBTTagCompound>, IRegStorageBase<Integer,IQuestLineEntry>
{
	public String getUnlocalisedName();
	public String getUnlocalisedDescription();
	
	// Defaults to the API if not used
	public void setParentDatabase(IQuestLineDatabase questDB);
	
	public IPropertyContainer getProperties();
	
	public int getQuestAt(int x, int y);
	
	public IQuestLineEntry createNewEntry();
}
