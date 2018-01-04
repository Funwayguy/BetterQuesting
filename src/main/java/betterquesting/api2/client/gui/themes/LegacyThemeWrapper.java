package betterquesting.api2.client.gui.themes;

import java.util.HashMap;
import net.minecraft.util.ResourceLocation;
import betterquesting.api.client.themes.ITheme;
import betterquesting.api2.client.gui.misc.GuiPadding;
import betterquesting.api2.client.gui.misc.GuiRectangle;
import betterquesting.api2.client.gui.resources.IGuiLine;
import betterquesting.api2.client.gui.resources.IGuiTexture;
import betterquesting.api2.client.gui.resources.SimpleLine;
import betterquesting.api2.client.gui.resources.SlicedTexture;
import betterquesting.api2.client.gui.resources.SlicedTexture.SliceMode;
import betterquesting.api2.client.gui.themes.presets.PresetColor;
import betterquesting.api2.client.gui.themes.presets.PresetTexture;

public class LegacyThemeWrapper implements IGuiTheme
{
	private final ITheme oldTheme;
	private final IGuiLine line = new SimpleLine();
	
	private final HashMap<ResourceLocation, IGuiTexture> TEX_MAP = new HashMap<ResourceLocation, IGuiTexture>();
	
	public LegacyThemeWrapper(ITheme oldTheme)
	{
		this.oldTheme = oldTheme;
		
		TEX_MAP.put(PresetTexture.PANEL_MAIN.getKey(), new SlicedTexture(oldTheme.getGuiTexture(), new GuiRectangle(0, 0, 48, 48), new GuiPadding(16, 16, 16, 16)).setSliceMode(SliceMode.SLICED_TILE));
		
		TEX_MAP.put(PresetTexture.BTN_NORMAL_0.getKey(), new SlicedTexture(oldTheme.getGuiTexture(), new GuiRectangle(48, 0, 200, 20), new GuiPadding(2, 2, 2, 3)).setSliceMode(SliceMode.SLICED_TILE));
		TEX_MAP.put(PresetTexture.BTN_NORMAL_1.getKey(), new SlicedTexture(oldTheme.getGuiTexture(), new GuiRectangle(48, 20, 200, 20), new GuiPadding(2, 2, 2, 3)).setSliceMode(SliceMode.SLICED_TILE));
		TEX_MAP.put(PresetTexture.BTN_NORMAL_2.getKey(), new SlicedTexture(oldTheme.getGuiTexture(), new GuiRectangle(48, 40, 200, 20), new GuiPadding(2, 2, 2, 3)).setSliceMode(SliceMode.SLICED_TILE));

		TEX_MAP.put(PresetTexture.BTN_CLEAN_0.getKey(), new SlicedTexture(oldTheme.getGuiTexture(), new GuiRectangle(48, 0, 200, 20), new GuiPadding(2, 2, 2, 3)).setSliceMode(SliceMode.SLICED_TILE));
		TEX_MAP.put(PresetTexture.BTN_CLEAN_1.getKey(), new SlicedTexture(oldTheme.getGuiTexture(), new GuiRectangle(48, 20, 200, 20), new GuiPadding(2, 2, 2, 3)).setSliceMode(SliceMode.SLICED_TILE));
		TEX_MAP.put(PresetTexture.BTN_CLEAN_2.getKey(), new SlicedTexture(oldTheme.getGuiTexture(), new GuiRectangle(48, 40, 200, 20), new GuiPadding(2, 2, 2, 3)).setSliceMode(SliceMode.SLICED_TILE));

		TEX_MAP.put(PresetTexture.SCROLL_V_0.getKey(), new SlicedTexture(oldTheme.getGuiTexture(), new GuiRectangle(248, 0, 8, 60), new GuiPadding(1, 1, 1, 1)).setSliceMode(SliceMode.SLICED_STRETCH));
		TEX_MAP.put(PresetTexture.SCROLL_V_1.getKey(), new SlicedTexture(oldTheme.getGuiTexture(), new GuiRectangle(248, 60, 8, 20), new GuiPadding(1, 1, 1, 1)).setSliceMode(SliceMode.SLICED_STRETCH));
		TEX_MAP.put(PresetTexture.SCROLL_V_2.getKey(), new SlicedTexture(oldTheme.getGuiTexture(), new GuiRectangle(248, 60, 8, 20), new GuiPadding(1, 1, 1, 1)).setSliceMode(SliceMode.SLICED_STRETCH));
		
		TEX_MAP.put(PresetTexture.ITEM_FRAME.getKey(), new SlicedTexture(oldTheme.getGuiTexture(), new GuiRectangle(0, 48, 18, 18), new GuiPadding(1, 1, 1, 1)).setSliceMode(SliceMode.SLICED_STRETCH));
		TEX_MAP.put(PresetTexture.AUX_FRAME_0.getKey(), new SlicedTexture(oldTheme.getGuiTexture(), new GuiRectangle(0, 128, 128, 128), new GuiPadding(1, 1, 1, 1)).setSliceMode(SliceMode.SLICED_STRETCH));
		
		// SLICE THEME TO PIECES!
	}
	
	@Override
	public String getName()
	{
		return oldTheme.getDisplayName();
	}
	
	@Override
	public ResourceLocation getID()
	{
		return oldTheme.getThemeID();
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
		if(key == null || key.equals(PresetColor.TEXT_MAIN.getKey()) || key.equals(PresetColor.TEXT_HEADER.getKey()))
		{
			System.out.println("This is fine...");
			return oldTheme.getTextColor();
		}
		
		return null;
	}
}
