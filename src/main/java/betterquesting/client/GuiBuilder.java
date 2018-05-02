package betterquesting.client;

import java.io.File;
import java.io.FileFilter;

import betterquesting.client.gui2.editors.GuiTextEditor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fluids.FluidStack;
import betterquesting.api.client.gui.misc.IGuiHelper;
import betterquesting.api.jdoc.IJsonDoc;
import betterquesting.api.misc.ICallback;
import betterquesting.api.misc.IMultiCallback;
import betterquesting.api.utils.BigItemStack;
import betterquesting.client.gui.editors.json.GuiJsonEntitySelection;
import betterquesting.client.gui.editors.json.GuiJsonFluidSelection;
import betterquesting.client.gui.editors.json.GuiJsonItemSelection;
import betterquesting.client.gui.editors.json.scrolling.GuiJsonEditor;
import betterquesting.client.gui.misc.GuiFileExplorer;

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
			mc.displayGuiScreen(new GuiJsonEditor(parent, (NBTTagList)json, jdoc, (ICallback<NBTTagList>)callback));
		} else if(json.getId() == 10)
		{
			mc.displayGuiScreen(new GuiJsonEditor(parent, (NBTTagCompound)json, jdoc, (ICallback<NBTTagCompound>)callback));
		}
		
		return;
	}
	
	@Override
	public void openItemEditor(GuiScreen parent, ICallback<BigItemStack> callback, BigItemStack stack)
	{
		mc.displayGuiScreen(new GuiJsonItemSelection(parent, callback, stack));
	}
	
	@Override
	public void openFluidEditor(GuiScreen parent, ICallback<FluidStack> callback, FluidStack stack)
	{
		mc.displayGuiScreen(new GuiJsonFluidSelection(parent, callback, stack));
	}
	
	@Override
	public void openEntityEditor(GuiScreen parent, ICallback<Entity> callback, Entity entity)
	{
		mc.displayGuiScreen(new GuiJsonEntitySelection(parent, callback, entity));
	}
	
	@Override
	public void openTextEditor(GuiScreen parent, ICallback<String> callback, String text)
	{
		mc.displayGuiScreen(new GuiTextEditor(parent, text, callback));
	}
	
	@Override
	public void openFileExplorer(GuiScreen parent, IMultiCallback<File> callback, File rootDir, FileFilter filter, boolean multiSelect)
	{
		mc.displayGuiScreen(new GuiFileExplorer(parent, callback, rootDir, filter).setMultiSelect(multiSelect));
	}
	
}
