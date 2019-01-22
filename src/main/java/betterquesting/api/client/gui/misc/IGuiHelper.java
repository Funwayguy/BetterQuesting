package betterquesting.api.client.gui.misc;

import betterquesting.api.jdoc.IJsonDoc;
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

@Deprecated
@SideOnly(Side.CLIENT)
public interface IGuiHelper
{
    <T extends NBTBase> void openJsonEditor(GuiScreen parent, ICallback<T> callback, T json, IJsonDoc jdoc);
    
    void openItemEditor(GuiScreen parent, ICallback<BigItemStack> callback, BigItemStack stack);
    
    void openFluidEditor(GuiScreen parent, ICallback<FluidStack> callback, FluidStack stack);
    
    void openEntityEditor(GuiScreen parent, ICallback<Entity> callback, Entity entity);
    
    void openTextEditor(GuiScreen parent, ICallback<String> editor, String text);
    
    void openFileExplorer(GuiScreen parent, ICallback<File[]> callback, File rootDir, FileFilter filter, boolean multiSelect);
}