package adv_director.client.toolbox;

import adv_director.api.client.gui.misc.IGuiEmbedded;
import adv_director.api.client.gui.misc.IGuiQuestLine;
import adv_director.api.client.toolbox.IToolboxTab;
import adv_director.api.client.toolbox.IToolboxTool;
import adv_director.client.toolbox.tools.ToolboxToolComplete;
import adv_director.client.toolbox.tools.ToolboxToolCopy;
import adv_director.client.toolbox.tools.ToolboxToolDelete;
import adv_director.client.toolbox.tools.ToolboxToolGrab;
import adv_director.client.toolbox.tools.ToolboxToolIcon;
import adv_director.client.toolbox.tools.ToolboxToolLink;
import adv_director.client.toolbox.tools.ToolboxToolNew;
import adv_director.client.toolbox.tools.ToolboxToolOpen;
import adv_director.client.toolbox.tools.ToolboxToolRemove;
import adv_director.client.toolbox.tools.ToolboxToolReset;
import adv_director.client.toolbox.tools.ToolboxToolScale;

public class ToolboxTabMain implements IToolboxTab
{
	public static final ToolboxTabMain instance = new ToolboxTabMain();
	
	private IGuiQuestLine gui;
	
	public IToolboxTool toolOpen;
	public IToolboxTool toolNew;
	public IToolboxTool toolGrab;
	public IToolboxTool toolLink;
	public IToolboxTool toolCopy;
	public IToolboxTool toolRem;
	public IToolboxTool toolDel;
	public IToolboxTool toolCom;
	public IToolboxTool toolRes;
	public IToolboxTool toolIco;
	public IToolboxTool toolSca;
	
	@Override
	public String getUnlocalisedName()
	{
		return "betterquesting.toolbox.tab.main";
	}
	
	@Override
	public void initTools(IGuiQuestLine designer)
	{
		this.gui = designer;
		
		toolOpen = new ToolboxToolOpen();
		toolNew = new ToolboxToolNew();
		toolGrab = new ToolboxToolGrab();
		toolLink = new ToolboxToolLink();
		toolCopy = new ToolboxToolCopy();
		toolRem = new ToolboxToolRemove();
		toolDel = new ToolboxToolDelete();
		toolCom = new ToolboxToolComplete();
		toolRes = new ToolboxToolReset();
		toolIco = new ToolboxToolIcon();
		toolSca = new ToolboxToolScale();
	}
	
	@Override
	public IGuiEmbedded getTabGui(int posX, int posY, int sizeX, int sizeZ)
	{
		if(gui == null)
		{
			return null;
		}
		
		return new ToolboxGuiMain(gui, posX, posY, sizeX, sizeZ);
	}
}
