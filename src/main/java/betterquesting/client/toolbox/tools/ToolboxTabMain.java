package betterquesting.client.toolbox.tools;

import betterquesting.api.client.gui.IGuiEmbedded;
import betterquesting.api.client.gui.quest.IGuiQuestLine;
import betterquesting.api.client.toolbox.IToolboxTab;
import betterquesting.api.client.toolbox.IToolboxTool;

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
