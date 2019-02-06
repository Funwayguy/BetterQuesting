package betterquesting.api.client.gui.misc;

import betterquesting.api.nbt_doc.INbtDoc;
import betterquesting.api.misc.ICallback;
import betterquesting.api.utils.BigItemStack;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTBase;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.File;
import java.io.FileFilter;

@SideOnly(Side.CLIENT)
public interface IGuiHook
{
    GuiScreen getHomeScreen(GuiScreen parent);
    
    <T extends NBTBase> GuiScreen getNbtEditor(GuiScreen parent, ICallback<T> callback, T nbt, INbtDoc nbtDoc);
    
    GuiScreen getItemEditor(GuiScreen parent, ICallback<BigItemStack> callback, BigItemStack stack);
    
    GuiScreen getFluidEditor(GuiScreen parent, ICallback<FluidStack> callback, FluidStack stack);
    
    GuiScreen getEntityEditor(GuiScreen parent, ICallback<Entity> callback, Entity entity);
    
    GuiScreen getTextEditor(GuiScreen parent, ICallback<String> callback, String text);
    
    GuiScreen getFileExplorer(GuiScreen parent, ICallback<File[]> callback, File rootDir, FileFilter filter, boolean multiSelect);
}