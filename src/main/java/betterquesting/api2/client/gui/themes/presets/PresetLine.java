package betterquesting.api2.client.gui.themes.presets;

import betterquesting.api2.client.gui.themes.IThemeRegistry;
import net.minecraft.util.ResourceLocation;
import betterquesting.api2.client.gui.resources.lines.IGuiLine;
import betterquesting.api2.client.gui.resources.lines.SimpleLine;
import betterquesting.client.themes.ThemeRegistry;
import betterquesting.core.BetterQuesting;

public enum PresetLine
{
	GUI_DIVIDER("gui_divider"),
	
	GRID_MAJOR("grid_major"),
	GRID_MINOR("grid_minor"),
	
	QUEST_LOCKED("quest_locked"),
	QUEST_UNLOCKED("quest_unlocked"),
	QUEST_PENDING("quest_pending"),
	QUEST_COMPLETE("quest_complete"),
	QUEST_REPEATABLE("quest_repeatable");
	
	private final ResourceLocation key;
	
	PresetLine(String key)
	{
		this.key = new ResourceLocation(BetterQuesting.MODID, key);
	}
	
	public IGuiLine getLine()
	{
		return ThemeRegistry.INSTANCE.getLine(this.key);
	}
	
	public ResourceLocation getKey()
	{
		return this.key;
	}
	
	public static void registerLines(IThemeRegistry reg)
	{
		reg.setDefaultLine(GUI_DIVIDER.key, new SimpleLine());
		
		reg.setDefaultLine(GRID_MAJOR.key, new SimpleLine());
		reg.setDefaultLine(GRID_MINOR.key, new SimpleLine(2, (short)43690));
		
		reg.setDefaultLine(QUEST_LOCKED.key, new SimpleLine());
		reg.setDefaultLine(QUEST_UNLOCKED.key, new SimpleLine());
		reg.setDefaultLine(QUEST_PENDING.key, new SimpleLine());
		reg.setDefaultLine(QUEST_COMPLETE.key, new SimpleLine());
		reg.setDefaultLine(QUEST_REPEATABLE.key, new SimpleLine());
	}
}
