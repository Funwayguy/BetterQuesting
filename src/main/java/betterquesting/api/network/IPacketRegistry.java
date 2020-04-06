package betterquesting.api.network;

import betterquesting.api2.utils.Tuple2;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;

public interface IPacketRegistry
{
    void registerServerHandler(@Nonnull ResourceLocation idName, @Nonnull Consumer<Tuple2<NBTTagCompound, EntityPlayerMP>> method);
    
    @SideOnly(Side.CLIENT)
    void registerClientHandler(@Nonnull ResourceLocation idName, @Nonnull Consumer<NBTTagCompound> method);
    
    @Nullable
    Consumer<Tuple2<NBTTagCompound,EntityPlayerMP>> getServerHandler(@Nonnull ResourceLocation idName);
    
    @Nullable
    @SideOnly(Side.CLIENT)
    Consumer<NBTTagCompound> getClientHandler(@Nonnull ResourceLocation idName);
}
