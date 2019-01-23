package betterquesting.api2.client.gui.themes;

import betterquesting.api2.client.gui.resources.colors.IGuiColor;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;
import betterquesting.api2.client.gui.resources.lines.IGuiLine;
import betterquesting.api2.client.gui.resources.textures.IGuiTexture;

public interface IGuiTheme
{
	String getName();
	ResourceLocation getID();
	
	IGuiTexture getTexture(ResourceLocation key);
	IGuiLine getLine(ResourceLocation key);
	IGuiColor getColor(ResourceLocation key);
	
	/** Use this if you wish to start replacing the GUI layouts themselves*/
	GuiScreen getHomeGui(GuiScreen parent);
}
