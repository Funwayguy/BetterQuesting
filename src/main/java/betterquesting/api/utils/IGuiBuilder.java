package betterquesting.api.utils;

import java.io.File;
import java.io.FileFilter;
import net.minecraft.client.gui.GuiScreen;
import betterquesting.api.client.IFileCallback;
import betterquesting.api.client.jdoc.IJsonDoc;
import betterquesting.api.quests.IQuest;
import betterquesting.api.quests.IQuestLine;
import betterquesting.client.gui.misc.ITextCallback;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public interface IGuiBuilder
{
	public GuiScreen getJsonEditor(GuiScreen parent, JsonElement json, IJsonDoc jdoc);
	public GuiScreen getItemEditor(GuiScreen parent, JsonObject json);
	public GuiScreen getFluidEditor(GuiScreen parent, JsonObject json);
	public GuiScreen getEntityEditor(GuiScreen parent, JsonObject json);
	
	public GuiScreen getQuestEditor(GuiScreen parent, IQuest quest);
	public GuiScreen getLineEditor(GuiScreen parent, IQuestLine questLine);
	
	public GuiScreen getTextEditor(GuiScreen parent, String text, ITextCallback editor, int id);
	public GuiScreen getFileExplorer(GuiScreen parent, IFileCallback callback, File rootDir, FileFilter filter, boolean multiSelect);
}
