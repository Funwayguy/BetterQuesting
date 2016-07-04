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
		if(click != 0)
		{
			return;
		}
		
		GuiButtonQuestInstance btn = ui.getClickedQuest(mx, my);
		
		if(btn != null && btn.quest != null)
		{
			btn.func_146113_a(screen.mc.getSoundHandler());
			screen.mc.displayGuiScreen(new GuiQuestInstance(screen, btn.quest));
		}
	}
}
