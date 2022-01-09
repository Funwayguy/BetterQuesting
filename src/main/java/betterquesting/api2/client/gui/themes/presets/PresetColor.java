package betterquesting.api2.client.gui.themes.presets;

import betterquesting.api2.client.gui.resources.colors.GuiColorPulse;
import betterquesting.api2.client.gui.resources.colors.GuiColorStatic;
import betterquesting.api2.client.gui.resources.colors.IGuiColor;
import betterquesting.api2.client.gui.themes.IThemeRegistry;
import net.minecraft.util.ResourceLocation;
import betterquesting.client.themes.ThemeRegistry;
import betterquesting.core.BetterQuesting;

// WARNING: Don't use the ordinal indexes. I'll probably be adding/removing enums infrequently
public enum PresetColor
{
	TEXT_HEADER("text_header"),
	TEXT_MAIN("text_main"),
	TEXT_AUX_0("text_aux_0"),
	TEXT_AUX_1("text_aux_1"),
	TEXT_HIGHLIGHT("text_highlight"),
	TEXT_WATERMARK("text_watermark"),
	
	ITEM_HIGHLIGHT("item_highlight"),
	
	GUI_DIVIDER("gui_divider"),
	
	UPDATE_NOTICE("update_notice"),
	
	GRID_MAJOR("grid_major"),
	GRID_MINOR("grid_minor"),
	
	BTN_DISABLED("btn_disabled"),
	BTN_IDLE("btn_idle"),
	BTN_HOVER("btn_hover"),
	
	QUEST_LINE_LOCKED("quest_line_locked"),
	QUEST_LINE_UNLOCKED("quest_line_unlocked"),
	QUEST_LINE_PENDING("quest_line_pending"),
	QUEST_LINE_COMPLETE("quest_line_complete"),
	QUEST_LINE_REPEATABLE("quest_line_repeatable"),


	QUEST_ICON_LOCKED("quest_icon_locked"),
	QUEST_ICON_UNLOCKED("quest_icon_unlocked"),
	QUEST_ICON_PENDING("quest_icon_pending"),
	QUEST_ICON_COMPLETE("quest_icon_complete"),
	QUEST_ICON_REPEATABLE("quest_icon_repeatable");

	private final ResourceLocation key;
	
	PresetColor(String key)
	{
		this.key = new ResourceLocation(BetterQuesting.MODID, key);
	}
	
	public IGuiColor getColor()
	{
		return ThemeRegistry.INSTANCE.getColor(this.key);
	}
	
	public ResourceLocation getKey()
	{
		return this.key;
	}
	
	public static void registerColors(IThemeRegistry reg)
	{
		reg.setDefaultColor(TEXT_HEADER.key, new GuiColorStatic(0, 0, 0, 255)); // Headers
		reg.setDefaultColor(TEXT_MAIN.key, new GuiColorStatic(0, 0, 0, 255)); // Paragraphs
		reg.setDefaultColor(TEXT_AUX_0.key, new GuiColorStatic(255, 255, 255, 255)); // Dark panels (White Text)
		reg.setDefaultColor(TEXT_AUX_1.key, new GuiColorStatic(0, 0, 0, 255)); // Light panels (Black Text)
		reg.setDefaultColor(TEXT_HIGHLIGHT.key, new GuiColorStatic(0, 0, 255, 255)); // Selection Highlight (Blue Invert)
		reg.setDefaultColor(TEXT_WATERMARK.key, new GuiColorStatic(128, 128, 128, 255)); // Text Field Watermark (Grey Text)
		
		reg.setDefaultColor(ITEM_HIGHLIGHT.key, new GuiColorStatic(255, 255, 255, 128));
		
		reg.setDefaultColor(GUI_DIVIDER.key, new GuiColorStatic(0, 0, 0, 255));
		
		reg.setDefaultColor(UPDATE_NOTICE.key, new GuiColorPulse(quickMix(255, 255, 0, 255), quickMix(128, 128, 0, 255), 1F, 0F));
		
		reg.setDefaultColor(GRID_MAJOR.key, new GuiColorStatic(0, 0, 0, 255));
		reg.setDefaultColor(GRID_MINOR.key, new GuiColorStatic(0, 0, 0, 255));
		
		reg.setDefaultColor(BTN_DISABLED.key, new GuiColorStatic(128, 128, 128, 255));
		reg.setDefaultColor(BTN_IDLE.key, new GuiColorStatic(255, 255, 255, 255));
		reg.setDefaultColor(BTN_HOVER.key, new GuiColorStatic(16777120));
		
		reg.setDefaultColor(QUEST_LINE_LOCKED.key, new GuiColorStatic(192, 0, 0, 255));
		reg.setDefaultColor(QUEST_LINE_UNLOCKED.key, new GuiColorPulse(quickMix(255, 255, 0, 255), quickMix(128, 128, 0, 255), 1F, 0F));
		reg.setDefaultColor(QUEST_LINE_PENDING.key, new GuiColorStatic(0, 255, 0, 255));
		reg.setDefaultColor(QUEST_LINE_COMPLETE.key, new GuiColorStatic(0, 255, 0, 255));
		
		reg.setDefaultColor(QUEST_ICON_LOCKED.key, new GuiColorStatic(128, 128, 128, 255));
		reg.setDefaultColor(QUEST_ICON_UNLOCKED.key, new GuiColorPulse(quickMix(192, 0, 0, 255), quickMix(96, 0, 0, 255), 1F, 0F));
		reg.setDefaultColor(QUEST_ICON_PENDING.key, new GuiColorPulse(quickMix(0, 255, 255, 255), quickMix(0, 128, 128, 255), 1F, 0F));
		reg.setDefaultColor(QUEST_ICON_COMPLETE.key, new GuiColorStatic(0, 255, 0, 255));
		reg.setDefaultColor(QUEST_ICON_REPEATABLE.key, new GuiColorPulse(quickMix(255, 69, 0, 255), quickMix(255, 96, 0, 255), 1F, 0F));
	}
	
	/**
	 * Colour mixing for the lazy
	 * @param red Red colour channel
	 * @param green Green colour channel
	 * @param blue Blue colour channel
	 * @param alpha Alpha transparency channel
	 * @return RGBA integer representation of the colour
	 */
	public static int quickMix(int red, int green, int blue, int alpha)
	{
		return ((alpha & 255) << 24) | ((red & 255) << 16) | ((green & 255) << 8) | (blue & 255);
	}
}
