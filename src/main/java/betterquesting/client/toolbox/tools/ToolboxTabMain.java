package betterquesting.client.toolbox.tools;

import betterquesting.api.client.gui.IGuiEmbedded;
import betterquesting.api.client.gui.quest.IGuiQuestLine;
import betterquesting.api.client.toolbox.IToolboxTab;

public class ToolboxTabMain implements IToolboxTab
{
	public static final ToolboxTabMain instance = new ToolboxTabMain();
	
	public ToolboxToolOpen toolOpen;
	public ToolboxToolNew toolNew;
	public ToolboxToolGrab toolGrab;
	public ToolboxToolLink toolLink;
	public ToolboxToolCopy toolCopy;
	public ToolboxToolRemove toolRem;
	public ToolboxToolDelete toolDel;
	public ToolboxToolComplete toolCom;
	public ToolboxToolReset toolRes;
	public ToolboxToolIcon toolIco;
	
	@Override
	public String getUnlocalisedName()
	{
		return "betterquesting.toolbox.tab.main";
	}
	
	@Override
	public void initTools(IGuiQuestLine designer)
	{
		toolOpen = new ToolboxToolOpen(designer);
		toolNew = new ToolboxToolNew(designer);
		toolGrab = new ToolboxToolGrab(designer);
		toolLink = new ToolboxToolLink(designer);
		toolCopy = new ToolboxToolCopy(designer);
		toolRem = new ToolboxToolRemove(designer);
		toolDel = new ToolboxToolDelete(designer);
		toolCom = new ToolboxToolComplete(designer);
		toolRes = new ToolboxToolReset(designer);
		toolIco = new ToolboxToolIcon(designer);
	}
	
	@Override
	public IGuiEmbedded getTabGui(int posX, int posY, int sizeX, int sizeZ)
	{
		return new ToolboxGuiMain(posX, posY, sizeX, sizeZ);
	}
}
