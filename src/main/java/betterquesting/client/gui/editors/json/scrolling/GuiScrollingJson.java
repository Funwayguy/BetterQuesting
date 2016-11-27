package betterquesting.client.gui.editors.json.scrolling;

import java.util.Map.Entry;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import betterquesting.api.client.gui.lists.GuiScrollingBase;
import betterquesting.api.jdoc.IJsonDoc;

public class GuiScrollingJson extends GuiScrollingBase<ScrollingJsonEntry>
{
	private JsonElement json;
	private boolean allowEdit = true;
	private IJsonDoc jDoc;
	private int posX;
	
	public GuiScrollingJson(Minecraft mc, int x, int y, int w, int h)
	{
		super(mc, x, y, w, h);
		this.posX = x;
	}
	
	public GuiScrollingJson setEditMode(boolean allowEdit)
	{
		this.allowEdit = allowEdit;
		return this;
	}
	
	public GuiScrollingJson setJson(JsonObject json, IJsonDoc jDoc)
	{
		this.jDoc = jDoc;
		setJsonInternal(json);
		return this;
	}
	
	public GuiScrollingJson setJson(JsonArray json, IJsonDoc jDoc)
	{
		this.jDoc = jDoc;
		setJsonInternal(json);
		return this;
	}
	
	private void setJsonInternal(JsonElement json)
	{
		this.json = json;
		refresh();
	}
	
	@Override
	public void onKeyTyped(char c, int keyCode)
	{
		super.onKeyTyped(c, keyCode);
		
		for(ScrollingJsonEntry sje : this.getEntryList())
		{
			sje.onKeyTyped(c, keyCode);
		}
	}
	
	public void refresh()
	{
		if(json == null)
		{
			return;
		}
		
		int width = this.getListWidth();
		
		this.getEntryList().clear();
		if(json.isJsonArray())
		{
			JsonArray ja = json.getAsJsonArray();
			
			for(int i = 0; i < ja.size(); i++)
			{
				ScrollingJsonEntry sje = new ScrollingJsonEntry(this, ja, i, jDoc, allowEdit);
				sje.setupEntry(posX, width);
				this.getEntryList().add(sje);
			}
		} else if(json.isJsonObject())
		{
			JsonObject jo = json.getAsJsonObject();
			
			for(Entry<String,JsonElement> entry : jo.entrySet())
			{
				ScrollingJsonEntry sje = new ScrollingJsonEntry(this, jo, entry.getKey(), jDoc, allowEdit);
				sje.setupEntry(posX, width);
				this.getEntryList().add(sje);
			}
		}
	}
}
