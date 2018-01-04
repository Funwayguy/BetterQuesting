package betterquesting.api2.client.gui.themes.presets;

import java.awt.Color;
import net.minecraft.util.ResourceLocation;
import betterquesting.api2.client.gui.themes.ThemeRegistry;
import betterquesting.core.BetterQuesting;

public enum PresetColor
{
	TEXT_HEADER("header"),
	TEXT_MAIN("main"),
	
	GUI_DIVIDER("divider"),
	
	GRID_MAJOR("grid_major"),
	GRID_MINOR("grid_minor"),
	
	BTN_DISABLED("btn_disabled"),
	BTN_IDLE("btn_idle"),
	BTN_HOVER("btn_hover");
	
	private final ResourceLocation key;
	
	private PresetColor(String key)
	{
		this.key = new ResourceLocation(BetterQuesting.MODID, key);
	}
	
	public int getColor()
	{
		return ThemeRegistry.INSTANCE.getColor(this.key);
	}
	
	public ResourceLocation getKey()
	{
		return this.key;
	}
	
	public static void registerColors(ThemeRegistry reg)
	{
		reg.setDefaultColor(TEXT_HEADER.key, Color.BLACK.getRGB());
		reg.setDefaultColor(TEXT_MAIN.key, Color.BLACK.getRGB());
		
		reg.setDefaultColor(GUI_DIVIDER.key, Color.BLACK.getRGB());
		
		reg.setDefaultColor(GRID_MAJOR.key, Color.BLACK.getRGB());
		reg.setDefaultColor(GRID_MINOR.key, Color.BLACK.getRGB());
		
		reg.setDefaultColor(BTN_DISABLED.key, Color.GRAY.getRGB());
		reg.setDefaultColor(BTN_IDLE.key, Color.WHITE.getRGB());
		reg.setDefaultColor(BTN_HOVER.key, 16777120);
	}
}
