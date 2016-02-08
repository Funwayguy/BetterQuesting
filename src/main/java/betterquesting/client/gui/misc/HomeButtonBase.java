package betterquesting.client.gui.misc;

import betterquesting.client.gui.GuiQuesting;

public abstract class HomeButtonBase
{
	public void openGui(GuiQuesting parent){}
	
	public void drawButton(int x, int y, float partialTick, int state)
	{
		
	}
	
	public boolean isEditOnly()
	{
		return false;
	}
}
