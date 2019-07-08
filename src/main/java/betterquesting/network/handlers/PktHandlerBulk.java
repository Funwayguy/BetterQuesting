package betterquesting.network.handlers;

import betterquesting.api.network.IPacketHandler;
import betterquesting.network.PacketTypeNative;
import betterquesting.network.PacketTypeRegistry;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;

import java.util.function.Consumer;

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
            Consumer<Tuple<NBTTagCompound,EntityPlayerMP>> handler = PacketTypeRegistry.INSTANCE.getServerHandler(new ResourceLocation(bTag.getString("ID")));
            if(handler != null) handler.accept(new Tuple<>(bTag, sender));
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
            Consumer<NBTTagCompound> handler = PacketTypeRegistry.INSTANCE.getClientHandler(new ResourceLocation(bTag.getString("ID")));
            if(handler != null) handler.accept(bTag);
        }
    }
}
