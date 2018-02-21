package betterquesting.api2.client.gui.themes.builtin;

import java.awt.Color;
import java.util.HashMap;

import betterquesting.api2.client.gui.resources.colors.GuiColorStatic;
import betterquesting.api2.client.gui.resources.colors.IGuiColor;
import net.minecraft.util.ResourceLocation;
import betterquesting.api2.client.gui.misc.GuiPadding;
import betterquesting.api2.client.gui.misc.GuiRectangle;
import betterquesting.api2.client.gui.resources.lines.IGuiLine;
import betterquesting.api2.client.gui.resources.textures.IGuiTexture;
import betterquesting.api2.client.gui.resources.lines.SimpleLine;
import betterquesting.api2.client.gui.resources.textures.SlicedTexture;
import betterquesting.api2.client.gui.resources.textures.SlicedTexture.SliceMode;
import betterquesting.api2.client.gui.themes.IGuiTheme;
import betterquesting.api2.client.gui.themes.presets.PresetTexture;
import betterquesting.core.BetterQuesting;

public class ThemeStandard implements IGuiTheme
{
	private static final ResourceLocation THEME_ID = new ResourceLocation(BetterQuesting.MODID, "standard");
	
	public static final IGuiTheme THEME_STD = new ThemeStandard();
	
	private final HashMap<ResourceLocation, IGuiTexture> TEX_MAP = new HashMap<ResourceLocation, IGuiTexture>();
	private final IGuiLine line = new SimpleLine();
	
	private final IGuiColor textColorMain = new GuiColorStatic(Color.WHITE.getRGB());
	
	public ThemeStandard()
	{
		TEX_MAP.put(PresetTexture.PANEL_MAIN.getKey(), new SlicedTexture(PresetTexture.TX_SIMPLE, new GuiRectangle(24, 0, 12, 12), new GuiPadding(3, 3, 3, 3)).setSliceMode(SliceMode.SLICED_STRETCH));
		TEX_MAP.put(PresetTexture.PANEL_INNER.getKey(), new SlicedTexture(PresetTexture.TX_SIMPLE, new GuiRectangle(48, 0, 12, 12), new GuiPadding(1, 1, 1, 1)).setSliceMode(SliceMode.SLICED_STRETCH));
	}
	
	@Override
	public String getName()
	{
		return "Standard";
	}
	
	@Override
	public ResourceLocation getID()
	{
		return THEME_ID;
	}
	
	@Override
	public IGuiTexture getTexture(ResourceLocation key)
	{
		return TEX_MAP.get(key);
	}
	
	@Override
	public IGuiLine getLine(ResourceLocation key)
	{
		return this.line;
	}
	
	@Override
	public IGuiColor getColor(ResourceLocation key)
	{
		return textColorMain;
	}
}
