package betterquesting.network.handlers;

import betterquesting.api.network.IPacketHandler;
import betterquesting.network.PacketTypeNative;
import betterquesting.network.PacketTypeRegistry;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;

public class PktHandlerBulk implements IPacketHandler
{
    @Override
    public ResourceLocation getRegistryName()
    {
        return PacketTypeNative.BULK.GetLocation();
    }
    
    @Override
    public void handleServer(NBTTagCompound tag, EntityPlayerMP sender)
    {
        NBTTagList list = tag.getTagList("bulk", 10);
        
        for(int i = 0; i < list.tagCount(); i++)
        {
            NBTTagCompound bTag = list.getCompoundTagAt(i);
            if(!bTag.hasKey("ID", 8)) continue;
            IPacketHandler handler = PacketTypeRegistry.INSTANCE.getPacketHandler(new ResourceLocation(bTag.getString("ID")));
            if(handler != null) handler.handleServer(bTag, sender);
        }
    }
    
    @Override
    public void handleClient(NBTTagCompound tag)
    {
        NBTTagList list = tag.getTagList("bulk", 10);
        
        for(int i = 0; i < list.tagCount(); i++)
        {
            NBTTagCompound bTag = list.getCompoundTagAt(i);
            if(!bTag.hasKey("ID", 8)) continue;
            IPacketHandler handler = PacketTypeRegistry.INSTANCE.getPacketHandler(new ResourceLocation(bTag.getString("ID")));
            if(handler != null) handler.handleClient(bTag);
        }
    }
}
