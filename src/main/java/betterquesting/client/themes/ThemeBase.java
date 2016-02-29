package betterquesting.client.themes;

import java.awt.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;

public abstract class ThemeBase
{
	public abstract String GetName();
	public abstract ResourceLocation guiTexture();
	
	/**
	 * Main text color used within questing UIs
	 */
	public Color textColor()
	{
		return Color.BLACK;
	}
	
	/**
	 * The method that is actually called for line color. 
	 */
	public final Color getLineColor(int questState, boolean isMain)
	{
		Color c = lineColor(questState, isMain);
		
		if(questState == 1 && (Minecraft.getSystemTime()/1000)%2 == 0)
		{
			return new Color(c.getRed()/255F*0.5F, c.getGreen()/255F*0.5F, c.getBlue()/255F*0.5F);
		} else
		{
			return c;
		}
	}
	
	/**
	 * The color of lines between quests<br>
	 * (0 = locked, 1 = incomplete, 2 = complete)<br>
	 * <b>Note:</b> Incomplete color will be darkened by 50% while blinking
	 */
	protected Color lineColor(int questState, boolean isMain)
	{
		switch(questState)
		{
			case 0: // Locked
				return new Color(0.75F, 0F, 0F);
			case 1: // Incomplete
				return Color.YELLOW;
			case 2: // Complete
				return Color.GREEN;
			default: // Error
				return Color.BLACK;
		}
	}
	
	/**
	 * The method that is actually called for icon color. 
	 */
	public final Color getIconColor(int hoverState, int questState, boolean isMain)
	{
		Color c = iconColor(questState, isMain);
		
		if(hoverState == 2)
		{
			return c;
		} else
		{
			return new Color(c.getRed()/255F*0.75F, c.getGreen()/255F*0.75F, c.getBlue()/255F*0.75F);
		}
	}
	
	/**
	 * Returns the quest icon color<br>
	 * (0 = locked, 1 incomplete, 2 pending rewards, 3 completed)<br>
	 * <b>Note:</b> Color will be darkened by 25% when not hovering over it
	 */
	protected Color iconColor(int questState, boolean isMain)
	{
		switch(questState)
		{
			case 0: // Locked
				return Color.GRAY;
			case 1: // Incomplete
				return new Color(0.75F, 0F, 0F);
			case 2: // Pending Rewards
				return new Color(0F, 1F, 1F);
			case 3: // Complete
				return Color.GREEN;
			default: // Error
				return Color.BLACK;
		}
	}
	
	/**
	 * Quick method for theme specific GUI replacements
	 */
	public GuiScreen getGui(GuiScreen gui)
	{
		return gui;
	}
}
