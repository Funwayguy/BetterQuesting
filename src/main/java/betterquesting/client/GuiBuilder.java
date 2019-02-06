package betterquesting.client;

import betterquesting.api.client.gui.misc.IGuiHook;
import betterquesting.api.misc.ICallback;
import betterquesting.api.nbt_doc.INbtDoc;
import betterquesting.api.utils.BigItemStack;
import betterquesting.client.gui2.GuiHome;
import betterquesting.client.gui2.editors.GuiFileBrowser;
import betterquesting.client.gui2.editors.GuiTextEditor;
import betterquesting.client.gui2.editors.nbt.GuiEntitySelection;
import betterquesting.client.gui2.editors.nbt.GuiFluidSelection;
import betterquesting.client.gui2.editors.nbt.GuiItemSelection;
import betterquesting.client.gui2.editors.nbt.GuiNbtEditor;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fluids.FluidStack;

import java.io.File;
import java.io.FileFilter;

public final class GuiBuilder implements IGuiHook
{
	public static final GuiBuilder INSTANCE = new GuiBuilder();
	
	@Override
    public GuiScreen getHomeScreen(GuiScreen parent)
    {
        return new GuiHome(parent);
    }
	
	@Override
	@SuppressWarnings("unchecked")
	public <T extends NBTBase> GuiScreen getNbtEditor(GuiScreen parent, ICallback<T> callback, T json, INbtDoc jdoc)
	{
		if(json.getId() == 9)
		{
			return new GuiNbtEditor(parent, (NBTTagList)json, (ICallback<NBTTagList>)callback);
		} else if(json.getId() == 10)
		{
			return new GuiNbtEditor(parent, (NBTTagCompound)json, (ICallback<NBTTagCompound>)callback);
		}
		
		return null;
	}
	
	@Override
	public GuiScreen getItemEditor(GuiScreen parent, ICallback<BigItemStack> callback, BigItemStack stack)
	{
		return new GuiItemSelection(parent, stack, callback);
	}
	
	@Override
	public GuiScreen getFluidEditor(GuiScreen parent, ICallback<FluidStack> callback, FluidStack stack)
	{
		return new GuiFluidSelection(parent, stack, callback);
	}
	
	@Override
	public GuiScreen getEntityEditor(GuiScreen parent, ICallback<Entity> callback, Entity entity)
	{
		return new GuiEntitySelection(parent, entity, callback);
	}
	
	@Override
	public GuiScreen getTextEditor(GuiScreen parent, ICallback<String> callback, String text)
	{
		return new GuiTextEditor(parent, text, callback);
	}
	
	@Override
	public GuiScreen getFileExplorer(GuiScreen parent, ICallback<File[]> callback, File rootDir, FileFilter filter, boolean multiSelect)
	{
		return new GuiFileBrowser(parent, callback, rootDir, filter).allowMultiSelect(multiSelect);
	}
	
}
