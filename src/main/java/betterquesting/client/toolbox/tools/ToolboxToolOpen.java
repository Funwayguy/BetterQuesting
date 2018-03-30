package betterquesting.client.toolbox.tools;

import betterquesting.client.gui2.GuiQuest;
import betterquesting.questing.QuestDatabase;
import net.minecraft.client.Minecraft;
import betterquesting.api.client.gui.controls.GuiButtonQuestInstance;
import betterquesting.api.client.gui.misc.IGuiQuestLine;
import betterquesting.api.client.toolbox.IToolboxTool;

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
			int qID = QuestDatabase.INSTANCE.getKey(btn.getQuest());
			
			Minecraft mc = Minecraft.getMinecraft();
			btn.playPressSound(mc.getSoundHandler());
			mc.displayGuiScreen(new GuiQuest(mc.currentScreen, qID));
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
