package betterquesting.api.misc;

import betterquesting.api.network.QuestingPacket;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public interface IDataSync
{
    // TODO: Just make this return a packet to send to a purpose made handler. We don't need the interface to deal with the recieving end. It's redundant and a waste of code.
    
    @Deprecated
	QuestingPacket getSyncPacket();
	QuestingPacket getSyncPacket(@Nullable List<UUID> users);
	@Deprecated
	void readPacket(NBTTagCompound payload);
}
