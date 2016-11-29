package betterquesting.api.client.gui.misc;

import java.io.File;
import java.io.FileFilter;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import betterquesting.api.jdoc.IJsonDoc;
import betterquesting.api.misc.ICallback;
import betterquesting.api.misc.IMultiCallback;
import betterquesting.api.utils.BigItemStack;
import com.google.gson.JsonElement;

@SideOnly(Side.CLIENT)
public interface IGuiHelper
{
	public <T extends JsonElement> void openJsonEditor(GuiScreen parent, ICallback<T> callback, T json, IJsonDoc jdoc);
	public void openItemEditor(GuiScreen parent, ICallback<BigItemStack> callback, BigItemStack stack);
	public void openFluidEditor(GuiScreen parent, ICallback<FluidStack> callback, FluidStack stack);
	public void openEntityEditor(GuiScreen parent, ICallback<Entity> callback, Entity entity);
	
	public void openTextEditor(GuiScreen parent, ICallback<String> editor, String text);
	public void openFileExplorer(GuiScreen parent, IMultiCallback<File> callback, File rootDir, FileFilter filter, boolean multiSelect);
}