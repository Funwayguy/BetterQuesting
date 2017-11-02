package betterquesting.client.gui.editors.json.scrolling;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import betterquesting.api.client.gui.lists.GuiScrollingBase;
import betterquesting.api.jdoc.IJsonDoc;

public class GuiScrollingJson extends GuiScrollingBase<ScrollingJsonEntry>
{
	private NBTBase json;
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
	
	public GuiScrollingJson setJson(NBTTagCompound json, IJsonDoc jDoc)
	{
		this.jDoc = jDoc;
		setJsonInternal(json);
		return this;
	}
	
	public GuiScrollingJson setJson(NBTTagList json, IJsonDoc jDoc)
	{
		this.jDoc = jDoc;
		setJsonInternal(json);
		return this;
	}
	
	private void setJsonInternal(NBTBase json)
	{
		this.json = json;
		refresh();
	}
	
	@Override
	public void onKeyTyped(char c, int keyCode)
	{
		super.onKeyTyped(c, keyCode);
		
		for(int i = this.getEntryList().size() - 1; i >= 0; i--)
		{
			ScrollingJsonEntry sje = this.getEntryList().get(i);
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
		if(json.getId() == 9)
		{
			NBTTagList ja = (NBTTagList)json;
			
			for(int i = 0; i < ja.tagCount(); i++)
			{
				ScrollingJsonEntry sje = new ScrollingJsonEntry(this, ja, i, jDoc, allowEdit);
				sje.setupEntry(posX, width);
				this.getEntryList().add(sje);
			}
			
			ScrollingJsonEntry sje = new ScrollingJsonEntry(this, ja, ja.tagCount(), null, allowEdit);
			sje.setupEntry(posX, width);
			this.getEntryList().add(sje);
		} else if(json.getId() == 10)
		{
			NBTTagCompound jo = (NBTTagCompound)json;
			
			for(String entry : jo.getKeySet())
			{
				ScrollingJsonEntry sje = new ScrollingJsonEntry(this, jo, entry, jDoc, allowEdit);
				sje.setupEntry(posX, width);
				this.getEntryList().add(sje);
			}
			
			ScrollingJsonEntry sje = new ScrollingJsonEntry(this, jo, "", null, allowEdit);
			sje.setupEntry(posX, width);
			this.getEntryList().add(sje);
		}
	}
}
