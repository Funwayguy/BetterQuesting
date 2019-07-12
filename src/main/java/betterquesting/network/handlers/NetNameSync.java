package betterquesting.network.handlers;

import betterquesting.core.BetterQuesting;
import betterquesting.network.PacketTypeRegistry;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class NetNameSync
{
    private static final ResourceLocation ID_NAME = new ResourceLocation("betterquesting:name_sync");
    
    public static void registerHandler()
    {
        PacketTypeRegistry.INSTANCE.registerServerHandler(ID_NAME, NetNameSync::onServer);
        
        if(BetterQuesting.proxy.isClient())
        {
            PacketTypeRegistry.INSTANCE.registerClientHandler(ID_NAME, NetNameSync::onClient);
        }
    }
    
    // TODO: Figure out how to deal with this. Party basis or on request? Request by username or UUID?
    
    private static void onServer(Tuple<NBTTagCompound, EntityPlayerMP> message)
    {
    }
    
    @SideOnly(Side.CLIENT)
    private static void onClient(NBTTagCompound message)
    {
    }
}
