package adv_director.client;

import java.io.File;
import java.io.FileFilter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;
import net.minecraftforge.fluids.FluidStack;
import adv_director.api.client.gui.misc.IGuiHelper;
import adv_director.api.jdoc.IJsonDoc;
import adv_director.api.misc.ICallback;
import adv_director.api.misc.IMultiCallback;
import adv_director.api.utils.BigItemStack;
import adv_director.client.gui.editors.GuiTextEditor;
import adv_director.client.gui.editors.json.GuiJsonEntitySelection;
import adv_director.client.gui.editors.json.GuiJsonFluidSelection;
import adv_director.client.gui.editors.json.GuiJsonItemSelection;
import adv_director.client.gui.editors.json.scrolling.GuiJsonEditor;
import adv_director.client.gui.misc.GuiFileExplorer;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

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
	public <T extends JsonElement> void openJsonEditor(GuiScreen parent, ICallback<T> callback, T json, IJsonDoc jdoc)
	{
		if(json.isJsonArray())
		{
			mc.displayGuiScreen(new GuiJsonEditor(parent, json.getAsJsonArray(), jdoc, (ICallback<JsonArray>)callback));
		} else if(json.isJsonObject())
		{
			mc.displayGuiScreen(new GuiJsonEditor(parent, json.getAsJsonObject(), jdoc, (ICallback<JsonObject>)callback));
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
		GuiTextEditor gui = new GuiTextEditor(parent, text);
		
		if(callback != null)
		{
			gui.setHost(callback);
		}
		
		mc.displayGuiScreen(gui);
	}
	
	@Override
	public void openFileExplorer(GuiScreen parent, IMultiCallback<File> callback, File rootDir, FileFilter filter, boolean multiSelect)
	{
		mc.displayGuiScreen(new GuiFileExplorer(parent, callback, rootDir, filter).setMultiSelect(multiSelect));
	}
	
}
