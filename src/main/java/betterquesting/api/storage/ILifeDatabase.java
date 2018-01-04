package betterquesting.api.storage;

import java.util.UUID;
import net.minecraft.nbt.NBTTagCompound;
import betterquesting.api.misc.IDataSync;
import betterquesting.api.misc.INBTSaveLoad;
import betterquesting.api.questing.party.IParty;

public interface ILifeDatabase extends INBTSaveLoad<NBTTagCompound>, IDataSync
{
	public int getLives(UUID uuid);
	public void setLives(UUID uuid, int value);
	
	public int getLives(IParty party);
	public void setLives(IParty party, int value);
}
