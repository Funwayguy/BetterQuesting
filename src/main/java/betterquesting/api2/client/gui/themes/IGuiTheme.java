package betterquesting.api2.client.gui.themes;

import net.minecraft.util.ResourceLocation;
import betterquesting.api2.client.gui.resources.IGuiLine;
import betterquesting.api2.client.gui.resources.IGuiTexture;

public interface IGuiTheme
{
	public String getName();
	public ResourceLocation getID();
	public IGuiTexture getTexture(ResourceLocation key);
	public IGuiLine getLine(ResourceLocation key);
	public Integer getColor(ResourceLocation key);
}
