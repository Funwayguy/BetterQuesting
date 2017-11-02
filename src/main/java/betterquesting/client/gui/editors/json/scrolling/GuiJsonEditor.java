package betterquesting.client.gui.editors.json.scrolling;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import betterquesting.api.client.gui.GuiScreenThemed;
import betterquesting.api.jdoc.IJsonDoc;
import betterquesting.api.misc.ICallback;

public class GuiJsonEditor extends GuiScreenThemed
{
	private final NBTBase json;
	private final IJsonDoc jDoc;
	private ICallback<NBTTagCompound> joCallback;
	private ICallback<NBTTagList> jaCallback;
	
	public GuiJsonEditor(GuiScreen parent, NBTTagCompound json, IJsonDoc jDoc)
	{
		this(parent, json, jDoc, null);
	}
	
	public GuiJsonEditor(GuiScreen parent, NBTTagCompound json, IJsonDoc jDoc, ICallback<NBTTagCompound> callback)
	{
		super(parent, "betterquesting.title.json_object");
		this.json = json;
		this.jDoc = jDoc;
		
		this.joCallback = callback;
	}
	
	public GuiJsonEditor(GuiScreen parent, NBTTagList json, IJsonDoc jDoc)
	{
		this(parent, json, jDoc, null);
	}
	
	public GuiJsonEditor(GuiScreen parent, NBTTagList json, IJsonDoc jDoc, ICallback<NBTTagList> callback)
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
		
		if(json.getId() == 10)
		{
			gsj.setJson((NBTTagCompound)json, jDoc);
		} else if(json.getId() == 9)
		{
			gsj.setJson((NBTTagList)json, jDoc);
		}
		gsj.refresh();
		this.embedded.add(gsj);
	}
	
	public void actionPerformed(GuiButton btn)
	{
		super.actionPerformed(btn);
		
		if(btn.id == 0)
		{
			if(json.getId() == 10 && joCallback != null)
			{
				joCallback.setValue((NBTTagCompound)json);
			} else if(json.getId() == 9 && jaCallback != null)
			{
				jaCallback.setValue((NBTTagList)json);
			}
		}
	}
}
