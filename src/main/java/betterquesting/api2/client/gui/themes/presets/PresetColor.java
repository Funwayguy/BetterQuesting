package betterquesting.api2.client.gui.themes.presets;

import java.awt.Color;
import net.minecraft.util.ResourceLocation;
import betterquesting.api2.client.gui.themes.ThemeRegistry;
import betterquesting.core.BetterQuesting;

public enum PresetColor
{
	TEXT_HEADER("text_header"),
	TEXT_MAIN("text_main"),
	TEXT_AUX_0("text_aux_0"),
	TEXT_AUX_1("text_aux_1"),
	
	GUI_DIVIDER("gui_divider"),
	
	GRID_MAJOR("grid_major"),
	GRID_MINOR("grid_minor"),
	
	BTN_DISABLED("btn_disabled"),
	BTN_IDLE("btn_idle"),
	BTN_HOVER("btn_hover"),
	
	QUEST_LINE_LOCKED("quest_line_locked"),
	QUEST_LINE_UNLOCKED("quest_line_unlocked", true),
	QUEST_LINE_PENDING("quest_line_pending"),
	QUEST_LINE_COMPLETE("quest_line_complete"),
	
	QUEST_ICON_LOCKED("quest_icon_locked"),
	QUEST_ICON_UNLOCKED("quest_icon_unlocked", true),
	QUEST_ICON_PENDING("quest_icon_pending", true),
	QUEST_ICON_COMPLETE("quest_icon_complete");
	
	private final ResourceLocation key;
	private final boolean pulse;
	
	private PresetColor(String key)
	{
		this(key, false);
	}
	
	private PresetColor(String key, boolean pulse)
	{
		this.key = new ResourceLocation(BetterQuesting.MODID, key);
		this.pulse = pulse;
	}
	
	public int getColor()
	{
		int cl = ThemeRegistry.INSTANCE.getColor(this.key);
		
		if(pulse)
		{
			float f = (float)Math.cos((System.currentTimeMillis()%1000)/1000D * Math.toRadians(360D)) / 2F + 0.5F;
			f = f * 0.5F + 0.5F;
			float la = (float)(cl >> 24 & 255) / 255.0F;
			float lr = (float)(cl >> 16 & 255) / 255.0F * f;
	        float lg = (float)(cl >> 8 & 255) / 255.0F * f;
	        float lb = (float)(cl & 255) / 255.0F * f;
	        
	        cl = new Color(lr, lg, lb, la).getRGB();
		}
		
		return cl;
	}
	
	public ResourceLocation getKey()
	{
		return this.key;
	}
	
	public static void registerColors(ThemeRegistry reg)
	{
		reg.setDefaultColor(TEXT_HEADER.key, Color.BLACK.getRGB()); // Headers
		reg.setDefaultColor(TEXT_MAIN.key, Color.BLACK.getRGB()); // Paragraphs
		reg.setDefaultColor(TEXT_AUX_0.key, Color.WHITE.getRGB()); // Dark panels
		reg.setDefaultColor(TEXT_AUX_1.key, Color.BLACK.getRGB()); // Light panels
		
		reg.setDefaultColor(GUI_DIVIDER.key, Color.BLACK.getRGB());
		
		reg.setDefaultColor(GRID_MAJOR.key, Color.BLACK.getRGB());
		reg.setDefaultColor(GRID_MINOR.key, Color.BLACK.getRGB());
		
		reg.setDefaultColor(BTN_DISABLED.key, Color.GRAY.getRGB());
		reg.setDefaultColor(BTN_IDLE.key, Color.WHITE.getRGB());
		reg.setDefaultColor(BTN_HOVER.key, 16777120);
		
		reg.setDefaultColor(QUEST_LINE_LOCKED.key, new Color(0.75F, 0F, 0F).getRGB());
		reg.setDefaultColor(QUEST_LINE_UNLOCKED.key, Color.YELLOW.getRGB());
		reg.setDefaultColor(QUEST_LINE_PENDING.key, Color.GREEN.getRGB());
		reg.setDefaultColor(QUEST_LINE_COMPLETE.key, Color.GREEN.getRGB());
		
		reg.setDefaultColor(QUEST_ICON_LOCKED.key, Color.GRAY.getRGB());
		reg.setDefaultColor(QUEST_ICON_UNLOCKED.key, new Color(0.75F, 0F, 0F).getRGB());
		reg.setDefaultColor(QUEST_ICON_PENDING.key, new Color(0F, 1F, 1F).getRGB());
		reg.setDefaultColor(QUEST_ICON_COMPLETE.key, Color.GREEN.getRGB());
	}
}
