package betterquesting.api2.client.gui.themes.builtin;

import java.awt.Color;
import java.util.HashMap;
import net.minecraft.util.ResourceLocation;
import betterquesting.api2.client.gui.misc.GuiPadding;
import betterquesting.api2.client.gui.misc.GuiRectangle;
import betterquesting.api2.client.gui.resources.IGuiLine;
import betterquesting.api2.client.gui.resources.IGuiTexture;
import betterquesting.api2.client.gui.resources.SimpleLine;
import betterquesting.api2.client.gui.resources.SlicedTexture;
import betterquesting.api2.client.gui.resources.SlicedTexture.SliceMode;
import betterquesting.api2.client.gui.themes.IGuiTheme;
import betterquesting.api2.client.gui.themes.presets.PresetTexture;
import betterquesting.core.BetterQuesting;

public class ThemeStandard implements IGuiTheme
{
	private static final ResourceLocation THEME_ID = new ResourceLocation(BetterQuesting.MODID, "standard");
	
	public static final IGuiTheme THEME_STD = new ThemeStandard();
	
	private final HashMap<ResourceLocation, IGuiTexture> TEX_MAP = new HashMap<ResourceLocation, IGuiTexture>();
	private final IGuiLine line = new SimpleLine();
	
	private final int textColorMain = Color.WHITE.getRGB();
	//private final int textColorSub = Color.WHITE.getRGB();
	
	private final IGuiTexture txPanMain = new SlicedTexture(PresetTexture.TX_SIMPLE, new GuiRectangle(24, 0, 12, 12), new GuiPadding(3, 3, 3, 3)).setSliceMode(SliceMode.SLICED_STRETCH);
	private final IGuiTexture txPanInner = new SlicedTexture(PresetTexture.TX_SIMPLE, new GuiRectangle(48, 0, 12, 12), new GuiPadding(1, 1, 1, 1)).setSliceMode(SliceMode.SLICED_STRETCH);
	
	public ThemeStandard()
	{
		TEX_MAP.put(PresetTexture.PANEL_MAIN.getKey(), txPanMain);
		TEX_MAP.put(PresetTexture.PANEL_INNER.getKey(), txPanInner);
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
	public Integer getColor(ResourceLocation key)
	{
		return textColorMain;
	}
}
