package betterquesting.api.questing;

import net.minecraft.nbt.NBTTagList;
import betterquesting.api.misc.IDataSync;
import betterquesting.api.misc.INBTSaveLoad;
import betterquesting.api.storage.IRegStorageBase;

public interface IQuestLineDatabase extends IRegStorageBase<Integer,IQuestLine>, INBTSaveLoad<NBTTagList>, IDataSync
{
	/**
	 * Deletes quest from all quest lines
	 */
	public void removeQuest(int lineID);
	
	public int getOrderIndex(int lineID);
	public void setOrderIndex(int lineID, int index);
	
	public IQuestLine createNew();
}
