package betterquesting.api2.client.gui.themes;

import net.minecraft.util.ResourceLocation;
import betterquesting.api2.client.gui.misc.GuiPadding;
import betterquesting.api2.client.gui.misc.GuiRectangle;
import betterquesting.api2.client.gui.resources.SlicedTexture;
import betterquesting.api2.client.gui.themes.builtin.ThemeLegacy;
import betterquesting.core.BetterQuesting;

public class TexturePreset
{
	public static final ResourceLocation TX_SIMPLE = new ResourceLocation(BetterQuesting.MODID, "textures/gui/simple_frames.png");
	
	public static final ResourceLocation PANEL_MAIN = new ResourceLocation(BetterQuesting.MODID, "panel_main");
	public static final ResourceLocation PANEL_DARK = new ResourceLocation(BetterQuesting.MODID, "panel_dark");
	public static final ResourceLocation PANEL_INNER = new ResourceLocation(BetterQuesting.MODID, "panel_inner");
	
	public static final ResourceLocation ITEM_FRAME = new ResourceLocation(BetterQuesting.MODID, "item_frame");
	public static final ResourceLocation AUX_FRAME_0 = new ResourceLocation(BetterQuesting.MODID, "aux_frame_0");
	public static final ResourceLocation AUX_FRAME_1 = new ResourceLocation(BetterQuesting.MODID, "aux_frame_1");
	
	public static final ResourceLocation BTN_NORMAL_0 = new ResourceLocation(BetterQuesting.MODID, "btn_normal_0");
	public static final ResourceLocation BTN_NORMAL_1 = new ResourceLocation(BetterQuesting.MODID, "btn_normal_1");
	public static final ResourceLocation BTN_NORMAL_2 = new ResourceLocation(BetterQuesting.MODID, "btn_normal_2");
	
	public static final ResourceLocation BTN_CLEAN_0 = new ResourceLocation(BetterQuesting.MODID, "btn_clean_0");
	public static final ResourceLocation BTN_CLEAN_1 = new ResourceLocation(BetterQuesting.MODID, "btn_clean_1");
	public static final ResourceLocation BTN_CLEAN_2 = new ResourceLocation(BetterQuesting.MODID, "btn_clean_2");
	
	public static final ResourceLocation BTN_ALT_0 = new ResourceLocation(BetterQuesting.MODID, "btn_alt_0");
	public static final ResourceLocation BTN_ALT_1 = new ResourceLocation(BetterQuesting.MODID, "btn_alt_1");
	public static final ResourceLocation BTN_ALT_2 = new ResourceLocation(BetterQuesting.MODID, "btn_alt_2");
	
	public static final ResourceLocation HOTBAR_0 = new ResourceLocation(BetterQuesting.MODID, "hotbar_0");
	public static final ResourceLocation HOTBAR_1 = new ResourceLocation(BetterQuesting.MODID, "hotbar_1");
	
	public static final ResourceLocation SCROLL_V_0 = new ResourceLocation(BetterQuesting.MODID, "scroll_v_0");
	public static final ResourceLocation SCROLL_V_1 = new ResourceLocation(BetterQuesting.MODID, "scroll_v_1");
	public static final ResourceLocation SCROLL_V_2 = new ResourceLocation(BetterQuesting.MODID, "scroll_v_2");
	
	public static final ResourceLocation SCROLL_H_0 = new ResourceLocation(BetterQuesting.MODID, "scroll_h_0");
	public static final ResourceLocation SCROLL_H_1 = new ResourceLocation(BetterQuesting.MODID, "scroll_h_1");
	public static final ResourceLocation SCROLL_H_2 = new ResourceLocation(BetterQuesting.MODID, "scroll_h_2");
	
	public static final ResourceLocation METER_V_0 = new ResourceLocation(BetterQuesting.MODID, "meter_v_0");
	public static final ResourceLocation METER_V_1 = new ResourceLocation(BetterQuesting.MODID, "meter_v_1");
	
	public static final ResourceLocation METER_H_0 = new ResourceLocation(BetterQuesting.MODID, "meter_h_0");
	public static final ResourceLocation METER_H_1 = new ResourceLocation(BetterQuesting.MODID, "meter_h_1");
	
	protected static void initPresets(ThemeRegistry reg)
	{
		reg.setDefaultTexture(PANEL_MAIN, new SlicedTexture(TX_SIMPLE, new GuiRectangle(0, 0, 12, 12), new GuiPadding(4, 4, 4, 4)));
		reg.setDefaultTexture(PANEL_DARK, new SlicedTexture(TX_SIMPLE, new GuiRectangle(12, 0, 12, 12), new GuiPadding(4, 4, 4, 4)));
		reg.setDefaultTexture(PANEL_INNER, new SlicedTexture(TX_SIMPLE, new GuiRectangle(24, 0, 12, 12), new GuiPadding(4, 4, 4, 4)));
		
		reg.setDefaultTexture(ITEM_FRAME, new SlicedTexture(TX_SIMPLE, new GuiRectangle(36, 0, 12, 12), new GuiPadding(4, 4, 4, 4)));
		reg.setDefaultTexture(AUX_FRAME_0, new SlicedTexture(TX_SIMPLE, new GuiRectangle(48, 0, 12, 12), new GuiPadding(4, 4, 4, 4)));
		reg.setDefaultTexture(AUX_FRAME_1, new SlicedTexture(TX_SIMPLE, new GuiRectangle(60, 0, 12, 12), new GuiPadding(4, 4, 4, 4)));
		
		reg.setDefaultTexture(BTN_NORMAL_0, new SlicedTexture(TX_SIMPLE, new GuiRectangle(72, 0, 12, 12), new GuiPadding(4, 4, 4, 4)));
		reg.setDefaultTexture(BTN_NORMAL_1, new SlicedTexture(TX_SIMPLE, new GuiRectangle(84, 0, 12, 12), new GuiPadding(4, 4, 4, 4)));
		reg.setDefaultTexture(BTN_NORMAL_2, new SlicedTexture(TX_SIMPLE, new GuiRectangle(96, 0, 12, 12), new GuiPadding(4, 4, 4, 4)));
		
		reg.setDefaultTexture(BTN_CLEAN_0, new SlicedTexture(TX_SIMPLE, new GuiRectangle(108, 0, 12, 12), new GuiPadding(4, 4, 4, 4)));
		reg.setDefaultTexture(BTN_CLEAN_1, new SlicedTexture(TX_SIMPLE, new GuiRectangle(120, 0, 12, 12), new GuiPadding(4, 4, 4, 4)));
		reg.setDefaultTexture(BTN_CLEAN_2, new SlicedTexture(TX_SIMPLE, new GuiRectangle(132, 0, 12, 12), new GuiPadding(4, 4, 4, 4)));
		
		reg.setDefaultTexture(BTN_ALT_0, new SlicedTexture(TX_SIMPLE, new GuiRectangle(144, 0, 12, 12), new GuiPadding(4, 4, 4, 4)));
		reg.setDefaultTexture(BTN_ALT_1, new SlicedTexture(TX_SIMPLE, new GuiRectangle(156, 0, 12, 12), new GuiPadding(4, 4, 4, 4)));
		reg.setDefaultTexture(BTN_ALT_2, new SlicedTexture(TX_SIMPLE, new GuiRectangle(178, 0, 12, 12), new GuiPadding(4, 4, 4, 4)));
		
		reg.setDefaultTexture(HOTBAR_0, new SlicedTexture(TX_SIMPLE, new GuiRectangle(190, 0, 12, 12), new GuiPadding(4, 4, 4, 4)));
		reg.setDefaultTexture(HOTBAR_1, new SlicedTexture(TX_SIMPLE, new GuiRectangle(202, 0, 12, 12), new GuiPadding(4, 4, 4, 4)));
		
		reg.setDefaultTexture(SCROLL_V_0, new SlicedTexture(TX_SIMPLE, new GuiRectangle(0, 12, 8, 8), new GuiPadding(3, 3, 3, 3)));
		reg.setDefaultTexture(SCROLL_V_1, new SlicedTexture(TX_SIMPLE, new GuiRectangle(8, 12, 8, 8), new GuiPadding(3, 3, 3, 3)));
		reg.setDefaultTexture(SCROLL_V_2, new SlicedTexture(TX_SIMPLE, new GuiRectangle(16, 12, 8, 8), new GuiPadding(3, 3, 3, 3)));
		
		reg.setDefaultTexture(SCROLL_H_0, new SlicedTexture(TX_SIMPLE, new GuiRectangle(24, 12, 8, 8), new GuiPadding(3, 3, 3, 3)));
		reg.setDefaultTexture(SCROLL_H_1, new SlicedTexture(TX_SIMPLE, new GuiRectangle(32, 12, 8, 8), new GuiPadding(3, 3, 3, 3)));
		reg.setDefaultTexture(SCROLL_H_2, new SlicedTexture(TX_SIMPLE, new GuiRectangle(40, 12, 8, 8), new GuiPadding(3, 3, 3, 3)));
		
		reg.setDefaultTexture(METER_V_0, new SlicedTexture(TX_SIMPLE, new GuiRectangle(48, 12, 8, 8), new GuiPadding(3, 3, 3, 3)));
		reg.setDefaultTexture(METER_V_1, new SlicedTexture(TX_SIMPLE, new GuiRectangle(52, 12, 8, 8), new GuiPadding(3, 3, 3, 3)));
		
		reg.setDefaultTexture(METER_H_0, new SlicedTexture(TX_SIMPLE, new GuiRectangle(48, 12, 8, 8), new GuiPadding(3, 3, 3, 3)));
		reg.setDefaultTexture(METER_H_1, new SlicedTexture(TX_SIMPLE, new GuiRectangle(52, 12, 8, 8), new GuiPadding(3, 3, 3, 3)));
		
		reg.registerTheme(ThemeLegacy.THEME_STD_LIGHT);
		reg.setTheme(ThemeLegacy.THEME_STD_LIGHT);
	}
}
