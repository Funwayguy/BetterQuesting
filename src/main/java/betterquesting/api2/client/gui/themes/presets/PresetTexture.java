 package betterquesting.api2.client.gui.themes.presets;

import net.minecraft.util.ResourceLocation;
import betterquesting.api2.client.gui.misc.GuiPadding;
import betterquesting.api2.client.gui.misc.GuiRectangle;
import betterquesting.api2.client.gui.resources.IGuiTexture;
import betterquesting.api2.client.gui.resources.SlicedTexture;
import betterquesting.api2.client.gui.resources.SlicedTexture.SliceMode;
import betterquesting.api2.client.gui.themes.ThemeRegistry;
import betterquesting.core.BetterQuesting;

public enum PresetTexture
{
	PANEL_MAIN("panel_main"),
	PANEL_DARK("panel_dark"),
	PANEL_INNER("panel_inner"),
	
	ITEM_FRAME("item_frame"),
	AUX_FRAME_0("aux_frame_0"),
	AUX_FRAME_1("aux_frame_1"),
	
	BTN_NORMAL_0("btn_normal_0"),
	BTN_NORMAL_1("btn_normal_1"),
	BTN_NORMAL_2("btn_normal_2"),
	
	BTN_CLEAN_0("btn_clean_0"),
	BTN_CLEAN_1("btn_clean_1"),
	BTN_CLEAN_2("btn_clean_2"),
	
	BTN_ALT_0("btn_alt_0"),
	BTN_ALT_1("btn_alt_1"),
	BTN_ALT_2("btn_alt_2"),
	
	HOTBAR_0("hotbar_0"),
	HOTBAR_1("hotbar_1"),
	
	SCROLL_V_0("scroll_v_0"),
	SCROLL_V_1("scroll_v_1"),
	SCROLL_V_2("scroll_v_2"),
	
	SCROLL_H_0("scroll_h_0"),
	SCROLL_H_1("scroll_h_1"),
	SCROLL_H_2("scroll_h_2"),
	
	METER_V_0("meter_v_0"),
	METER_V_1("meter_v_1"),
	
	METER_H_0("meter_h_0"),
	METER_H_1("meter_h_1"),
	
	// Normal quest frame
	QUEST_NORM_0("quest_norm_0"),
	QUEST_NORM_1("quest_norm_1"),
	QUEST_NORM_2("quest_norm_2"),
	QUEST_NORM_3("quest_norm_3"),
	
	// Main quest frame
	QUEST_MAIN_0("quest_main_0"),
	QUEST_MAIN_1("quest_main_1"),
	QUEST_MAIN_2("quest_main_2"),
	QUEST_MAIN_3("quest_main_3"),
	
	// Auxiliary quest frame (not normally used)
	QUEST_AUX_0("quest_aux_0"),
	QUEST_AUX_1("quest_aux_1"),
	QUEST_AUX_2("quest_aux_2"),
	QUEST_AUX_3("quest_aux_3");
	
	public static final ResourceLocation TX_SIMPLE = new ResourceLocation(BetterQuesting.MODID, "textures/gui/simple_frames.png");
	public static final ResourceLocation TX_QUEST = new ResourceLocation(BetterQuesting.MODID, "textures/gui/quest_frames.png");
	
	private final ResourceLocation key;
	
	private PresetTexture(String key)
	{
		this.key = new ResourceLocation(BetterQuesting.MODID, key);
	}
	
	public IGuiTexture getTexture()
	{
		return ThemeRegistry.INSTANCE.getTexture(this.key);
	}
	
	public ResourceLocation getKey()
	{
		return this.key;
	}
	
	public static void registerTextures(ThemeRegistry reg)
	{
		reg.setDefaultTexture(PANEL_MAIN.key, new SlicedTexture(TX_SIMPLE, new GuiRectangle(0, 0, 12, 12), new GuiPadding(4, 4, 4, 4)));
		reg.setDefaultTexture(PANEL_DARK.key, new SlicedTexture(TX_SIMPLE, new GuiRectangle(12, 0, 12, 12), new GuiPadding(4, 4, 4, 4)));
		reg.setDefaultTexture(PANEL_INNER.key, new SlicedTexture(TX_SIMPLE, new GuiRectangle(24, 0, 12, 12), new GuiPadding(3, 3, 3, 3)));
		
		reg.setDefaultTexture(ITEM_FRAME.key, new SlicedTexture(TX_SIMPLE, new GuiRectangle(36, 0, 12, 12), new GuiPadding(1, 1, 1, 1)));
		reg.setDefaultTexture(AUX_FRAME_0.key, new SlicedTexture(TX_SIMPLE, new GuiRectangle(48, 0, 12, 12), new GuiPadding(1, 1, 1, 1)));
		reg.setDefaultTexture(AUX_FRAME_1.key, new SlicedTexture(TX_SIMPLE, new GuiRectangle(60, 0, 12, 12), new GuiPadding(1, 1, 1, 1)));
		
		reg.setDefaultTexture(BTN_NORMAL_0.key, new SlicedTexture(TX_SIMPLE, new GuiRectangle(72, 0, 12, 12), new GuiPadding(2, 2, 2, 3)));
		reg.setDefaultTexture(BTN_NORMAL_1.key, new SlicedTexture(TX_SIMPLE, new GuiRectangle(84, 0, 12, 12), new GuiPadding(2, 2, 2, 3)));
		reg.setDefaultTexture(BTN_NORMAL_2.key, new SlicedTexture(TX_SIMPLE, new GuiRectangle(96, 0, 12, 12), new GuiPadding(2, 2, 2, 3)));
		
		reg.setDefaultTexture(BTN_CLEAN_0.key, new SlicedTexture(TX_SIMPLE, new GuiRectangle(108, 0, 12, 12), new GuiPadding(2, 2, 2, 2)));
		reg.setDefaultTexture(BTN_CLEAN_1.key, new SlicedTexture(TX_SIMPLE, new GuiRectangle(120, 0, 12, 12), new GuiPadding(2, 2, 2, 2)));
		reg.setDefaultTexture(BTN_CLEAN_2.key, new SlicedTexture(TX_SIMPLE, new GuiRectangle(132, 0, 12, 12), new GuiPadding(2, 2, 2, 2)));
		
		reg.setDefaultTexture(BTN_ALT_0.key, new SlicedTexture(TX_SIMPLE, new GuiRectangle(144, 0, 12, 12), new GuiPadding(2, 2, 2, 2)));
		reg.setDefaultTexture(BTN_ALT_1.key, new SlicedTexture(TX_SIMPLE, new GuiRectangle(156, 0, 12, 12), new GuiPadding(2, 2, 2, 2)));
		reg.setDefaultTexture(BTN_ALT_2.key, new SlicedTexture(TX_SIMPLE, new GuiRectangle(178, 0, 12, 12), new GuiPadding(2, 2, 2, 2)));
		
		reg.setDefaultTexture(HOTBAR_0.key, new SlicedTexture(TX_SIMPLE, new GuiRectangle(190, 0, 12, 12), new GuiPadding(3, 3, 2, 2)));
		reg.setDefaultTexture(HOTBAR_1.key, new SlicedTexture(TX_SIMPLE, new GuiRectangle(202, 0, 12, 12), new GuiPadding(3, 3, 3, 3)));
		
		reg.setDefaultTexture(SCROLL_V_0.key, new SlicedTexture(TX_SIMPLE, new GuiRectangle(0, 12, 8, 8), new GuiPadding(3, 3, 3, 3)));
		reg.setDefaultTexture(SCROLL_V_1.key, new SlicedTexture(TX_SIMPLE, new GuiRectangle(8, 12, 8, 8), new GuiPadding(3, 3, 3, 3)));
		reg.setDefaultTexture(SCROLL_V_2.key, new SlicedTexture(TX_SIMPLE, new GuiRectangle(16, 12, 8, 8), new GuiPadding(3, 3, 3, 3)));
		
		reg.setDefaultTexture(SCROLL_H_0.key, new SlicedTexture(TX_SIMPLE, new GuiRectangle(24, 12, 8, 8), new GuiPadding(3, 3, 3, 3)));
		reg.setDefaultTexture(SCROLL_H_1.key, new SlicedTexture(TX_SIMPLE, new GuiRectangle(32, 12, 8, 8), new GuiPadding(3, 3, 3, 3)));
		reg.setDefaultTexture(SCROLL_H_2.key, new SlicedTexture(TX_SIMPLE, new GuiRectangle(40, 12, 8, 8), new GuiPadding(3, 3, 3, 3)));
		
		reg.setDefaultTexture(METER_V_0.key, new SlicedTexture(TX_SIMPLE, new GuiRectangle(48, 12, 8, 8), new GuiPadding(3, 3, 3, 3)));
		reg.setDefaultTexture(METER_V_1.key, new SlicedTexture(TX_SIMPLE, new GuiRectangle(52, 12, 8, 8), new GuiPadding(3, 3, 3, 3)));
		
		reg.setDefaultTexture(METER_H_0.key, new SlicedTexture(TX_SIMPLE, new GuiRectangle(48, 12, 8, 8), new GuiPadding(3, 3, 3, 3)));
		reg.setDefaultTexture(METER_H_1.key, new SlicedTexture(TX_SIMPLE, new GuiRectangle(52, 12, 8, 8), new GuiPadding(3, 3, 3, 3)));
		
		reg.setDefaultTexture(QUEST_NORM_0.key, new SlicedTexture(TX_QUEST, new GuiRectangle(0, 0, 24, 24), new GuiPadding(8, 8, 8, 8)).setSliceMode(SliceMode.SLICED_STRETCH));
		reg.setDefaultTexture(QUEST_NORM_1.key, new SlicedTexture(TX_QUEST, new GuiRectangle(0, 24, 24, 24), new GuiPadding(8, 8, 8, 8)).setSliceMode(SliceMode.SLICED_STRETCH));
		reg.setDefaultTexture(QUEST_NORM_2.key, new SlicedTexture(TX_QUEST, new GuiRectangle(0, 48, 24, 24), new GuiPadding(8, 8, 8, 8)).setSliceMode(SliceMode.SLICED_STRETCH));
		reg.setDefaultTexture(QUEST_NORM_3.key, new SlicedTexture(TX_QUEST, new GuiRectangle(0, 72, 24, 24), new GuiPadding(8, 8, 8, 8)).setSliceMode(SliceMode.SLICED_STRETCH));
		
		reg.setDefaultTexture(QUEST_MAIN_0.key, new SlicedTexture(TX_QUEST, new GuiRectangle(24, 0, 24, 24), new GuiPadding(8, 8, 8, 8)).setSliceMode(SliceMode.STRETCH));
		reg.setDefaultTexture(QUEST_MAIN_1.key, new SlicedTexture(TX_QUEST, new GuiRectangle(24, 24, 24, 24), new GuiPadding(8, 8, 8, 8)).setSliceMode(SliceMode.STRETCH));
		reg.setDefaultTexture(QUEST_MAIN_2.key, new SlicedTexture(TX_QUEST, new GuiRectangle(24, 48, 24, 24), new GuiPadding(8, 8, 8, 8)).setSliceMode(SliceMode.STRETCH));
		reg.setDefaultTexture(QUEST_MAIN_3.key, new SlicedTexture(TX_QUEST, new GuiRectangle(24, 72, 24, 24), new GuiPadding(8, 8, 8, 8)).setSliceMode(SliceMode.STRETCH));
		
		reg.setDefaultTexture(QUEST_AUX_0.key, new SlicedTexture(TX_QUEST, new GuiRectangle(48, 0, 24, 24), new GuiPadding(8, 8, 8, 8)).setSliceMode(SliceMode.SLICED_STRETCH));
		reg.setDefaultTexture(QUEST_AUX_1.key, new SlicedTexture(TX_QUEST, new GuiRectangle(48, 24, 24, 24), new GuiPadding(8, 8, 8, 8)).setSliceMode(SliceMode.SLICED_STRETCH));
		reg.setDefaultTexture(QUEST_AUX_2.key, new SlicedTexture(TX_QUEST, new GuiRectangle(48, 48, 24, 24), new GuiPadding(8, 8, 8, 8)).setSliceMode(SliceMode.SLICED_STRETCH));
		reg.setDefaultTexture(QUEST_AUX_3.key, new SlicedTexture(TX_QUEST, new GuiRectangle(48, 72, 24, 24), new GuiPadding(8, 8, 8, 8)).setSliceMode(SliceMode.SLICED_STRETCH));
	}
}
