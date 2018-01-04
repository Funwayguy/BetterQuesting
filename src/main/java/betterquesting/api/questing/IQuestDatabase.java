package betterquesting.api.questing;

import net.minecraft.nbt.NBTTagList;
import betterquesting.api.misc.IDataSync;
import betterquesting.api.misc.INBTSaveLoad;
import betterquesting.api.storage.IRegStorageBase;

public interface IQuestDatabase extends IRegStorageBase<Integer,IQuest>, INBTSaveLoad<NBTTagList>, IDataSync
{
	public IQuest createNew();
}
