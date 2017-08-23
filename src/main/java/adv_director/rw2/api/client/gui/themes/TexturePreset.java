package adv_director.rw2.api.client.gui.themes;

import org.lwjgl.util.Rectangle;
import adv_director.core.AdvDirector;
import adv_director.rw2.api.client.gui.misc.GuiPadding;
import adv_director.rw2.api.client.gui.resources.SlicedTexture;
import net.minecraft.util.ResourceLocation;

public class TexturePreset
{
	public static final ResourceLocation TX_LEGACY = new ResourceLocation(AdvDirector.MODID, "textures/gui/legacy_gui.png");
	public static final ResourceLocation TX_SIMPLE = new ResourceLocation(AdvDirector.MODID, "textures/gui/simple_frames.png");
	
	public static final ResourceLocation PANEL_MAIN = new ResourceLocation(AdvDirector.MODID, "panel_main");
	public static final ResourceLocation PANEL_DARK = new ResourceLocation(AdvDirector.MODID, "panel_dark");
	public static final ResourceLocation PANEL_INNER = new ResourceLocation(AdvDirector.MODID, "panel_inner");
	
	public static final ResourceLocation ITEM_FRAME = new ResourceLocation(AdvDirector.MODID, "item_frame");
	public static final ResourceLocation AUX_FRAME_0 = new ResourceLocation(AdvDirector.MODID, "aux_frame_0");
	public static final ResourceLocation AUX_FRAME_1 = new ResourceLocation(AdvDirector.MODID, "aux_frame_1");
	
	public static final ResourceLocation BTN_NORMAL_0 = new ResourceLocation(AdvDirector.MODID, "btn_normal_0");
	public static final ResourceLocation BTN_NORMAL_1 = new ResourceLocation(AdvDirector.MODID, "btn_normal_1");
	public static final ResourceLocation BTN_NORMAL_2 = new ResourceLocation(AdvDirector.MODID, "btn_normal_2");
	
	public static final ResourceLocation BTN_CLEAN_0 = new ResourceLocation(AdvDirector.MODID, "btn_clean_0");
	public static final ResourceLocation BTN_CLEAN_1 = new ResourceLocation(AdvDirector.MODID, "btn_clean_1");
	public static final ResourceLocation BTN_CLEAN_2 = new ResourceLocation(AdvDirector.MODID, "btn_clean_2");
	
	public static final ResourceLocation BTN_ALT_0 = new ResourceLocation(AdvDirector.MODID, "btn_alt_0");
	public static final ResourceLocation BTN_ALT_1 = new ResourceLocation(AdvDirector.MODID, "btn_alt_1");
	public static final ResourceLocation BTN_ALT_2 = new ResourceLocation(AdvDirector.MODID, "btn_alt_2");
	
	public static final ResourceLocation HOTBAR_0 = new ResourceLocation(AdvDirector.MODID, "hotbar_0");
	public static final ResourceLocation HOTBAR_1 = new ResourceLocation(AdvDirector.MODID, "hotbar_1");
	
	public static final ResourceLocation SCROLL_V_0 = new ResourceLocation(AdvDirector.MODID, "scroll_v_0");
	public static final ResourceLocation SCROLL_V_1 = new ResourceLocation(AdvDirector.MODID, "scroll_v_1");
	public static final ResourceLocation SCROLL_V_2 = new ResourceLocation(AdvDirector.MODID, "scroll_v_2");
	
	public static final ResourceLocation SCROLL_H_0 = new ResourceLocation(AdvDirector.MODID, "scroll_h_0");
	public static final ResourceLocation SCROLL_H_1 = new ResourceLocation(AdvDirector.MODID, "scroll_h_1");
	public static final ResourceLocation SCROLL_H_2 = new ResourceLocation(AdvDirector.MODID, "scroll_h_2");
	
	public static final ResourceLocation METER_V_0 = new ResourceLocation(AdvDirector.MODID, "meter_v_0");
	public static final ResourceLocation METER_V_1 = new ResourceLocation(AdvDirector.MODID, "meter_v_1");
	
	public static final ResourceLocation METER_H_0 = new ResourceLocation(AdvDirector.MODID, "meter_h_0");
	public static final ResourceLocation METER_H_1 = new ResourceLocation(AdvDirector.MODID, "meter_h_1");
	
	protected static void initPresets(ThemeRegistry reg)
	{
		reg.setDefaultTexture(PANEL_MAIN, new SlicedTexture(TX_SIMPLE, new Rectangle(0, 0, 12, 12), new GuiPadding(4, 4, 4, 4)));
		reg.setDefaultTexture(PANEL_DARK, new SlicedTexture(TX_SIMPLE, new Rectangle(12, 0, 12, 12), new GuiPadding(4, 4, 4, 4)));
		reg.setDefaultTexture(PANEL_INNER, new SlicedTexture(TX_SIMPLE, new Rectangle(24, 0, 12, 12), new GuiPadding(4, 4, 4, 4)));
		
		reg.setDefaultTexture(ITEM_FRAME, new SlicedTexture(TX_SIMPLE, new Rectangle(36, 0, 12, 12), new GuiPadding(4, 4, 4, 4)));
		reg.setDefaultTexture(AUX_FRAME_0, new SlicedTexture(TX_SIMPLE, new Rectangle(48, 0, 12, 12), new GuiPadding(4, 4, 4, 4)));
		reg.setDefaultTexture(AUX_FRAME_1, new SlicedTexture(TX_SIMPLE, new Rectangle(60, 0, 12, 12), new GuiPadding(4, 4, 4, 4)));
		
		reg.setDefaultTexture(BTN_NORMAL_0, new SlicedTexture(TX_SIMPLE, new Rectangle(72, 0, 12, 12), new GuiPadding(4, 4, 4, 4)));
		reg.setDefaultTexture(BTN_NORMAL_1, new SlicedTexture(TX_SIMPLE, new Rectangle(84, 0, 12, 12), new GuiPadding(4, 4, 4, 4)));
		reg.setDefaultTexture(BTN_NORMAL_2, new SlicedTexture(TX_SIMPLE, new Rectangle(96, 0, 12, 12), new GuiPadding(4, 4, 4, 4)));
		
		reg.setDefaultTexture(BTN_CLEAN_0, new SlicedTexture(TX_SIMPLE, new Rectangle(108, 0, 12, 12), new GuiPadding(4, 4, 4, 4)));
		reg.setDefaultTexture(BTN_CLEAN_1, new SlicedTexture(TX_SIMPLE, new Rectangle(120, 0, 12, 12), new GuiPadding(4, 4, 4, 4)));
		reg.setDefaultTexture(BTN_CLEAN_2, new SlicedTexture(TX_SIMPLE, new Rectangle(132, 0, 12, 12), new GuiPadding(4, 4, 4, 4)));
		
		reg.setDefaultTexture(BTN_ALT_0, new SlicedTexture(TX_SIMPLE, new Rectangle(144, 0, 12, 12), new GuiPadding(4, 4, 4, 4)));
		reg.setDefaultTexture(BTN_ALT_1, new SlicedTexture(TX_SIMPLE, new Rectangle(156, 0, 12, 12), new GuiPadding(4, 4, 4, 4)));
		reg.setDefaultTexture(BTN_ALT_2, new SlicedTexture(TX_SIMPLE, new Rectangle(178, 0, 12, 12), new GuiPadding(4, 4, 4, 4)));
		
		reg.setDefaultTexture(HOTBAR_0, new SlicedTexture(TX_SIMPLE, new Rectangle(190, 0, 12, 12), new GuiPadding(4, 4, 4, 4)));
		reg.setDefaultTexture(HOTBAR_1, new SlicedTexture(TX_SIMPLE, new Rectangle(202, 0, 12, 12), new GuiPadding(4, 4, 4, 4)));
		
		reg.setDefaultTexture(SCROLL_V_0, new SlicedTexture(TX_SIMPLE, new Rectangle(0, 12, 8, 8), new GuiPadding(3, 3, 3, 3)));
		reg.setDefaultTexture(SCROLL_V_1, new SlicedTexture(TX_SIMPLE, new Rectangle(8, 12, 8, 8), new GuiPadding(3, 3, 3, 3)));
		reg.setDefaultTexture(SCROLL_V_2, new SlicedTexture(TX_SIMPLE, new Rectangle(16, 12, 8, 8), new GuiPadding(3, 3, 3, 3)));
		
		reg.setDefaultTexture(SCROLL_H_0, new SlicedTexture(TX_SIMPLE, new Rectangle(24, 12, 8, 8), new GuiPadding(3, 3, 3, 3)));
		reg.setDefaultTexture(SCROLL_H_1, new SlicedTexture(TX_SIMPLE, new Rectangle(32, 12, 8, 8), new GuiPadding(3, 3, 3, 3)));
		reg.setDefaultTexture(SCROLL_H_2, new SlicedTexture(TX_SIMPLE, new Rectangle(40, 12, 8, 8), new GuiPadding(3, 3, 3, 3)));
		
		reg.setDefaultTexture(METER_V_0, new SlicedTexture(TX_SIMPLE, new Rectangle(48, 12, 8, 8), new GuiPadding(3, 3, 3, 3)));
		reg.setDefaultTexture(METER_V_1, new SlicedTexture(TX_SIMPLE, new Rectangle(52, 12, 8, 8), new GuiPadding(3, 3, 3, 3)));
		
		reg.setDefaultTexture(METER_H_0, new SlicedTexture(TX_SIMPLE, new Rectangle(48, 12, 8, 8), new GuiPadding(3, 3, 3, 3)));
		reg.setDefaultTexture(METER_H_1, new SlicedTexture(TX_SIMPLE, new Rectangle(52, 12, 8, 8), new GuiPadding(3, 3, 3, 3)));
	}
}
