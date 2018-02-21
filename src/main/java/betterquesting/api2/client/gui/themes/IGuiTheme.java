package betterquesting.api2.client.gui.themes;

import betterquesting.api2.client.gui.resources.colors.IGuiColor;
import net.minecraft.util.ResourceLocation;
import betterquesting.api2.client.gui.resources.lines.IGuiLine;
import betterquesting.api2.client.gui.resources.textures.IGuiTexture;

public interface IGuiTheme
{
	public String getName();
	public ResourceLocation getID();
	public IGuiTexture getTexture(ResourceLocation key);
	public IGuiLine getLine(ResourceLocation key);
	public IGuiColor getColor(ResourceLocation key);
}
