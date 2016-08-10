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
	public GuiScreen openTextEditor(GuiScreen parent, IJsonStorage<JsonPrimitive> json);
	public GuiScreen openJsonEditor(GuiScreen parent, IJsonStorage<? extends JsonElement> json, String docPrefix);
	public GuiScreen openItemEditor(GuiScreen parent, IJsonStorage<? extends JsonObject> json);
	public GuiScreen openFluidEditor(GuiScreen parent, IJsonStorage<? extends JsonObject> json);
	public GuiScreen openEntityEditor(GuiScreen parent, IJsonStorage<? extends JsonObject> json);
	
	public GuiScreen openQuestEditor(GuiScreen parent, int questId);
	public GuiScreen openLineEditor(GuiScreen parnet, int questLineId);
	
	public GuiScreen openFileExplorer(GuiScreen parent, IFileCallback callback, boolean singleFile);
}
