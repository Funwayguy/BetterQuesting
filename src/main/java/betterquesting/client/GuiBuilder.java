package betterquesting.client;

import java.io.File;
import java.io.FileFilter;
import net.minecraft.client.gui.GuiScreen;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import betterquesting.api.client.IFileCallback;
import betterquesting.api.client.gui.premade.screens.GuiFileExplorer;
import betterquesting.api.client.jdoc.IJsonDoc;
import betterquesting.api.quests.IQuest;
import betterquesting.api.quests.IQuestLine;
import betterquesting.api.utils.IGuiBuilder;
import betterquesting.client.gui.editors.GuiQuestEditor;
import betterquesting.client.gui.editors.GuiQuestLineEditorA;
import betterquesting.client.gui.editors.GuiQuestLineEditorB;
import betterquesting.client.gui.editors.GuiTextEditor;
import betterquesting.client.gui.editors.json.GuiJsonArray;
import betterquesting.client.gui.editors.json.GuiJsonEntitySelection;
import betterquesting.client.gui.editors.json.GuiJsonFluidSelection;
import betterquesting.client.gui.editors.json.GuiJsonItemSelection;
import betterquesting.client.gui.editors.json.GuiJsonObject;
import betterquesting.client.gui.misc.ITextCallback;

public final class GuiBuilder implements IGuiBuilder
{
	public static final GuiBuilder INSTANCE = new GuiBuilder();
	
	private GuiBuilder()
	{
	}
	
	@Override
	public GuiScreen getJsonEditor(GuiScreen parent, JsonElement json, IJsonDoc jdoc)
	{
		if(json.isJsonArray())
		{
			new GuiJsonArray(parent, json.getAsJsonArray(), jdoc);
		} else if(json.isJsonObject())
		{
			new GuiJsonObject(parent, json.getAsJsonObject(), jdoc);
		}
		
		return null;
	}
	
	@Override
	public GuiScreen getItemEditor(GuiScreen parent, JsonObject json)
	{
		return new GuiJsonItemSelection(parent, json);
	}
	
	@Override
	public GuiScreen getFluidEditor(GuiScreen parent, JsonObject json)
	{
		return new GuiJsonFluidSelection(parent, json);
	}
	
	@Override
	public GuiScreen getEntityEditor(GuiScreen parent, JsonObject json)
	{
		return new GuiJsonEntitySelection(parent, json);
	}
	
	@Override
	public GuiScreen getQuestEditor(GuiScreen parent, IQuest quest)
	{
		return new GuiQuestEditor(parent, quest);
	}
	
	@Override
	public GuiScreen getLineEditor(GuiScreen parent, IQuestLine questLine)
	{
		if(questLine == null)
		{
			return new GuiQuestLineEditorA(parent);
		} else
		{
			return new GuiQuestLineEditorB(parent, questLine);
		}
	}
	
	@Override
	public GuiScreen getTextEditor(GuiScreen parent, String text, ITextCallback editor, int id)
	{
		GuiTextEditor gui = new GuiTextEditor(parent, text);
		
		if(editor != null)
		{
			gui.setHost(editor, id);
		}
		
		return gui;
	}
	
	@Override
	public GuiScreen getFileExplorer(GuiScreen parent, IFileCallback callback, File rootDir, FileFilter filter, boolean multiSelect)
	{
		return new GuiFileExplorer(parent, callback, rootDir, filter).setMultiSelect(multiSelect);
	}
	
}
