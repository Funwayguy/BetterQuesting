package betterquesting.api.storage;

import betterquesting.api2.storage.INBTPartial;
import net.minecraft.nbt.NBTTagCompound;

import java.util.UUID;

public interface ILifeDatabase extends INBTPartial<NBTTagCompound, UUID>
{
	int getLives(UUID uuid);
	void setLives(UUID uuid, int value);
	
	void reset();
}
