package betterquesting.api2.client.gui.themes;

import betterquesting.api.client.gui.misc.IGuiHook;
import betterquesting.api2.client.gui.resources.colors.IGuiColor;
import betterquesting.api2.client.gui.resources.lines.IGuiLine;
import betterquesting.api2.client.gui.resources.textures.IGuiTexture;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

public interface IGuiTheme
{
	String getName();
	ResourceLocation getID();
	
	@Nullable
	IGuiTexture getTexture(ResourceLocation key);
	@Nullable
	IGuiLine getLine(ResourceLocation key);
	@Nullable
	IGuiColor getColor(ResourceLocation key);
	
	/** Use this if you wish to start replacing the GUI layouts and not just textures. May change this hook in the near future so don't bet on it staying around*/
	@Nullable
    IGuiHook getGuiHook();
}
