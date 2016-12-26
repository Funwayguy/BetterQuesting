package betterquesting.api.client.gui.misc;

import net.minecraft.util.ResourceLocation;
import betterquesting.api.client.gui.QuestLineButtonTree;
import betterquesting.api.client.toolbox.IToolboxTool;

public interface IGuiQuestLine extends IGuiEmbedded
{
	public IToolboxTool getActiveTool();
	public void setActiveTool(IToolboxTool tool);
	
	// Can be used to modify button positions without making permanent changes
	public QuestLineButtonTree getQuestLine();
	public void setQuestLine(QuestLineButtonTree line, boolean resetView);
	
	public void setBackground(ResourceLocation image, int size);
	
	public int getRelativeX(int mx);
	public int getRelativeY(int my);
	
	public int getScreenX(int rx);
	public int getScreenY(int ry);
	
	// These are mostly used in copySettings() but can be used for advanced
	// tools which may need more contextual information
	public int getZoom();
	public int getScrollX();
	public int getScrollY();
	
	public int getPosX();
	public int getPosY();
	
	public int getWidth();
	public int getHeight();
	
	public void copySettings(IGuiQuestLine gui);
}
