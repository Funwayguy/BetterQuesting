package betterquesting.client.toolbox.tools;

import net.minecraft.client.Minecraft;
import betterquesting.api.client.gui.controls.GuiButtonQuestInstance;
import betterquesting.api.client.gui.quest.IGuiQuestLine;
import betterquesting.api.client.toolbox.IToolboxTool;
import betterquesting.client.gui.GuiQuestInstance;

public class ToolboxToolOpen implements IToolboxTool
{
	private IGuiQuestLine gui;
	
	public void initTool(IGuiQuestLine gui)
	{
		this.gui = gui;
	}

	@Override
	public void disableTool()
	{
	}
	
	@Override
	public void onMouseClick(int mx, int my, int click)
	{
		if(click != 0)
		{
			return;
		}
		
		GuiButtonQuestInstance btn = gui.getQuestLine().getButtonAt(mx, my);
		
		if(btn != null)
		{
			Minecraft mc = Minecraft.getMinecraft();
			btn.func_146113_a(mc.getSoundHandler());
			mc.displayGuiScreen(new GuiQuestInstance(mc.currentScreen, btn.getQuest()));
		}
	}

	@Override
	public void drawTool(int mx, int my, float partialTick)
	{
	}

	@Override
	public void onMouseScroll(int mx, int my, int scroll)
	{
	}

	@Override
	public void onKeyPressed(char c, int key)
	{
	}

	@Override
	public boolean allowTooltips()
	{
		return true;
	}

	@Override
	public boolean allowScrolling(int click)
	{
		return true;
	}

	@Override
	public boolean allowZoom()
	{
		return true;
	}

	@Override
	public boolean clampScrolling()
	{
		return true;
	}
}
