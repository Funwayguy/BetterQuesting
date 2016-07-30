package betterquesting.client.toolbox.tools;

import betterquesting.client.gui.editors.GuiQuestLineDesigner;
import betterquesting.client.toolbox.ToolboxGui;
import betterquesting.client.toolbox.ToolboxTab;

public class ToolboxTabMain extends ToolboxTab
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
	public void initTools(GuiQuestLineDesigner designer)
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
	public ToolboxGui getTabGui(GuiQuestLineDesigner designer, int posX, int posY, int sizeX, int sizeZ)
	{
		return new ToolboxGuiMain(designer, posX, posY, sizeX, sizeZ);
	}
}
