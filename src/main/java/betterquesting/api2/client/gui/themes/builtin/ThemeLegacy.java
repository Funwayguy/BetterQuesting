package betterquesting.api2.client.gui.themes.builtin;

import java.awt.Color;
import java.util.HashMap;
import net.minecraft.util.ResourceLocation;
import betterquesting.api2.client.gui.misc.GuiPadding;
import betterquesting.api2.client.gui.misc.GuiRectangle;
import betterquesting.api2.client.gui.resources.IGuiLine;
import betterquesting.api2.client.gui.resources.IGuiTexture;
import betterquesting.api2.client.gui.resources.SlicedTexture;
import betterquesting.api2.client.gui.resources.SlicedTexture.SliceMode;
import betterquesting.api2.client.gui.themes.IGuiTheme;
import betterquesting.api2.client.gui.themes.presets.PresetTexture;
import betterquesting.core.BetterQuesting;

public class ThemeLegacy implements IGuiTheme
{
	public static final ResourceLocation TX_LEGACY = new ResourceLocation(BetterQuesting.MODID, "textures/gui/legacy_gui.png");
	
	public static final IGuiTheme THEME_STD_LIGHT = new ThemeLegacy();
	
	private final ResourceLocation STD_LIGHT = new ResourceLocation(BetterQuesting.MODID, "standard_light");
	
	private final HashMap<ResourceLocation, IGuiTexture> texMap = new HashMap<ResourceLocation, IGuiTexture>();
	
	private final int textColorMain = Color.BLACK.getRGB();
	private final int textColorSub = Color.BLACK.getRGB();
	
	private final IGuiTexture txPanMain = new SlicedTexture(TX_LEGACY, new GuiRectangle(0, 0, 48, 48), new GuiPadding(16, 16, 16, 16)).setSliceMode(SliceMode.SLICED_TILE);
	private final IGuiTexture txPanInner = new SlicedTexture(TX_LEGACY, new GuiRectangle(0, 128, 128, 128), new GuiPadding(1, 1, 1, 1)).setSliceMode(SliceMode.SLICED_STRETCH);
	
	private final IGuiTexture txBtnNrm0 = new SlicedTexture(TX_LEGACY, new GuiRectangle(48, 16, 16, 16), new GuiPadding(2, 2, 2, 3)).setSliceMode(SliceMode.SLICED_TILE);
	private final IGuiTexture txBtnNrm1 = new SlicedTexture(TX_LEGACY, new GuiRectangle(64, 16, 16, 16), new GuiPadding(2, 2, 2, 3)).setSliceMode(SliceMode.SLICED_TILE);
	private final IGuiTexture txBtnNrm2 = new SlicedTexture(TX_LEGACY, new GuiRectangle(80, 16, 16, 16), new GuiPadding(2, 2, 2, 3)).setSliceMode(SliceMode.SLICED_TILE);
	
	private final IGuiTexture txBtnCln0 = new SlicedTexture(TX_LEGACY, new GuiRectangle(96, 16, 16, 16), new GuiPadding(2, 2, 2, 3)).setSliceMode(SliceMode.SLICED_TILE);
	private final IGuiTexture txBtnCln1 = new SlicedTexture(TX_LEGACY, new GuiRectangle(112, 16, 16, 16), new GuiPadding(2, 2, 2, 3)).setSliceMode(SliceMode.SLICED_TILE);
	private final IGuiTexture txBtnCln2 = new SlicedTexture(TX_LEGACY, new GuiRectangle(128, 16, 16, 16), new GuiPadding(2, 2, 2, 3)).setSliceMode(SliceMode.SLICED_TILE);
	
	private final IGuiTexture txItem = new SlicedTexture(TX_LEGACY, new GuiRectangle(48, 32, 16, 16), new GuiPadding(1, 1, 1, 1)).setSliceMode(SliceMode.SLICED_STRETCH);
	
	private final IGuiTexture txScV0 = new SlicedTexture(TX_LEGACY, new GuiRectangle(48, 0, 8, 16), new GuiPadding(1, 1, 1, 1)).setSliceMode(SliceMode.SLICED_TILE);
	private final IGuiTexture txScV1 = new SlicedTexture(TX_LEGACY, new GuiRectangle(56, 0, 8, 16), new GuiPadding(3, 4, 3, 4)).setSliceMode(SliceMode.SLICED_TILE);
	
	private final IGuiTexture txScH0 = new SlicedTexture(TX_LEGACY, new GuiRectangle(64, 0, 16, 8), new GuiPadding(4, 3, 4, 3)).setSliceMode(SliceMode.SLICED_TILE);
	private final IGuiTexture txScH1 = new SlicedTexture(TX_LEGACY, new GuiRectangle(64, 8, 16, 8), new GuiPadding(1, 1, 1, 1)).setSliceMode(SliceMode.SLICED_TILE);
	
	public ThemeLegacy()
	{
		texMap.put(PresetTexture.PANEL_MAIN.getKey(), txPanMain);
		texMap.put(PresetTexture.PANEL_INNER.getKey(), txPanInner);
		
		texMap.put(PresetTexture.BTN_NORMAL_0.getKey(), txBtnNrm0);
		texMap.put(PresetTexture.BTN_NORMAL_1.getKey(), txBtnNrm1);
		texMap.put(PresetTexture.BTN_NORMAL_2.getKey(), txBtnNrm2);
		
		texMap.put(PresetTexture.BTN_CLEAN_0.getKey(), txBtnCln0);
		texMap.put(PresetTexture.BTN_CLEAN_1.getKey(), txBtnCln1);
		texMap.put(PresetTexture.BTN_CLEAN_2.getKey(), txBtnCln2);
		
		texMap.put(PresetTexture.ITEM_FRAME.getKey(), txItem);
		
		texMap.put(PresetTexture.SCROLL_V_0.getKey(), txScV0);
		texMap.put(PresetTexture.SCROLL_V_1.getKey(), txScV1);
		texMap.put(PresetTexture.SCROLL_V_2.getKey(), txScV1);
		
		texMap.put(PresetTexture.SCROLL_H_0.getKey(), txScH0);
		texMap.put(PresetTexture.SCROLL_H_1.getKey(), txScH1);
		texMap.put(PresetTexture.SCROLL_H_2.getKey(), txScH1);
	}
	
	@Override
	public String getName()
	{
		return "Standard Light";
	}

	@Override
	public ResourceLocation getID()
	{
		return STD_LIGHT;
	}

	@Override
	public IGuiTexture getTexture(ResourceLocation key)
	{
		return texMap.get(key);
	}
	
	@Override
	public IGuiLine getLine(ResourceLocation key)
	{
		return null;
	}
	
	@Override
	public Integer getColor(ResourceLocation key)
	{
		return null;//isSubtext? textColorSub : textColorMain;
	}
}
