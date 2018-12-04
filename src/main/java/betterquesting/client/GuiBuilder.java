package betterquesting.client;

import betterquesting.api.client.gui.misc.IGuiHelper;
import betterquesting.api.jdoc.IJsonDoc;
import betterquesting.api.misc.ICallback;
import betterquesting.api.utils.BigItemStack;
import betterquesting.client.gui2.editors.GuiFileBrowser;
import betterquesting.client.gui2.editors.GuiTextEditor;
import betterquesting.client.gui2.editors.nbt.GuiEntitySelection;
import betterquesting.client.gui2.editors.nbt.GuiFluidSelection;
import betterquesting.client.gui2.editors.nbt.GuiItemSelection;
import betterquesting.client.gui2.editors.nbt.GuiNbtEditor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fluids.FluidStack;

import java.io.File;
import java.io.FileFilter;

public final class GuiBuilder implements IGuiHelper
{
	public static final GuiBuilder INSTANCE = new GuiBuilder();
	
	private final Minecraft mc;
	
	private GuiBuilder()
	{
		this.mc = Minecraft.getMinecraft();
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <T extends NBTBase> void openJsonEditor(GuiScreen parent, ICallback<T> callback, T json, IJsonDoc jdoc)
	{
		if(json.getId() == 9)
		{
			mc.displayGuiScreen(new GuiNbtEditor(parent, (NBTTagList)json, (ICallback<NBTTagList>)callback));
		} else if(json.getId() == 10)
		{
			mc.displayGuiScreen(new GuiNbtEditor(parent, (NBTTagCompound)json, (ICallback<NBTTagCompound>)callback));
		}
	}
	
	@Override
	public void openItemEditor(GuiScreen parent, ICallback<BigItemStack> callback, BigItemStack stack)
	{
		mc.displayGuiScreen(new GuiItemSelection(parent, stack, callback));
	}
	
	@Override
	public void openFluidEditor(GuiScreen parent, ICallback<FluidStack> callback, FluidStack stack)
	{
		mc.displayGuiScreen(new GuiFluidSelection(parent, stack, callback));
	}
	
	@Override
	public void openEntityEditor(GuiScreen parent, ICallback<Entity> callback, Entity entity)
	{
		mc.displayGuiScreen(new GuiEntitySelection(parent, entity, callback));
	}
	
	@Override
	public void openTextEditor(GuiScreen parent, ICallback<String> callback, String text)
	{
		mc.displayGuiScreen(new GuiTextEditor(parent, text, callback));
	}
	
	@Override
	public void openFileExplorer(GuiScreen parent, ICallback<File[]> callback, File rootDir, FileFilter filter, boolean multiSelect)
	{
		mc.displayGuiScreen(new GuiFileBrowser(parent, callback, rootDir, filter).allowMultiSelect(multiSelect));
	}
	
}
