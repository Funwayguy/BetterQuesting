package betterquesting.client.gui.editors.json.scrolling;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import betterquesting.api.client.gui.GuiScreenThemed;
import betterquesting.api.jdoc.IJsonDoc;
import betterquesting.api.misc.ICallback;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class GuiJsonEditor extends GuiScreenThemed
{
	private final JsonElement json;
	private final IJsonDoc jDoc;
	private ICallback<JsonObject> joCallback;
	private ICallback<JsonArray> jaCallback;
	
	public GuiJsonEditor(GuiScreen parent, JsonObject json, IJsonDoc jDoc)
	{
		this(parent, json, jDoc, null);
	}
	
	public GuiJsonEditor(GuiScreen parent, JsonObject json, IJsonDoc jDoc, ICallback<JsonObject> callback)
	{
		super(parent, "betterquesting.title.json_object");
		this.json = json;
		this.jDoc = jDoc;
		
		this.joCallback = callback;
	}
	
	public GuiJsonEditor(GuiScreen parent, JsonArray json, IJsonDoc jDoc)
	{
		this(parent, json, jDoc, null);
	}
	
	public GuiJsonEditor(GuiScreen parent, JsonArray json, IJsonDoc jDoc, ICallback<JsonArray> callback)
	{
		super(parent, "betterquesting.title.json_array");
		this.json = json;
		this.jDoc = jDoc;
		
		this.jaCallback = callback;
	}
	
	public void initGui()
	{
		super.initGui();
		
		if(jDoc != null)
		{
			String ulTitle = jDoc.getUnlocalisedTitle();
			String lTitle = I18n.format(ulTitle);
			
			if(!ulTitle.equals(lTitle))
			{
				this.setTitle(I18n.format(jDoc.getUnlocalisedTitle()));
			}
		}
		
		GuiScrollingJson gsj = new GuiScrollingJson(mc, guiLeft + 16, guiTop + 32, sizeX - 32, sizeY - 64);
		
		if(json.isJsonObject())
		{
			gsj.setJson(json.getAsJsonObject(), jDoc);
		} else
		{
			gsj.setJson(json.getAsJsonArray(), jDoc);
		}
		gsj.refresh();
		this.embedded.add(gsj);
	}
	
	public void actionPerformed(GuiButton btn)
	{
		super.actionPerformed(btn);
		
		if(btn.id == 0)
		{
			if(json.isJsonObject() && joCallback != null)
			{
				joCallback.setValue(json.getAsJsonObject());
			} else if(json.isJsonArray() && jaCallback != null)
			{
				jaCallback.setValue(json.getAsJsonArray());
			}
		}
	}
}
