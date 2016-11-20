package betterquesting.client;

import java.io.File;
import java.io.FileFilter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import betterquesting.api.client.gui.IGuiHelper;
import betterquesting.api.jdoc.IJsonDoc;
import betterquesting.api.other.IFileCallback;
import betterquesting.api.other.ITextCallback;
import betterquesting.client.gui.editors.GuiTextEditor;
import betterquesting.client.gui.editors.json.GuiJsonArray;
import betterquesting.client.gui.editors.json.GuiJsonEntitySelection;
import betterquesting.client.gui.editors.json.GuiJsonFluidSelection;
import betterquesting.client.gui.editors.json.GuiJsonItemSelection;
import betterquesting.client.gui.editors.json.GuiJsonObject;
import betterquesting.client.gui.misc.GuiFileExplorer;
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
	public void openJsonEditor(GuiScreen parent, JsonElement json, IJsonDoc jdoc)
	{
		if(json.isJsonArray())
		{
			mc.displayGuiScreen(new GuiJsonArray(parent, json.getAsJsonArray(), jdoc));
		} else if(json.isJsonObject())
		{
			mc.displayGuiScreen(new GuiJsonObject(parent, json.getAsJsonObject(), jdoc));
		}
		
		return;
	}
	
	@Override
	public void openItemEditor(GuiScreen parent, JsonObject json)
	{
		mc.displayGuiScreen(new GuiJsonItemSelection(parent, json));
	}
	
	@Override
	public void openFluidEditor(GuiScreen parent, JsonObject json)
	{
		mc.displayGuiScreen(new GuiJsonFluidSelection(parent, json));
	}
	
	@Override
	public void openEntityEditor(GuiScreen parent, JsonObject json)
	{
		mc.displayGuiScreen(new GuiJsonEntitySelection(parent, json));
	}
	
	@Override
	public void openTextEditor(GuiScreen parent, ITextCallback editor, String text)
	{
		GuiTextEditor gui = new GuiTextEditor(parent, text);
		
		if(editor != null)
		{
			gui.setHost(editor);
		}
		
		mc.displayGuiScreen(gui);
	}
	
	@Override
	public void openFileExplorer(GuiScreen parent, IFileCallback callback, File rootDir, FileFilter filter, boolean multiSelect)
	{
		mc.displayGuiScreen(new GuiFileExplorer(parent, callback, rootDir, filter).setMultiSelect(multiSelect));
	}
	
}
