package betterquesting.api.network;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;

public interface IPacketRegistry {
    void registerServerHandler(@Nonnull ResourceLocation idName, @Nonnull Consumer<Tuple<NBTTagCompound, EntityPlayerMP>> method);

    @SideOnly(Side.CLIENT)
    void registerClientHandler(@Nonnull ResourceLocation idName, @Nonnull Consumer<NBTTagCompound> method);

    @Nullable
    Consumer<Tuple<NBTTagCompound, EntityPlayerMP>> getServerHandler(@Nonnull ResourceLocation idName);

    @Nullable
    @SideOnly(Side.CLIENT)
    Consumer<NBTTagCompound> getClientHandler(@Nonnull ResourceLocation idName);
}
