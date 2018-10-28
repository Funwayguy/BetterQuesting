package betterquesting.api2.client.gui.themes;

import betterquesting.api.client.themes.ITheme;
import betterquesting.api.enums.EnumQuestState;
import betterquesting.api2.client.gui.misc.GuiPadding;
import betterquesting.api2.client.gui.misc.GuiRectangle;
import betterquesting.api2.client.gui.resources.colors.GuiColorStatic;
import betterquesting.api2.client.gui.resources.colors.IGuiColor;
import betterquesting.api2.client.gui.resources.lines.IGuiLine;
import betterquesting.api2.client.gui.resources.textures.IGuiTexture;
import betterquesting.api2.client.gui.resources.textures.SlicedTexture;
import betterquesting.api2.client.gui.resources.textures.SlicedTexture.SliceMode;
import betterquesting.api2.client.gui.themes.presets.PresetColor;
import betterquesting.api2.client.gui.themes.presets.PresetLine;
import betterquesting.api2.client.gui.themes.presets.PresetTexture;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;

public class LegacyThemeWrapper implements IGuiTheme
{
	private final ITheme oldTheme;
	//private final IGuiLine line = new SimpleLine();
	private final IGuiColor color;
	private final IGuiColor colLine;
	
	private final HashMap<ResourceLocation, IGuiTexture> TEX_MAP = new HashMap<>();
	private final HashMap<ResourceLocation, IGuiLine> LINE_MAP = new HashMap<>();
	
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

		TEX_MAP.put(PresetTexture.SCROLL_V_BG.getKey(), new SlicedTexture(oldTheme.getGuiTexture(), new GuiRectangle(248, 0, 8, 60), new GuiPadding(1, 1, 1, 1)).setSliceMode(SliceMode.SLICED_STRETCH));
		TEX_MAP.put(PresetTexture.SCROLL_V_0.getKey(), new SlicedTexture(oldTheme.getGuiTexture(), new GuiRectangle(248, 60, 8, 20), new GuiPadding(1, 1, 1, 1)).setSliceMode(SliceMode.SLICED_STRETCH));
		TEX_MAP.put(PresetTexture.SCROLL_V_1.getKey(), new SlicedTexture(oldTheme.getGuiTexture(), new GuiRectangle(248, 60, 8, 20), new GuiPadding(1, 1, 1, 1)).setSliceMode(SliceMode.SLICED_STRETCH));
		TEX_MAP.put(PresetTexture.SCROLL_V_2.getKey(), new SlicedTexture(oldTheme.getGuiTexture(), new GuiRectangle(248, 60, 8, 20), new GuiPadding(1, 1, 1, 1)).setSliceMode(SliceMode.SLICED_STRETCH));
		
		TEX_MAP.put(PresetTexture.ITEM_FRAME.getKey(), new SlicedTexture(oldTheme.getGuiTexture(), new GuiRectangle(0, 48, 18, 18), new GuiPadding(1, 1, 1, 1)).setSliceMode(SliceMode.SLICED_STRETCH));
		TEX_MAP.put(PresetTexture.AUX_FRAME_0.getKey(), new SlicedTexture(oldTheme.getGuiTexture(), new GuiRectangle(0, 128, 128, 128), new GuiPadding(1, 1, 1, 1)).setSliceMode(SliceMode.SLICED_STRETCH));
		
		IGuiTexture qTexMain = new SlicedTexture(oldTheme.getGuiTexture(), new GuiRectangle(0, 104, 24, 24), new GuiPadding(8, 8, 8, 8));
		TEX_MAP.put(PresetTexture.QUEST_NORM_0.getKey(), qTexMain);
		TEX_MAP.put(PresetTexture.QUEST_NORM_1.getKey(), qTexMain);
		TEX_MAP.put(PresetTexture.QUEST_NORM_2.getKey(), qTexMain);
		TEX_MAP.put(PresetTexture.QUEST_NORM_3.getKey(), qTexMain);
		
		IGuiTexture qTexNorm = new SlicedTexture(oldTheme.getGuiTexture(), new GuiRectangle(24, 104, 24, 24), new GuiPadding(8, 8, 8, 8));
		TEX_MAP.put(PresetTexture.QUEST_MAIN_0.getKey(), qTexNorm);
		TEX_MAP.put(PresetTexture.QUEST_MAIN_1.getKey(), qTexNorm);
		TEX_MAP.put(PresetTexture.QUEST_MAIN_2.getKey(), qTexNorm);
		TEX_MAP.put(PresetTexture.QUEST_MAIN_3.getKey(), qTexNorm);
		
		LINE_MAP.put(PresetLine.QUEST_LOCKED.getKey(), new LegacyLineWrapper(oldTheme, EnumQuestState.LOCKED));
		LINE_MAP.put(PresetLine.QUEST_UNLOCKED.getKey(), new LegacyLineWrapper(oldTheme, EnumQuestState.UNLOCKED));
		LINE_MAP.put(PresetLine.QUEST_PENDING.getKey(), new LegacyLineWrapper(oldTheme, EnumQuestState.UNCLAIMED));
		LINE_MAP.put(PresetLine.QUEST_COMPLETE.getKey(), new LegacyLineWrapper(oldTheme, EnumQuestState.COMPLETED));
		
		this.color = new GuiColorStatic(oldTheme.getTextColor());
		this.colLine = new GuiColorStatic(0xFFFFFFFF);
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
		return LINE_MAP.get(key);
	}
	
	@Override
	public IGuiColor getColor(ResourceLocation key)
	{
		if(key == null || key.equals(PresetColor.TEXT_MAIN.getKey()) || key.equals(PresetColor.TEXT_HEADER.getKey()) || key.equals(PresetColor.TEXT_AUX_1.getKey()) || key.equals(PresetColor.GUI_DIVIDER.getKey()))
		{
			return this.color;
		} else if(key.equals(PresetColor.QUEST_LINE_LOCKED.getKey()) || key.equals(PresetColor.QUEST_LINE_UNLOCKED.getKey()) || key.equals(PresetColor.QUEST_LINE_PENDING.getKey()) || key.equals(PresetColor.QUEST_LINE_COMPLETE.getKey()))
		{
			return this.colLine;
		}
		
		return null;
	}
}
