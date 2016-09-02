package betterquesting.api.utils;

import betterquesting.api.client.IFileCallback;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.client.gui.GuiScreen;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public interface IGuiBuilder
{
	public GuiScreen openTextEditor(GuiScreen parent, IJsonCallback<JsonPrimitive> json);
	public GuiScreen openJsonEditor(GuiScreen parent, IJsonCallback<JsonElement> json, String docPrefix);
	public GuiScreen openItemEditor(GuiScreen parent, IJsonCallback<JsonObject> json);
	public GuiScreen openFluidEditor(GuiScreen parent, IJsonCallback<JsonObject> json);
	public GuiScreen openEntityEditor(GuiScreen parent, IJsonCallback<JsonObject> json);
	
	public GuiScreen openQuestEditor(GuiScreen parent, int questId);
	public GuiScreen openLineEditor(GuiScreen parnet, int questLineId);
	
	public GuiScreen openFileExplorer(GuiScreen parent, IFileCallback callback, boolean singleFile);
}
