package betterquesting.api.client.gui;

import java.io.File;
import java.io.FileFilter;
import net.minecraft.client.gui.GuiScreen;
import betterquesting.api.jdoc.IJsonDoc;
import betterquesting.api.other.IFileCallback;
import betterquesting.api.other.ITextCallback;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public interface IGuiHelper
{
	public void openJsonEditor(GuiScreen parent, JsonElement json, IJsonDoc jdoc);
	public void openItemEditor(GuiScreen parent, JsonObject json);
	public void openFluidEditor(GuiScreen parent, JsonObject json);
	public void openEntityEditor(GuiScreen parent, JsonObject json);
	
	public void openTextEditor(GuiScreen parent, ITextCallback editor, String text);
	public void openFileExplorer(GuiScreen parent, IFileCallback callback, File rootDir, FileFilter filter, boolean multiSelect);
}