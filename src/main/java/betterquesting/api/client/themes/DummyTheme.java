package betterquesting.api.client.themes;

import java.awt.Color;
import net.minecraft.util.ResourceLocation;
import betterquesting.api.client.gui.IGuiEmbedded;
import betterquesting.api.enums.EnumQuestState;
import betterquesting.api.quests.IQuestContainer;

/**
 * A dummy theme used by GuiElements when BetterQuesting isn't loaded
 */
public final class DummyTheme implements IThemeBase
{
	public static final DummyTheme INSTANCE = new DummyTheme();
	
	private final ResourceLocation TEX = new ResourceLocation("missingno");
	private final ResourceLocation ID = new ResourceLocation("NULL");
	
	private DummyTheme()
	{
	}
	
	@Override
	public ResourceLocation getThemeID()
	{
		return ID;
	}
	
	@Override
	public String getDisplayName()
	{
		return "NULL";
	}
	
	@Override
	public ResourceLocation getGuiTexture()
	{
		return TEX;
	}
	
	@Override
	public int getQuestIconColor(IQuestContainer quest, EnumQuestState state, int hoverState)
	{
		return Color.GRAY.getRGB();
	}
	
	@Override
	public int getQuestLineColor(IQuestContainer quest, EnumQuestState state)
	{
		return Color.GRAY.getRGB();
	}
	
	@Override
	public int getTextColor()
	{
		return Color.BLACK.getRGB();
	}
	
	@Override
	public int getLineStipple(IQuestContainer quest, EnumQuestState state)
	{
		return 0xAAAA;
	}
	
	@Override
	public float getLineWidth(IQuestContainer quest, EnumQuestState state)
	{
		return 4F;
	}
	
	@Override
	public IGuiEmbedded getGuiOverride(IGuiEmbedded gui)
	{
		return gui;
	}

	@Override
	public ResourceLocation getButtonSound()
	{
		return new ResourceLocation("gui.button.press");
	}
}
