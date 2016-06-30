package betterquesting.client.toolbox.tools;

import betterquesting.client.gui.GuiQuestInstance;
import betterquesting.client.gui.GuiQuesting;
import betterquesting.client.gui.misc.GuiButtonQuestInstance;
import betterquesting.client.toolbox.ToolboxTool;

public class ToolboxToolOpen extends ToolboxTool
{
	public ToolboxToolOpen(GuiQuesting screen)
	{
		super(screen);
	}
	
	@Override
	public void onMouseClick(int mx, int my, int click)
	{
		GuiButtonQuestInstance btn = ui.getClickedQuest(mx, my);
		
		if(btn != null && btn.quest != null)
		{
			btn.playPressSound(screen.mc.getSoundHandler());
			screen.mc.displayGuiScreen(new GuiQuestInstance(screen, btn.quest));
		}
	}
}
