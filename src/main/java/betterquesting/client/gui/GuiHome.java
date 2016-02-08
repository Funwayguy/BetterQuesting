package betterquesting.client.gui;

import net.minecraft.client.gui.GuiScreen;

public class GuiHome extends GuiQuesting
{
	public GuiHome(GuiScreen parent)
	{
		super(parent, "betterquesting.title.home");
	}
	
	@Override
	public void drawScreen(int mx, int my, float partialTick)
	{
		super.drawScreen(mx, my, partialTick);
	}
	
	public static void registerButton()
	{
		
	}
}
