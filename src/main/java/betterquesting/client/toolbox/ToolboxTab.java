package betterquesting.client.toolbox;

import net.minecraft.client.resources.I18n;
import betterquesting.client.gui.editors.GuiQuestLineDesigner;

public abstract class ToolboxTab
{
	private GuiQuestLineDesigner curDes;
	
	public String getDisplayName()
	{
		return I18n.format(getUnlocalisedName());
	}
	
	public abstract String getUnlocalisedName();
	
	public abstract void initTools(GuiQuestLineDesigner designer);
	
	/**
	 * This GUI should contain all buttons/options for your editor tools
	 */
	public abstract ToolboxGui getTabGui(GuiQuestLineDesigner designer, int posX, int posY, int sizeX, int sizeZ);
	
	public final void init_(GuiQuestLineDesigner designer)
	{
		curDes = designer;
		initTools(designer);
	}
	
	public final boolean hasInit(GuiQuestLineDesigner designer)
	{
		return curDes != null && curDes == designer;
	}
}
