package betterquesting.client.toolbox.tools;

import betterquesting.client.gui.GuiQuesting;
import betterquesting.client.gui.misc.GuiButtonQuestInstance;
import betterquesting.client.toolbox.ToolboxTool;

public class ToolboxToolIcon extends ToolboxTool
{
	public ToolboxToolIcon(GuiQuesting screen)
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
			screen.mc.displayGuiScreen(new GuiToolIconProxy(screen, btn.quest));
		}
	}
}
