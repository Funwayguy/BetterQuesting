package betterquesting.api.storage;

import betterquesting.api.misc.IDataSync;
import betterquesting.api.questing.party.IParty;
import betterquesting.api2.storage.INBTProgress;
import net.minecraft.nbt.NBTTagCompound;

import java.util.UUID;

public interface ILifeDatabase extends INBTProgress<NBTTagCompound>, IDataSync
{
	int getLives(UUID uuid);
	void setLives(UUID uuid, int value);
	
	int getLives(IParty party);
	void setLives(IParty party, int value);
	
	void reset();
}
