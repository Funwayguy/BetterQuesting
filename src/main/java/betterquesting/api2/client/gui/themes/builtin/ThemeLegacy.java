package betterquesting.api2.client.gui.themes.builtin;

import java.util.HashMap;
import net.minecraft.util.ResourceLocation;
import betterquesting.api2.client.gui.misc.GuiPadding;
import betterquesting.api2.client.gui.misc.GuiRectangle;
import betterquesting.api2.client.gui.resources.IGuiTexture;
import betterquesting.api2.client.gui.resources.SlicedTexture;
import betterquesting.api2.client.gui.resources.SlicedTexture.SliceMode;
import betterquesting.api2.client.gui.themes.IGuiTheme;
import betterquesting.api2.client.gui.themes.TexturePreset;
import betterquesting.core.BetterQuesting;

public class ThemeLegacy implements IGuiTheme
{
	public static final ResourceLocation TX_LEGACY = new ResourceLocation(BetterQuesting.MODID, "textures/gui/legacy_gui.png");
	
	public static final IGuiTheme THEME_STD_LIGHT = new ThemeLegacy();
	
	private final ResourceLocation STD_LIGHT = new ResourceLocation(BetterQuesting.MODID, "standard_light");
	
	private final HashMap<ResourceLocation, IGuiTexture> texMap = new HashMap<ResourceLocation, IGuiTexture>();
	
	private final IGuiTexture txPanMain = new SlicedTexture(TX_LEGACY, new GuiRectangle(0, 0, 48, 48), new GuiPadding(16, 16, 16, 16)).setSliceMode(SliceMode.SLICED_TILE);
	private final IGuiTexture txPanInner = new SlicedTexture(TX_LEGACY, new GuiRectangle(0, 128, 128, 128), new GuiPadding(1, 1, 1, 1)).setSliceMode(SliceMode.SLICED_STRETCH);
	
	private final IGuiTexture txBtnNrm0 = new SlicedTexture(TX_LEGACY, new GuiRectangle(48, 16, 16, 16), new GuiPadding(1, 1, 1, 1)).setSliceMode(SliceMode.SLICED_TILE);
	private final IGuiTexture txBtnNrm1 = new SlicedTexture(TX_LEGACY, new GuiRectangle(64, 16, 16, 16), new GuiPadding(1, 1, 1, 1)).setSliceMode(SliceMode.SLICED_TILE);
	private final IGuiTexture txBtnNrm2 = new SlicedTexture(TX_LEGACY, new GuiRectangle(80, 16, 16, 16), new GuiPadding(1, 1, 1, 1)).setSliceMode(SliceMode.SLICED_TILE);
	
	private final IGuiTexture txBtnCln0 = new SlicedTexture(TX_LEGACY, new GuiRectangle(96, 16, 16, 16), new GuiPadding(1, 1, 1, 1)).setSliceMode(SliceMode.SLICED_TILE);
	private final IGuiTexture txBtnCln1 = new SlicedTexture(TX_LEGACY, new GuiRectangle(112, 16, 16, 16), new GuiPadding(1, 1, 1, 1)).setSliceMode(SliceMode.SLICED_TILE);
	private final IGuiTexture txBtnCln2 = new SlicedTexture(TX_LEGACY, new GuiRectangle(128, 16, 16, 16), new GuiPadding(1, 1, 1, 1)).setSliceMode(SliceMode.SLICED_TILE);
	
	private final IGuiTexture txItem = new SlicedTexture(TX_LEGACY, new GuiRectangle(48, 32, 16, 16), new GuiPadding(1, 1, 1, 1)).setSliceMode(SliceMode.SLICED_STRETCH);
	
	private final IGuiTexture txScV0 = new SlicedTexture(TX_LEGACY, new GuiRectangle(48, 0, 8, 16), new GuiPadding(1, 1, 1, 1)).setSliceMode(SliceMode.SLICED_TILE);
	private final IGuiTexture txScV1 = new SlicedTexture(TX_LEGACY, new GuiRectangle(56, 0, 8, 16), new GuiPadding(3, 4, 3, 4)).setSliceMode(SliceMode.SLICED_TILE);
	
	private final IGuiTexture txScH0 = new SlicedTexture(TX_LEGACY, new GuiRectangle(64, 0, 16, 8), new GuiPadding(4, 3, 4, 3)).setSliceMode(SliceMode.SLICED_TILE);
	private final IGuiTexture txScH1 = new SlicedTexture(TX_LEGACY, new GuiRectangle(64, 8, 16, 8), new GuiPadding(1, 1, 1, 1)).setSliceMode(SliceMode.SLICED_TILE);
	
	public ThemeLegacy()
	{
		texMap.put(TexturePreset.PANEL_MAIN, txPanMain);
		texMap.put(TexturePreset.PANEL_INNER, txPanInner);
		
		texMap.put(TexturePreset.BTN_NORMAL_0, txBtnNrm0);
		texMap.put(TexturePreset.BTN_NORMAL_1, txBtnNrm1);
		texMap.put(TexturePreset.BTN_NORMAL_2, txBtnNrm2);
		
		texMap.put(TexturePreset.BTN_CLEAN_0, txBtnCln0);
		texMap.put(TexturePreset.BTN_CLEAN_1, txBtnCln1);
		texMap.put(TexturePreset.BTN_CLEAN_2, txBtnCln2);
		
		texMap.put(TexturePreset.ITEM_FRAME, txItem);
		
		texMap.put(TexturePreset.SCROLL_V_0, txScV0);
		texMap.put(TexturePreset.SCROLL_V_1, txScV1);
		texMap.put(TexturePreset.SCROLL_V_2, txScV1);
		
		texMap.put(TexturePreset.SCROLL_H_0, txScH0);
		texMap.put(TexturePreset.SCROLL_H_1, txScH1);
		texMap.put(TexturePreset.SCROLL_H_2, txScH1);
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
}
