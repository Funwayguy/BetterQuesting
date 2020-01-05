package betterquesting.network;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;

public class PacketQuesting
{
	private final CompoundNBT tags;
	
    public PacketQuesting(CompoundNBT tags)
	{
		this.tags = tags;
	}
	
	public PacketQuesting(PacketBuffer buf)
    {
        this.tags = buf.readCompoundTag();
    }
    
    public CompoundNBT getTags()
    {
        return this.tags;
    }

	public void toBytes(PacketBuffer buf)
	{
		buf.writeCompoundTag(tags);
	}
}
